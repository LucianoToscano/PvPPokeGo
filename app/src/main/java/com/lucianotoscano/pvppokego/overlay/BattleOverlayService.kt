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
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.detect.BattleFrameEventDetector
import com.lucianotoscano.pvppokego.detect.BattleOcrDetector
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
import kotlin.math.min

class BattleOverlayService : Service(), BattleOverlayView.Callbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var gameRepo: GameDataRepository
    private var engine: BattleEngine? = null
    private var detector: BattleOcrDetector? = null
    private var frameDetector: BattleFrameEventDetector? = null
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
    private var moveMenuView: View? = null
    private var hudCollapsed = false
    private var appUiVisible = false

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)
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

                val pm = getSystemService(MediaProjectionManager::class.java)
                projection = pm.getMediaProjection(resultCode, resultData).also { mp ->
                    mp.registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() { stopSelf() }
                    }, null)
                }

                capture = ScreenCapture(this@BattleOverlayService, projection!!).also { it.start() }
                withContext(Dispatchers.Main) { attachOverlay() }
                startScanLoop()
                startForeground(NOTIFICATION_ID, notification("V04 • ${leagueLabel(currentLeagueCp)} • HUD PvP ativo"))
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
        redraw()
    }

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

        val centerX = BattleOverlayView.ENEMY_MOVE_CENTER_X
        fastTouch = zone(centerX, 370f, 106f, 116f) { showMoveMenu(MoveKind.FAST) }
        charged1Touch = zone(centerX - 72f, 495f, 120f, 130f) { showMoveMenu(MoveKind.CHARGED_1) }
        charged2Touch = zone(centerX + 72f, 495f, 120f, 130f) { showMoveMenu(MoveKind.CHARGED_2) }
    }

    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        hudToggleTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            setOnClickListener { toggleHudCollapsed() }
        }.also { wm.addView(it, hudToggleParams()) }
    }

    private fun hudToggleParams(): WindowManager.LayoutParams {
        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)
        val size = (52f * unit).toInt().coerceAtLeast(44)
        val centerX = BattleOverlayView.HUD_TOGGLE_CENTER_X * sx
        val centerY = BattleOverlayView.HUD_TOGGLE_CENTER_Y * sy
        return smallTouchableParams(size, size).apply {
            x = (centerX - size / 2f).toInt()
            y = (centerY - size / 2f).toInt()
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

    /**
     * Poke-Genie-like move picker: colored type rows, rank order and the PvP values that matter.
     * It is our own overlay UI and does not send any input to Pokemon GO.
     */
    private fun showMoveMenu(kind: MoveKind) {
        if (hudCollapsed || appUiVisible) return
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
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = roundedBackground(Color.argb(246, 20, 24, 31), Color.argb(220, 225, 230, 238), 18f*density, dp(1))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = when (kind) {
                MoveKind.FAST -> "SELECIONAR ATAQUE RÁPIDO"
                else -> "SELECIONAR ATAQUE CARREGADO"
            }
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val close = TextView(this).apply {
            text = "✕"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(dp(12), dp(4), dp(6), dp(4))
            isClickable = true
            setOnClickListener { dismissMoveMenu() }
        }
        header.addView(title); header.addView(close)
        panel.addView(header)

        panel.addView(TextView(this).apply {
            text = "Cores por tipo • ordem pelo ranking PvP • toque para trocar"
            setTextColor(Color.rgb(178, 185, 196))
            textSize = 10.5f
            setPadding(0, dp(2), 0, dp(8))
        })

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        addMoveMenuRow(
            parent = list,
            title = "AUTOMÁTICO  •  PvPoke/OCR",
            detail = "Usa o moveset mais provável e aprende o golpe observado.",
            baseColor = Color.rgb(74, 82, 96),
            selected = false
        ) {
            when (kind) {
                MoveKind.FAST -> e.clearFastManualOverride()
                MoveKind.CHARGED_1 -> e.clearChargedManualOverride(0)
                MoveKind.CHARGED_2 -> e.clearChargedManualOverride(1)
            }
            frameDetector?.reset(); dismissMoveMenu(); redraw()
        }

        options.forEachIndexed { index, move ->
            val detail = if (kind == MoveKind.FAST) {
                val turns = move.turns
                "${move.type.uppercase()}  •  Poder ${move.power}  •  +${move.energyGain} energia  •  $turns turno${if (turns == 1) "" else "s"}"
            } else {
                "${move.type.uppercase()}  •  Poder ${move.power}  •  Custo ${move.chargedCost} energia"
            }
            addMoveMenuRow(
                parent = list,
                title = "TOP #${index + 1}   ${move.name}",
                detail = detail,
                baseColor = moveTypeColor(move.type),
                selected = move.moveId == selectedId
            ) {
                when (kind) {
                    MoveKind.FAST -> e.selectFast(move)
                    MoveKind.CHARGED_1 -> e.selectCharged1(move)
                    MoveKind.CHARGED_2 -> e.selectCharged2(move)
                }
                frameDetector?.reset(); dismissMoveMenu(); redraw()
            }
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(list, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        panel.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val width = min((metrics.widthPixels * .92f).toInt(), dp(430))
        val estimatedRows = (options.size + 1).coerceAtMost(7)
        val desiredHeight = dp(74 + estimatedRows * 70)
        val height = min(desiredHeight, (metrics.heightPixels * .68f).toInt())
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
            y = (metrics.heightPixels * .15f).toInt()
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
        fun dp(v: Int): Int = (v*d).toInt()
        val rowColor = darken(baseColor, .54f)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = roundedBackground(rowColor, if (selected) Color.WHITE else baseColor, 13f*d, if (selected) dp(2) else dp(1))
            isClickable = true
            setOnClickListener { onTap() }
        }
        row.addView(TextView(this).apply {
            text = (if (selected) "✓  " else "") + title
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 1
        })
        row.addView(TextView(this).apply {
            text = detail
            setTextColor(Color.rgb(234, 237, 242))
            textSize = 10.5f
            setPadding(0, dp(3), 0, 0)
            maxLines = 2
        })
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(7)
        })
    }

    private fun roundedBackground(color: Int, strokeColor: Int, radius: Float, strokeWidth: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }

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
                            val event = frameDetector?.analyze(frame, e.currentFastDurationMs(), now)
                            if (event != null) {
                                e.onReserveSlotsDetected(event.reserve1Visible, event.reserve2Visible)
                                e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                            }
                        }

                        if (settingsRepo.autoRecognition && now - lastOcrAt >= OCR_INTERVAL_MS) {
                            val d = detector?.detect(frame)
                            e = engine
                            if (d != null && e != null) {
                                val changes = e.onDetection(d)
                                if (changes.playerChanged || changes.enemyChanged) frameDetector?.reset()
                                val textEvent = e.onBattleText(d.battleText, now)
                                when {
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
                            frameDetector?.freezeForCharged(now, 1_800L)
                        }

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

    private suspend fun reloadLeague(cp: Int) {
        if (cp == currentLeagueCp) return
        withContext(Dispatchers.IO) { gameRepo.load(cp) }
        currentLeagueCp = cp
        engine = BattleEngine(gameRepo)
        frameDetector?.reset()
        withContext(Dispatchers.Main) {
            dismissMoveMenu()
            startForeground(NOTIFICATION_ID, notification("V04 • ${leagueLabel(cp)} • HUD PvP ativo"))
            redraw()
        }
    }

    private fun redraw() {
        val view = overlayView ?: return
        val enabled = settingsRepo.overlayEnabled && !appUiVisible
        view.visibility = if (enabled) View.VISIBLE else View.GONE
        view.setCollapsed(hudCollapsed)

        val ui = engine?.uiState(settingsRepo.debugMode)
        ui?.let(view::render)

        val selectorsEnabled = enabled && !hudCollapsed
        fastTouch?.visibility = if (selectorsEnabled && ui?.fastMove != null) View.VISIBLE else View.GONE
        charged1Touch?.visibility = if (selectorsEnabled && ui?.charged1 != null) View.VISIBLE else View.GONE
        charged2Touch?.visibility = if (selectorsEnabled && ui?.charged2 != null) View.VISIBLE else View.GONE
        hudToggleTouch?.visibility = if (enabled) View.VISIBLE else View.GONE
        if (!selectorsEnabled) dismissMoveMenu()
    }

    override fun onFastMoveTapped() { showMoveMenu(MoveKind.FAST) }
    override fun onCharged1Tapped() { showMoveMenu(MoveKind.CHARGED_1) }
    override fun onCharged2Tapped() { showMoveMenu(MoveKind.CHARGED_2) }
    override fun onResetTapped() {
        engine?.resetBattle(); frameDetector?.reset(); dismissMoveMenu(); redraw()
    }
    override fun onManualFastDelta(delta: Int) {
        engine?.manualFastDelta(delta); redraw()
    }
    override fun onMoveSelected(kind: BattleOverlayView.MoveKind, move: MoveDef) {
        when (kind) {
            BattleOverlayView.MoveKind.FAST -> engine?.selectFast(move)
            BattleOverlayView.MoveKind.CHARGED_1 -> engine?.selectCharged1(move)
            BattleOverlayView.MoveKind.CHARGED_2 -> engine?.selectCharged2(move)
        }
        frameDetector?.reset(); dismissMoveMenu(); redraw()
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
        runCatching { overlayView?.let { windowManager?.removeView(it) } }
        runCatching { fastTouch?.let { windowManager?.removeView(it) } }
        runCatching { charged1Touch?.let { windowManager?.removeView(it) } }
        runCatching { charged2Touch?.let { windowManager?.removeView(it) } }
        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        overlayView = null; fastTouch = null; charged1Touch = null; charged2Touch = null; hudToggleTouch = null
        detector?.close(); detector = null; frameDetector = null
        capture?.stop(); capture = null; projection?.stop(); projection = null
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
        private val SUPPORTED_LEAGUE_CPS = setOf(1500, 2500, 10000)
    }
}
