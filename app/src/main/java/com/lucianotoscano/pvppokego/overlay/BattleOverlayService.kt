package com.lucianotoscano.pvppokego.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.lucianotoscano.pvppokego.MainActivity
import com.lucianotoscano.pvppokego.PvPPokeGoApp
import com.lucianotoscano.pvppokego.capture.ScreenCapture
import com.lucianotoscano.pvppokego.data.BattleLeagueMode
import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.detect.BattleFrameEventDetector
import com.lucianotoscano.pvppokego.detect.BattleOcrDetector
import com.lucianotoscano.pvppokego.detect.BattlePresenceTracker
import com.lucianotoscano.pvppokego.engine.BattleEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.min

class BattleOverlayService : Service(), BattleOverlayView.Callbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var gameRepo: GameDataRepository
    private var engine: BattleEngine? = null
    private var detector: BattleOcrDetector? = null
    private var frameDetector: BattleFrameEventDetector? = null
    private val presenceTracker = BattlePresenceTracker()
    private var lastNativeTopCardY: Int? = null
    private var lastEditSnapshot: Triple<String, Float, Float>? = null
    private var projection: MediaProjection? = null
    private var capture: ScreenCapture? = null
    private var windowManager: WindowManager? = null
    private var overlayView: BattleOverlayView? = null
    private var scanJob: Job? = null
    private var currentLeagueCp: Int = 1500

    private var fastTouch: View? = null
    private var charged1Touch: View? = null
    private var charged2Touch: View? = null
    private var hudToggleTouch: View? = null
    private var hudLockTouch: View? = null
    private var editTouchLayer: View? = null
    private var editToolbarView: View? = null
    private var moveMenuView: View? = null
    private var quickMenu: QuickOverlayMenu? = null
    private var hudCollapsed = false
    private var appUiVisible = false

    private var battleActive = false
    private var lastBattleEvidenceAtMs = 0L
    private var switchChoicePromptUntilMs = 0L

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)
        settingsRepo.ensureTopHudSafeV4()
        // A previously-running flag at process/service creation means the prior session
        // did not shut down cleanly. Only in that case clear legacy edit mode so an
        // invisible fullscreen drag layer cannot revive unexpectedly. On a clean start,
        // preserve an edit-mode choice the user explicitly made in the settings screen.
        val recoveringStaleSession = settingsRepo.overlayServiceRunning
        if (recoveringStaleSession) settingsRepo.editMode = false
        // Free-drag via the global padlock is always re-armed safely on service creation.
        settingsRepo.layoutLocked = true
        settingsRepo.overlayServiceRunning = true
        gameRepo = GameDataRepository(this)
        startForeground(NOTIFICATION_ID, notification("Aguardando captura de tela"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_RESET -> {
                engine?.resetBattle()
                frameDetector?.reset()
                battleActive = false
                presenceTracker.reset()
                lastNativeTopCardY = null
                lastBattleEvidenceAtMs = 0L
                switchChoicePromptUntilMs = 0L
                dismissMoveMenu()
                redraw()
                return START_STICKY
            }
            ACTION_APP_FOREGROUND -> {
                appUiVisible = true
                dismissMoveMenu()
                redraw()
                return START_STICKY
            }
            ACTION_APP_BACKGROUND -> {
                appUiVisible = false
                redraw()
                return START_STICKY
            }
        }

        if (intent?.hasExtra(EXTRA_APP_VISIBLE) == true) {
            appUiVisible = intent.getBooleanExtra(EXTRA_APP_VISIBLE, false)
        }

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val code = intent?.getIntExtra(EXTRA_RESULT_CODE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val data = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (code != Int.MIN_VALUE && data != null && projection == null) startProjection(code, data)
        return START_STICKY
    }

    private fun startProjection(resultCode: Int, resultData: Intent) {
        scope.launch {
            try {
                currentLeagueCp = settingsRepo.configuredLeagueCp()
                    ?: settingsRepo.lastDetectedLeagueCp.takeIf { it in SUPPORTED_LEAGUE_CPS }
                    ?: 1500

                withContext(Dispatchers.IO) { gameRepo.load(currentLeagueCp) }
                engine = BattleEngine(gameRepo)
                detector = BattleOcrDetector(gameRepo)
                frameDetector = BattleFrameEventDetector()
                presenceTracker.reset()

                val pm = getSystemService(MediaProjectionManager::class.java)
                val mediaProjection = pm.getMediaProjection(resultCode, resultData)
                    ?: error("MediaProjection indisponível")
                mediaProjection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() { stopSelf() }
                }, null)
                projection = mediaProjection

                capture = ScreenCapture(this@BattleOverlayService, mediaProjection).also { it.start() }
                withContext(Dispatchers.Main) { attachOverlay() }
                startScanLoop()
                startForeground(NOTIFICATION_ID, notification("V05 FIX • ${leagueLabel(currentLeagueCp)} • HUD PvP ativo"))
            } catch (t: Throwable) {
                startForeground(NOTIFICATION_ID, notification("Erro: ${t.javaClass.simpleName}"))
                stopSelf()
            }
        }
    }

    private fun attachOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WindowManager::class.java)

        overlayView = BattleOverlayView(this, settingsRepo, gameRepo, this).also { view ->
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            windowManager?.addView(view, params)
        }

        attachMoveTouchZones()
        attachHudToggleTouch()
        attachHudLockTouch()
        quickMenu = QuickOverlayMenu(
            this,
            windowManager!!,
            settingsRepo,
            object : QuickOverlayMenu.Callbacks {
                override fun onSettingsChanged(rebuildTouchZones: Boolean) {
                    if (rebuildTouchZones) rebuildTouchableZones()
                    redraw()
                }

                override fun onToggleHud() {
                    toggleHudCollapsed()
                }

                override fun isHudCollapsed(): Boolean = hudCollapsed
                override fun isLayoutLocked(): Boolean = settingsRepo.layoutLocked

                override fun onResetHudPositions() {
                    settingsRepo.clearHudPositions()
                    rebuildTouchableZones()
                    redraw()
                }

                override fun currentBattleModeLabel(): String = currentBattleModeLabelText()
            }
        ).also { it.attach() }
        redraw()
    }

    private fun enemyMoveAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_ENEMY_MOVES,
        BattleOverlayView.ENEMY_MOVE_CENTER_X,
        BattleOverlayView.ENEMY_MOVE_FAST_Y
    )

    private fun hudToggleAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_HUD_TOGGLE,
        BattleOverlayView.HUD_TOGGLE_CENTER_X,
        BattleOverlayView.HUD_TOGGLE_CENTER_Y
    )

    private fun hudLockAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_HUD_LOCK,
        BattleOverlayView.HUD_LOCK_CENTER_X,
        BattleOverlayView.HUD_LOCK_CENTER_Y
    )

    private fun attachMoveTouchZones() {
        val wm = windowManager ?: return
        if (fastTouch != null) return

        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)

        fun zone(centerRefX: Float, centerRefY: Float, widthRef: Float, heightRef: Float, onTap: (View) -> Unit): View {
            val view = View(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = true
                setOnClickListener { onTap(this) }
            }
            val w = (widthRef * unit).toInt().coerceAtLeast(44)
            val h = (heightRef * unit).toInt().coerceAtLeast(44)
            val cx = centerRefX * sx
            val cy = centerRefY * sy
            val params = smallTouchableParams(w, h).apply {
                x = (cx - w / 2f).toInt()
                y = (cy - h / 2f).toInt()
            }
            wm.addView(view, params)
            return view
        }

        val (centerX, fastY) = enemyMoveAnchor()
        fastTouch = zone(centerX, fastY + 15f, 106f, 116f) { showMoveMenu(MoveKind.FAST) }
        charged1Touch = zone(centerX - 72f, fastY + 140f, 120f, 130f) { showMoveMenu(MoveKind.CHARGED_1) }
        charged2Touch = zone(centerX + 72f, fastY + 140f, 120f, 130f) { showMoveMenu(MoveKind.CHARGED_2) }
    }

    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        val params = hudControlParams(hudToggleAnchor())
        hudToggleTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_TOGGLE) { toggleHudCollapsed() }
        }.also { wm.addView(it, params) }
    }

    private fun attachHudLockTouch() {
        val wm = windowManager ?: return
        if (hudLockTouch != null) return
        val params = hudControlParams(hudLockAnchor())
        hudLockTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_LOCK) {
                settingsRepo.layoutLocked = !settingsRepo.layoutLocked
                // Global padlock is the single source of truth for free dragging.
                // Rebuild the drag layer on the next UI turn so unlocking immediately
                // makes every HUD block draggable without opening the legacy edit mode.
                if (settingsRepo.layoutLocked) {
                    overlayView?.setEditTarget(null)
                    removeEditTouchLayer()
                } else {
                    dismissMoveMenu()
                }
                hudLockTouch?.post { redraw() }
            }
        }.also { wm.addView(it, params) }
    }

    private fun hudControlParams(anchor: Pair<Float, Float>): WindowManager.LayoutParams {
        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)
        val size = (52f * unit).toInt().coerceAtLeast(44)
        val centerX = anchor.first * sx
        val centerY = anchor.second * sy
        return smallTouchableParams(size, size).apply {
            x = (centerX - size / 2f).toInt()
            y = (centerY - size / 2f).toInt()
        }
    }

    private fun installHudControlDrag(view: View, params: WindowManager.LayoutParams, block: String, onTap: () -> Unit) {
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragging = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; dragging = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (!settingsRepo.layoutLocked && abs(dx) + abs(dy) >= 6f) dragging = true
                    if (dragging) {
                        val maxX = (resources.displayMetrics.widthPixels - params.width).coerceAtLeast(0)
                        val maxY = (resources.displayMetrics.heightPixels - params.height).coerceAtLeast(0)
                        params.x = (startX + dx).toInt().coerceIn(0, maxX)
                        params.y = (startY + dy).toInt().coerceIn(0, maxY)
                        runCatching { windowManager?.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val sx = resources.displayMetrics.widthPixels / 864f
                        val sy = resources.displayMetrics.heightPixels / 1536f
                        settingsRepo.setPosition(block, (params.x + params.width/2f) / sx, (params.y + params.height/2f) / sy)
                        redraw()
                    } else onTap()
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
    }

    private fun toggleHudCollapsed() {
        hudCollapsed = !hudCollapsed
        dismissMoveMenu()
        overlayView?.setCollapsed(hudCollapsed)
        redraw()
    }

    private fun smallTouchableParams(width: Int, height: Int): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

    /** Full-screen touch layer is temporary while editing/free-drag is active. Lock again before battling. */
    private fun attachEditTouchLayer() {
        val wm = windowManager ?: return
        if (editTouchLayer != null) return

        var candidate: String? = null
        var downX = 0f
        var downY = 0f
        var dragging = false

        val layer = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        candidate = overlayView?.dragTargetAt(event.x, event.y)
                        overlayView?.setEditTarget(candidate)
                        candidate?.let { block ->
                            overlayView?.blockPosition(block)?.let { pos ->
                                lastEditSnapshot = Triple(block, pos.first, pos.second)
                            }
                        }
                        downX = event.x
                        downY = event.y
                        dragging = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val block = candidate
                        if (block != null) {
                            val moved = abs(event.x - downX) + abs(event.y - downY) >= 6f
                            if (moved) dragging = true
                            if (dragging) overlayView?.moveDragTarget(block, event.x, event.y)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val block = candidate
                        if (block != null) {
                            if (dragging) {
                                overlayView?.moveDragTarget(block, event.x, event.y)
                                rebuildTouchableZones()
                            } else {
                                overlayView?.setEditTarget(block)
                            }
                        }
                        candidate = null
                        dragging = false
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        candidate = null
                        dragging = false
                        overlayView?.setEditTarget(null)
                        true
                    }
                    else -> true
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }

        editTouchLayer = layer
        wm.addView(layer, params)
        if (settingsRepo.editMode) attachEditToolbar() else removeEditToolbar()
        bringHudControlsToFront()
        quickMenu?.bringToFront()
    }

    /** Compact edit controls. Select a HUD block, then resize/fade/lock/hide/undo or save. */
    private fun attachEditToolbar() {
        val wm = windowManager ?: return
        if (editToolbarView != null) return
        val d = resources.displayMetrics.density
        fun dp(v: Int): Int = (v * d).toInt()

        fun button(textValue: String, action: () -> Unit) = TextView(this).apply {
            text = textValue
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            background = roundedBackground(Color.argb(210, 24, 29, 37), Color.argb(210, 238, 241, 246), dp(8).toFloat(), dp(1))
            isClickable = true
            setOnClickListener { action() }
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(3), dp(3), dp(3))
            background = roundedBackground(Color.argb(170, 15, 18, 24), Color.argb(150, 255, 255, 255), dp(10).toFloat(), dp(1))
        }

        fun selected(): String? = overlayView?.selectedEditTarget()
        fun refresh() { redraw() }

        val actions = listOf<Pair<String, () -> Unit>>(
            "−" to { selected()?.let { settingsRepo.setBlockScale(it, settingsRepo.blockScale(it) - .10f); refresh() } },
            "+" to { selected()?.let { settingsRepo.setBlockScale(it, settingsRepo.blockScale(it) + .10f); refresh() } },
            "◐" to { selected()?.let { val o=settingsRepo.blockOpacity(it); settingsRepo.setBlockOpacity(it, if (o <= .36f) 1f else o - .25f); refresh() } },
            "L" to { selected()?.let { settingsRepo.setBlockLocked(it, !settingsRepo.isBlockLocked(it)); refresh() } },
            "H" to { selected()?.let { settingsRepo.setBlockHidden(it, !settingsRepo.isBlockHidden(it)); refresh() } },
            "↶" to { lastEditSnapshot?.let { (block,x,y) -> settingsRepo.setPosition(block,x,y); overlayView?.setEditTarget(block); rebuildTouchableZones(); refresh() } },
            "✓" to { settingsRepo.editMode=false; overlayView?.setEditTarget(null); rebuildTouchableZones(); redraw() },
            "↺" to { settingsRepo.resetHudCustomization(); overlayView?.setEditTarget(null); rebuildTouchableZones(); redraw() }
        )
        actions.forEach { (label, action) ->
            bar.addView(button(label, action), LinearLayout.LayoutParams(0, dp(34), 1f).apply { marginStart=dp(2); marginEnd=dp(2) })
        }

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val width = min((screenW * .92f).toInt(), dp(390))
        val height = dp(42)
        val params = smallTouchableParams(width, height).apply {
            x = (screenW - width) / 2
            y = (screenH - height - dp(62)).coerceAtLeast(0)
        }
        editToolbarView = bar
        wm.addView(bar, params)
    }

    private fun removeEditToolbar() {
        val bar = editToolbarView ?: return
        runCatching { windowManager?.removeView(bar) }
        editToolbarView = null
    }

    private fun removeEditTouchLayer() {
        val layer = editTouchLayer
        if (layer != null) runCatching { windowManager?.removeView(layer) }
        editTouchLayer = null
        removeEditToolbar()
        overlayView?.setEditTarget(null)
    }

    private fun syncEditTouchLayer(enabled: Boolean) {
        val shouldDrag = enabled && !hudCollapsed && (settingsRepo.editMode || !settingsRepo.layoutLocked)
        if (shouldDrag) {
            attachEditTouchLayer()
            if (settingsRepo.editMode) attachEditToolbar() else removeEditToolbar()
            bringHudControlsToFront()
            quickMenu?.bringToFront()
        } else removeEditTouchLayer()
    }

    private fun bringHudControlsToFront() {
        fun readd(view: View?) {
            if (view == null) return
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return
            // Reuse the exact LayoutParams captured by the drag listener; creating a new object
            // here would make the visible control move independently from its touch coordinates.
            runCatching { windowManager?.removeView(view) }
            runCatching { windowManager?.addView(view, params) }
        }
        readd(hudToggleTouch)
        if (!hudCollapsed) readd(hudLockTouch)
    }

    private fun rebuildTouchableZones() {
        runCatching { fastTouch?.let { windowManager?.removeView(it) } }
        runCatching { charged1Touch?.let { windowManager?.removeView(it) } }
        runCatching { charged2Touch?.let { windowManager?.removeView(it) } }
        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        runCatching { hudLockTouch?.let { windowManager?.removeView(it) } }
        fastTouch = null
        charged1Touch = null
        charged2Touch = null
        hudToggleTouch = null
        hudLockTouch = null
        attachMoveTouchZones()
        attachHudToggleTouch()
        attachHudLockTouch()
    }

    /** Compact type-colored submenu. Individual rows carry the color; there is no black panel behind them. */
    private fun showMoveMenu(kind: MoveKind) {
        // In manual HUD mode (automatic recognition disabled), battleActive has no
        // visual/OCR evidence by design; move selection must still remain usable.
        if (hudCollapsed || appUiVisible || (settingsRepo.autoRecognition && !battleActive) || settingsRepo.editMode) return
        val wm = windowManager ?: return
        val e = engine ?: return
        val options = when (kind) {
            MoveKind.FAST -> e.possibleFastMoves()
            MoveKind.CHARGED_1, MoveKind.CHARGED_2 -> e.possibleChargedMoves()
        }
        if (options.isEmpty()) return

        dismissMoveMenu()

        val metrics = resources.displayMetrics
        val density = metrics.density
        fun dp(v: Int): Int = (v * density).toInt()

        val ui = e.uiState(false)
        val selectedId = when (kind) {
            MoveKind.FAST -> ui.fastMove?.moveId
            MoveKind.CHARGED_1 -> ui.charged1?.move?.moveId
            MoveKind.CHARGED_2 -> ui.charged2?.move?.moveId
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setBackgroundColor(Color.TRANSPARENT)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = if (kind == MoveKind.FAST) "ATAQUE RÁPIDO" else "ATAQUE CARREGADO"
            setTextColor(Color.WHITE)
            setShadowLayer(4f, 0f, 1f, Color.BLACK)
            textSize = 12.5f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val close = TextView(this).apply {
            text = "✕"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setShadowLayer(4f, 0f, 1f, Color.BLACK)
            textSize = 16f
            setPadding(dp(8), 0, dp(2), 0)
            isClickable = true
            setOnClickListener { dismissMoveMenu() }
        }
        header.addView(title)
        header.addView(close)
        panel.addView(header)

        panel.addView(TextView(this).apply {
            text = "PvP • toque para trocar"
            setTextColor(Color.WHITE)
            setShadowLayer(4f, 0f, 1f, Color.BLACK)
            textSize = 8.5f
            setPadding(0, 0, 0, dp(4))
        })

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        addMoveMenuRow(
            parent = list,
            title = "AUTO • PvPoke/OCR",
            detail = "moveset provável + aprendizado OCR",
            baseColor = Color.rgb(74, 82, 96),
            selected = false
        ) {
            when (kind) {
                MoveKind.FAST -> e.clearFastManualOverride()
                MoveKind.CHARGED_1 -> e.clearChargedManualOverride(0)
                MoveKind.CHARGED_2 -> e.clearChargedManualOverride(1)
            }
            frameDetector?.reset()
            dismissMoveMenu()
            redraw()
        }

        options.forEachIndexed { index, move ->
            val detail = if (kind == MoveKind.FAST) {
                "${move.type.uppercase()} • ${move.power} DMG • +${move.energyGain}E • ${move.turns}T"
            } else {
                "${move.type.uppercase()} • ${move.power} DMG • ${move.chargedCost}E"
            }
            addMoveMenuRow(
                parent = list,
                title = "#${index + 1} ${move.name}",
                detail = detail,
                baseColor = moveTypeColor(move.type),
                selected = move.moveId == selectedId
            ) {
                when (kind) {
                    MoveKind.FAST -> e.selectFast(move)
                    MoveKind.CHARGED_1 -> e.selectCharged1(move)
                    MoveKind.CHARGED_2 -> e.selectCharged2(move)
                }
                frameDetector?.reset()
                dismissMoveMenu()
                redraw()
            }
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.TRANSPARENT)
            addView(list, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        panel.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val width = min((metrics.widthPixels * .70f).toInt(), dp(325))
        val estimatedRows = (options.size + 1).coerceAtMost(7)
        val desiredHeight = dp(48 + estimatedRows * 51)
        val height = min(desiredHeight, (metrics.heightPixels * .52f).toInt())
        val params = WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (metrics.widthPixels - width) / 2
            y = (metrics.heightPixels * .26f).toInt()
        }

        moveMenuView = panel
        wm.addView(panel, params)
    }

    private fun addMoveMenuRow(
        parent: LinearLayout,
        title: String,
        detail: String,
        baseColor: Int,
        selected: Boolean,
        onTap: () -> Unit
    ) {
        val d = resources.displayMetrics.density
        fun dp(v: Int): Int = (v * d).toInt()
        val rgb = darken(baseColor, .82f)
        val rowColor = withAlpha(rgb, 198)
        val border = if (selected) Color.argb(245, 255, 255, 255) else withAlpha(baseColor, 220)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background = roundedBackground(rowColor, border, 9f * d, if (selected) dp(2) else dp(1))
            isClickable = true
            setOnClickListener { onTap() }
        }
        row.addView(TextView(this).apply {
            text = (if (selected) "✓ " else "") + title
            setTextColor(Color.WHITE)
            textSize = 11.5f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 1
        })
        row.addView(TextView(this).apply {
            text = detail
            setTextColor(Color.argb(245, 248, 249, 252))
            textSize = 9f
            setPadding(0, dp(1), 0, 0)
            maxLines = 1
        })
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(4)
        })
    }

    private fun roundedBackground(color: Int, strokeColor: Int, radius: Float, strokeWidth: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color)
    )

    private fun darken(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).toInt().coerceIn(0, 255),
        (Color.green(color) * factor).toInt().coerceIn(0, 255),
        (Color.blue(color) * factor).toInt().coerceIn(0, 255)
    )

    private fun moveTypeColor(type: String): Int = when(type.lowercase()) {
        "fairy" -> Color.rgb(239,153,230); "psychic" -> Color.rgb(250,137,131)
        "fighting" -> Color.rgb(219,66,86); "rock" -> Color.rgb(206,193,141)
        "fire" -> Color.rgb(251,165,75); "steel" -> Color.rgb(85,151,164)
        "water" -> Color.rgb(91,166,224); "flying" -> Color.rgb(155,180,229)
        "ghost" -> Color.rgb(99,111,191); "grass" -> Color.rgb(93,190,101)
        "bug" -> Color.rgb(161,194,49); "ground" -> Color.rgb(214,133,85)
        "ice" -> Color.rgb(125,212,200); "dark" -> Color.rgb(113,121,140)
        "dragon" -> Color.rgb(7,115,200); "normal" -> Color.rgb(153,156,161)
        "electric" -> Color.rgb(243,219,85); "poison" -> Color.rgb(178,98,204)
        else -> Color.rgb(130,136,146)
    }

    private fun dismissMoveMenu() {
        val view = moveMenuView ?: return
        runCatching { windowManager?.removeView(view) }
        moveMenuView = null
    }

    private fun markBattleEvidence(now: Long) {
        lastBattleEvidenceAtMs = now
        battleActive = true
    }

    private fun normalizedBattleText(raw: String): String =
        Normalizer.normalize(raw.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private fun battleTextLooksActive(raw: String): Boolean {
        if (raw.isBlank()) return false
        val normalized = normalizedBattleText(raw)
        return BATTLE_TEXT_MARKERS.any(normalized::contains)
    }

    private fun switchChoicePromptDetected(raw: String): Boolean {
        if (raw.isBlank()) return false
        return normalizedBattleText(raw).contains("quer trocar por um pokemon")
    }

    private fun startScanLoop() {
        scanJob?.cancel()
        scanJob = scope.launch {
            var lastOcrAt = 0L
            var lastTeamOcrAt = 0L
            var lastLeagueOcrAt = 0L
            var consecutiveErrors = 0

            while (isActive) {
                val frame = capture?.acquireLatestBitmap()
                if (frame != null) {
                    try {
                        val now = System.currentTimeMillis()
                        val manualCp = settingsRepo.configuredLeagueCp()
                        if (manualCp != null && manualCp != currentLeagueCp && engine?.activeEnemyName() == null) reloadLeague(manualCp)

                        if (
                            settingsRepo.leagueMode == BattleLeagueMode.AUTO &&
                            engine?.activeEnemyName() == null &&
                            now - lastLeagueOcrAt >= LEAGUE_OCR_INTERVAL_MS
                        ) {
                            detector?.detectLeagueCp(frame)
                                ?.takeIf { it in SUPPORTED_LEAGUE_CPS }
                                ?.let { detectedCp ->
                                    settingsRepo.lastDetectedLeagueCp = detectedCp
                                    if (detectedCp != currentLeagueCp) reloadLeague(detectedCp)
                                }
                            lastLeagueOcrAt = now
                        }

                        var e = engine
                        if (settingsRepo.autoRecognition && e != null) {
                            val event = frameDetector?.analyze(
                                frame,
                                e.currentFastDurationMs(),
                                e.currentOwnFastDurationMs(),
                                now
                            )
                            if (event != null) {
                                lastNativeTopCardY = event.nativeTopCardY ?: lastNativeTopCardY
                                battleActive = presenceTracker.update(
                                    visualConfidence = event.battleConfidence,
                                    nowMs = now
                                )
                                e.onBattleConfidence(presenceTracker.confidence)
                                if (event.battleUiVisible) markBattleEvidence(now)
                                if (battleActive || event.battleUiVisible) {
                                    e.onReserveSlotsDetected(event.reserve1Visible, event.reserve2Visible)
                                }
                                if (event.fastMoveCount > 0 && (battleActive || event.battleUiVisible)) {
                                    e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                                }
                                if (event.ownFastMoveCount > 0 && (battleActive || event.battleUiVisible)) {
                                    e.onOwnFastDetected(event.ownFastMoveCount, event.confidence, event.source, event.opponentHpRatio)
                                }
                            }
                        }

                        if (settingsRepo.autoRecognition && now - lastOcrAt >= OCR_INTERVAL_MS) {
                            val d = detector?.detect(frame)
                            e = engine
                            if (d != null && e != null) {
                                if (settingsRepo.leagueMode == BattleLeagueMode.AUTO) {
                                    inferLeagueCpFromCards(d)?.let { inferredCp ->
                                        if (inferredCp != currentLeagueCp) {
                                            settingsRepo.lastDetectedLeagueCp = inferredCp
                                            reloadLeague(inferredCp)
                                            e = engine
                                        }
                                    }
                                }
                                val pairedCards = d.player != null && d.opponent != null
                                val textEvidence = battleTextLooksActive(d.battleText)
                                val switchPrompt = switchChoicePromptDetected(d.battleText)
                                if (pairedCards || textEvidence || switchPrompt) {
                                    battleActive = presenceTracker.update(
                                        pairedPokemonCards = pairedCards,
                                        battleTextEvidence = textEvidence,
                                        switchPrompt = switchPrompt,
                                        nowMs = now
                                    )
                                    e?.onBattleConfidence(presenceTracker.confidence)
                                    markBattleEvidence(now)
                                }
                                if (switchPrompt) {
                                    switchChoicePromptUntilMs = now + SWITCH_CHOICE_PROMPT_GRACE_MS
                                }
                                val changes = e?.onDetection(d)
                                if (changes?.playerChanged == true || changes?.enemyChanged == true) frameDetector?.reset()
                                if (changes?.playerChanged == true) switchChoicePromptUntilMs = 0L
                                val textEvent = e?.onBattleText(d.battleText, now)
                                if (textEvent != null) when {
                                    textEvent.newChargedPrompt -> frameDetector?.freezeForCharged(now)
                                    textEvent.chargedMoveConfirmed -> frameDetector?.freezeForCharged(now, 2_200L)
                                }
                            }
                            lastOcrAt = now
                        }

                        e = engine
                        if (
                            settingsRepo.autoRecognition && e != null && e.needsOwnTeamScan() &&
                            now - lastTeamOcrAt >= TEAM_OCR_INTERVAL_MS
                        ) {
                            detector?.detectOwnTeam(frame)?.let(e::onOwnTeamDetected)
                            lastTeamOcrAt = now
                        }

                        if (engine?.maybeConfirmPendingCharged(now) == true) {
                            markBattleEvidence(now)
                            frameDetector?.freezeForCharged(now, 1_800L)
                        }

                        battleActive = presenceTracker.tick(now)
                        engine?.onBattleConfidence(presenceTracker.confidence)
                        if (!battleActive) switchChoicePromptUntilMs = 0L

                        consecutiveErrors = 0
                        withContext(Dispatchers.Main) { redraw() }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (_: Throwable) {
                        consecutiveErrors++
                    } finally {
                        frame.recycle()
                    }
                }
                delay(if (consecutiveErrors == 0) FRAME_INTERVAL_MS else 650L)
            }
        }
    }

    /** Fallback when lobby OCR misses the league: live card CPs identify the cap reliably in normal GBL. */
    private fun inferLeagueCpFromCards(d: BattleDetection): Int? {
        val cps = listOfNotNull(d.player?.cp, d.opponent?.cp).filter { it > 0 }
        if (cps.isEmpty()) return null
        val maxCp = cps.maxOrNull() ?: return null
        return when {
            maxCp > 2500 -> 10000
            maxCp > 1500 -> 2500
            cps.size >= 2 -> 1500
            else -> null
        }
    }

    private suspend fun reloadLeague(cp: Int) {
        if (cp == currentLeagueCp) return
        withContext(Dispatchers.IO) { gameRepo.load(cp) }
        currentLeagueCp = cp
        engine = BattleEngine(gameRepo)
        frameDetector?.reset()
        presenceTracker.reset()
        lastNativeTopCardY = null
        switchChoicePromptUntilMs = 0L
        withContext(Dispatchers.Main) {
            dismissMoveMenu()
            startForeground(NOTIFICATION_ID, notification("V05 FIX • ${leagueLabel(cp)} • HUD PvP ativo"))
            redraw()
        }
    }

    private fun redraw() {
        val view = overlayView ?: return
        // With automatic recognition disabled, keep the HUD available as a manual
        // overlay instead of making every control disappear because battleActive can no
        // longer receive visual/OCR evidence.
        val enabled = settingsRepo.overlayEnabled && !appUiVisible && (battleActive || !settingsRepo.autoRecognition)
        view.visibility = if (enabled) View.VISIBLE else View.GONE
        view.setCollapsed(hudCollapsed)
        view.setNativeTopCardY(lastNativeTopCardY)
        view.setSwitchChoicePromptVisible(enabled && System.currentTimeMillis() <= switchChoicePromptUntilMs)

        val ui = engine?.uiState(settingsRepo.debugMode)
        ui?.let(view::render)

        syncEditTouchLayer(enabled)
        val selectorsEnabled = enabled && !hudCollapsed && settingsRepo.layoutLocked && !settingsRepo.editMode
        fastTouch?.visibility = if (selectorsEnabled && ui?.fastMove != null) View.VISIBLE else View.GONE
        charged1Touch?.visibility = if (selectorsEnabled && ui?.charged1 != null) View.VISIBLE else View.GONE
        charged2Touch?.visibility = if (selectorsEnabled && ui?.charged2 != null) View.VISIBLE else View.GONE
        // Eye is the single restore control when HUD is collapsed. Lock/menu/league all disappear.
        hudToggleTouch?.visibility = if (enabled) View.VISIBLE else View.GONE
        hudLockTouch?.visibility = if (enabled && !hudCollapsed) View.VISIBLE else View.GONE
        quickMenu?.setVisible(enabled && !hudCollapsed)
        if (!hudCollapsed) quickMenu?.refreshStatusBadge()
        if (!selectorsEnabled) dismissMoveMenu()
    }

    override fun onFastMoveTapped() { showMoveMenu(MoveKind.FAST) }
    override fun onCharged1Tapped() { showMoveMenu(MoveKind.CHARGED_1) }
    override fun onCharged2Tapped() { showMoveMenu(MoveKind.CHARGED_2) }
    override fun onResetTapped() {
        engine?.resetBattle()
        frameDetector?.reset()
        presenceTracker.reset()
        lastNativeTopCardY = null
        battleActive = false
        lastBattleEvidenceAtMs = 0L
        switchChoicePromptUntilMs = 0L
        dismissMoveMenu()
        redraw()
    }
    override fun onManualFastDelta(delta: Int) {
        engine?.manualFastDelta(delta)
        redraw()
    }
    override fun onMoveSelected(kind: BattleOverlayView.MoveKind, move: MoveDef) {
        when (kind) {
            BattleOverlayView.MoveKind.FAST -> engine?.selectFast(move)
            BattleOverlayView.MoveKind.CHARGED_1 -> engine?.selectCharged1(move)
            BattleOverlayView.MoveKind.CHARGED_2 -> engine?.selectCharged2(move)
        }
        frameDetector?.reset()
        dismissMoveMenu()
        redraw()
    }

    private fun currentBattleModeLabelText(): String {
        val league = when (settingsRepo.leagueMode) {
            BattleLeagueMode.AUTO -> when (currentLeagueCp) {
                2500 -> "AUTO-U"
                10000 -> "AUTO-M"
                else -> "AUTO-G"
            }
            BattleLeagueMode.GREAT -> "GRANDE"
            BattleLeagueMode.ULTRA -> "ULTRA"
            BattleLeagueMode.MASTER -> "MESTRA"
        }
        return if (settingsRepo.battleAssistEnabled) "$league • ASSIST" else league
    }

    private fun notification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, BattleOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, PvPPokeGoApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("PvPPokeGo")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "Parar", stop)
            .build()
    }

    override fun onDestroy() {
        scanJob?.cancel()
        dismissMoveMenu()
        quickMenu?.destroy()
        quickMenu = null
        removeEditTouchLayer()
        runCatching { overlayView?.let { windowManager?.removeView(it) } }
        runCatching { fastTouch?.let { windowManager?.removeView(it) } }
        runCatching { charged1Touch?.let { windowManager?.removeView(it) } }
        runCatching { charged2Touch?.let { windowManager?.removeView(it) } }
        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        runCatching { hudLockTouch?.let { windowManager?.removeView(it) } }
        overlayView = null
        fastTouch = null
        charged1Touch = null
        charged2Touch = null
        hudToggleTouch = null
        hudLockTouch = null
        detector?.close()
        detector = null
        frameDetector = null
        capture?.stop()
        capture = null
        projection?.stop()
        projection = null
        settingsRepo.overlayServiceRunning = false
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun leagueLabel(cp: Int): String = when (cp) {
        2500 -> "Liga Ultra"
        10000 -> "Liga Mestra"
        else -> "Liga Grande"
    }

    private enum class MoveKind { FAST, CHARGED_1, CHARGED_2 }

    companion object {
        const val EXTRA_RESULT_CODE = "media_projection_result_code"
        const val EXTRA_RESULT_DATA = "media_projection_result_data"
        const val EXTRA_APP_VISIBLE = "pvppokego_app_visible"
        const val ACTION_STOP = "com.lucianotoscano.pvppokego.STOP"
        const val ACTION_RESET = "com.lucianotoscano.pvppokego.RESET"
        const val ACTION_APP_FOREGROUND = "com.lucianotoscano.pvppokego.APP_FOREGROUND"
        const val ACTION_APP_BACKGROUND = "com.lucianotoscano.pvppokego.APP_BACKGROUND"
        private const val NOTIFICATION_ID = 1107
        private const val FRAME_INTERVAL_MS = 105L
        private const val OCR_INTERVAL_MS = 520L
        private const val TEAM_OCR_INTERVAL_MS = 1_650L
        private const val LEAGUE_OCR_INTERVAL_MS = 1_900L
        private const val BATTLE_VISIBILITY_GRACE_MS = 3_800L
        private const val SWITCH_CHOICE_PROMPT_GRACE_MS = 2_400L
        private const val EDIT_LONG_PRESS_MS = 180L
        private val SUPPORTED_LEAGUE_CPS = setOf(1500, 2500, 10000)
        private val BATTLE_TEXT_MARKERS = listOf(
            "usou ",
            "quer usar um escudo",
            "escudo protetor",
            "bloqueado",
            "supereficaz",
            "nao e muito eficaz",
            "quer trocar por um pokemon",
            "agora nao"
        )
    }
}
