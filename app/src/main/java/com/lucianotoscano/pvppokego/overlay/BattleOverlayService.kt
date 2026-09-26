package com.lucianotoscano.pvppokego.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
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

    // The full visual HUD remains click-through. Only tiny transparent windows over the
    // three move selectors and the hide/restore button receive touches.
    private var fastTouch: View? = null
    private var charged1Touch: View? = null
    private var charged2Touch: View? = null
    private var hudToggleTouch: View? = null
    private var hudCollapsed = false

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(this)
        gameRepo = GameDataRepository(this)
        startForeground(NOTIFICATION_ID, notification("Aguardando captura de tela"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_RESET -> {
                engine?.resetBattle(); frameDetector?.reset(); redraw(); return START_STICKY
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf(); return START_NOT_STICKY
        }
        val code = intent?.getIntExtra(EXTRA_RESULT_CODE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val data = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra(EXTRA_RESULT_DATA)
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

        overlayView = BattleOverlayView(this, settingsRepo, this).also { view ->
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

    /** Only the three move selectors receive touch; the rest of the battle remains untouched. */
    private fun attachMoveTouchZones() {
        val wm = windowManager ?: return
        if (fastTouch != null) return

        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)

        fun zone(
            centerRefX: Float,
            centerRefY: Float,
            widthRef: Float,
            heightRef: Float,
            onTap: (View) -> Unit
        ): View {
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

        fastTouch = zone(620f, 370f, 106f, 116f) { showNativeMoveMenu(MoveKind.FAST, it) }
        charged1Touch = zone(548f, 495f, 120f, 130f) { showNativeMoveMenu(MoveKind.CHARGED_1, it) }
        charged2Touch = zone(692f, 495f, 120f, 130f) { showNativeMoveMenu(MoveKind.CHARGED_2, it) }
    }

    /** Red X while expanded; tiny PvPPokeGo restore bubble while collapsed. */
    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        hudToggleTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            setOnClickListener { toggleHudCollapsed() }
        }.also { view ->
            wm.addView(view, hudToggleParams())
        }
    }

    private fun hudToggleParams(): WindowManager.LayoutParams {
        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)
        val size = (54f * unit).toInt().coerceAtLeast(44)
        val centerX = (if (hudCollapsed) 42f else 96f) * sx
        val centerY = 38f * sy
        return smallTouchableParams(size, size).apply {
            x = (centerX - size / 2f).toInt()
            y = (centerY - size / 2f).toInt()
        }
    }

    private fun toggleHudCollapsed() {
        hudCollapsed = !hudCollapsed
        overlayView?.setCollapsed(hudCollapsed)
        hudToggleTouch?.let { view ->
            runCatching { windowManager?.updateViewLayout(view, hudToggleParams()) }
        }
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

    private fun showNativeMoveMenu(kind: MoveKind, anchor: View) {
        if (hudCollapsed) return
        val e = engine ?: return
        val options = when (kind) {
            MoveKind.FAST -> e.possibleFastMoves()
            MoveKind.CHARGED_1, MoveKind.CHARGED_2 -> e.possibleChargedMoves()
        }
        if (options.isEmpty()) return

        PopupMenu(this, anchor).apply {
            menu.add("Automático (PvPoke/OCR)").setOnMenuItemClickListener {
                when (kind) {
                    MoveKind.FAST -> e.clearFastManualOverride()
                    MoveKind.CHARGED_1 -> e.clearChargedManualOverride(0)
                    MoveKind.CHARGED_2 -> e.clearChargedManualOverride(1)
                }
                frameDetector?.reset()
                redraw()
                true
            }
            options.forEach { move ->
                val detail = if (kind == MoveKind.FAST) "+${move.energyGain}E" else "${move.chargedCost}E"
                menu.add("${move.name}  •  $detail").setOnMenuItemClickListener {
                    when (kind) {
                        MoveKind.FAST -> e.selectFast(move)
                        MoveKind.CHARGED_1 -> e.selectCharged1(move)
                        MoveKind.CHARGED_2 -> e.selectCharged2(move)
                    }
                    frameDetector?.reset()
                    redraw()
                    true
                }
            }
            show()
        }
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

                        // Manual league changes from Settings are picked up before battle starts.
                        val manualCp = settingsRepo.configuredLeagueCp()
                        if (manualCp != null && manualCp != currentLeagueCp && engine?.activeEnemyName() == null) {
                            reloadLeague(manualCp)
                        }

                        // Automatic mode reads Great/Ultra/Master from the lobby.
                        if (
                            settingsRepo.leagueMode == BattleLeagueMode.AUTO &&
                            engine?.activeEnemyName() == null &&
                            now - lastLeagueOcrAt >= LEAGUE_OCR_INTERVAL_MS
                        ) {
                            detector?.detectLeagueCp(frame)?.takeIf { it in SUPPORTED_LEAGUE_CPS }?.let { detectedCp ->
                                settingsRepo.lastDetectedLeagueCp = detectedCp
                                if (detectedCp != currentLeagueCp) reloadLeague(detectedCp)
                            }
                            lastLeagueOcrAt = now
                        }

                        var e = engine

                        // Cheap visual event detector runs much faster than OCR.
                        if (settingsRepo.autoRecognition && e != null) {
                            val event = frameDetector?.analyze(frame, e.currentFastDurationMs(), now)
                            if (event != null) {
                                e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                            }
                        }

                        // OCR: name/CP + charged prompt/banner text.
                        if (settingsRepo.autoRecognition && now - lastOcrAt >= OCR_INTERVAL_MS) {
                            val d = detector?.detect(frame)
                            e = engine
                            if (d != null && e != null) {
                                val changes = e.onDetection(d)
                                if (changes.playerChanged || changes.enemyChanged) frameDetector?.reset()
                                val textEvent = e.onBattleText(d.battleText, now)
                                if (textEvent.newChargedPrompt) frameDetector?.freezeForCharged(now)
                            }
                            lastOcrAt = now
                        }

                        // Remember the user's three Pokémon; stop the larger pass once all are known.
                        e = engine
                        if (settingsRepo.autoRecognition && e != null && e.needsOwnTeamScan() && now - lastTeamOcrAt >= TEAM_OCR_INTERVAL_MS) {
                            detector?.detectOwnTeam(frame)?.let(e::onOwnTeamDetected)
                            lastTeamOcrAt = now
                        }

                        // Exact "X usou Y" text wins; fallback only covers unreadable charge animations.
                        engine?.maybeConfirmPendingCharged(now)

                        consecutiveErrors = 0
                        withContext(Dispatchers.Main) { redraw() }
                    } catch (ce: CancellationException) { throw ce }
                    catch (_: Throwable) { consecutiveErrors++ }
                    finally { frame.recycle() }
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
            startForeground(NOTIFICATION_ID, notification("V04 • ${leagueLabel(cp)} • HUD PvP ativo"))
            redraw()
        }
    }

    private fun redraw() {
        val view = overlayView ?: return
        val enabled = settingsRepo.overlayEnabled
        view.visibility = if (enabled) View.VISIBLE else View.GONE
        view.setCollapsed(hudCollapsed)
        val ui = engine?.uiState(settingsRepo.debugMode)
        ui?.let(view::render)

        val selectorsEnabled = enabled && !hudCollapsed
        fastTouch?.visibility = if (selectorsEnabled && ui?.fastMove != null) View.VISIBLE else View.GONE
        charged1Touch?.visibility = if (selectorsEnabled && ui?.charged1 != null) View.VISIBLE else View.GONE
        charged2Touch?.visibility = if (selectorsEnabled && ui?.charged2 != null) View.VISIBLE else View.GONE
        hudToggleTouch?.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    // Retained callbacks keep the visual view compatible with the V03 interaction API.
    override fun onFastMoveTapped() {
        val options = engine?.possibleFastMoves().orEmpty()
        overlayView?.showMoveMenu(BattleOverlayView.MoveKind.FAST, options)
    }
    override fun onCharged1Tapped() {
        val options = engine?.possibleChargedMoves().orEmpty()
        overlayView?.showMoveMenu(BattleOverlayView.MoveKind.CHARGED_1, options)
    }
    override fun onCharged2Tapped() {
        val options = engine?.possibleChargedMoves().orEmpty()
        overlayView?.showMoveMenu(BattleOverlayView.MoveKind.CHARGED_2, options)
    }
    override fun onResetTapped() { engine?.resetBattle(); frameDetector?.reset(); redraw() }
    override fun onManualFastDelta(delta: Int) { engine?.manualFastDelta(delta); redraw() }
    override fun onMoveSelected(kind: BattleOverlayView.MoveKind, move: MoveDef) {
        when (kind) {
            BattleOverlayView.MoveKind.FAST -> engine?.selectFast(move)
            BattleOverlayView.MoveKind.CHARGED_1 -> engine?.selectCharged1(move)
            BattleOverlayView.MoveKind.CHARGED_2 -> engine?.selectCharged2(move)
        }
        frameDetector?.reset()
        redraw()
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
        runCatching { overlayView?.let { windowManager?.removeView(it) } }
        runCatching { fastTouch?.let { windowManager?.removeView(it) } }
        runCatching { charged1Touch?.let { windowManager?.removeView(it) } }
        runCatching { charged2Touch?.let { windowManager?.removeView(it) } }
        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        overlayView = null
        fastTouch = null
        charged1Touch = null
        charged2Touch = null
        hudToggleTouch = null
        detector?.close(); detector = null
        frameDetector = null
        capture?.stop(); capture = null
        projection?.stop(); projection = null
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
        const val ACTION_STOP = "com.lucianotoscano.pvppokego.STOP"
        const val ACTION_RESET = "com.lucianotoscano.pvppokego.RESET"
        private const val NOTIFICATION_ID = 1107
        private const val FRAME_INTERVAL_MS = 105L
        private const val OCR_INTERVAL_MS = 720L
        private const val TEAM_OCR_INTERVAL_MS = 1_650L
        private const val LEAGUE_OCR_INTERVAL_MS = 1_900L
        private val SUPPORTED_LEAGUE_CPS = setOf(1500, 2500, 10000)
    }
}
