package com.lucianotoscano.pvppokego.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.lucianotoscano.pvppokego.MainActivity
import com.lucianotoscano.pvppokego.PvPPokeGoApp
import com.lucianotoscano.pvppokego.capture.ScreenCapture
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
                withContext(Dispatchers.IO) { gameRepo.load(1500) }
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
                startForeground(NOTIFICATION_ID, notification("V03: contador visual ativo"))
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
        redraw()
    }

    private fun startScanLoop() {
        scanJob?.cancel()
        scanJob = scope.launch {
            var lastOcrAt = 0L
            var lastTeamOcrAt = 0L
            var consecutiveErrors = 0
            while (isActive) {
                val frame = capture?.acquireLatestBitmap()
                if (frame != null) {
                    try {
                        val now = System.currentTimeMillis()
                        val e = engine

                        // Cheap visual event detector runs much faster than OCR.
                        if (settingsRepo.autoRecognition && e != null) {
                            val event = frameDetector?.analyze(frame, e.currentFastDurationMs(), now)
                            if (event != null) {
                                e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                            }
                        }

                        // OCR remains throttled: name/CP + charged prompt only.
                        if (settingsRepo.autoRecognition && now - lastOcrAt >= OCR_INTERVAL_MS) {
                            val d = detector?.detect(frame)
                            if (d != null && e != null) {
                                val changes = e.onDetection(d)
                                if (changes.playerChanged || changes.enemyChanged) frameDetector?.reset()
                                val textEvent = e.onBattleText(d.battleText, now)
                                if (textEvent.newChargedPrompt) frameDetector?.freezeForCharged(now)
                            }
                            lastOcrAt = now
                        }

                        // A larger OCR pass remembers the user's three Pokémon. Once all three are
                        // known it stops, so it does not stay in the hot path for the whole battle.
                        if (settingsRepo.autoRecognition && e != null && e.needsOwnTeamScan() && now - lastTeamOcrAt >= TEAM_OCR_INTERVAL_MS) {
                            detector?.detectOwnTeam(frame)?.let(e::onOwnTeamDetected)
                            lastTeamOcrAt = now
                        }

                        // Charged prompt was seen but the exact move text may be hidden by a shield.
                        // Confirm the likely move after the charge animation and preserve residual energy.
                        e?.maybeConfirmPendingCharged(now)

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

    private fun redraw() {
        val view = overlayView ?: return
        view.visibility = if (settingsRepo.overlayEnabled) android.view.View.VISIBLE else android.view.View.GONE
        engine?.uiState(settingsRepo.debugMode)?.let(view::render)
    }

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
        overlayView = null
        detector?.close(); detector = null
        frameDetector = null
        capture?.stop(); capture = null
        projection?.stop(); projection = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_RESULT_CODE = "media_projection_result_code"
        const val EXTRA_RESULT_DATA = "media_projection_result_data"
        const val ACTION_STOP = "com.lucianotoscano.pvppokego.STOP"
        const val ACTION_RESET = "com.lucianotoscano.pvppokego.RESET"
        private const val NOTIFICATION_ID = 1107
        private const val FRAME_INTERVAL_MS = 105L
        private const val OCR_INTERVAL_MS = 720L
        private const val TEAM_OCR_INTERVAL_MS = 1_650L
    }
}
