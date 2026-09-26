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
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.detect.BattleFrameEventDetector
import com.lucianotoscano.pvppokego.detect.BattleOcrDetector
import com.lucianotoscano.pvppokego.engine.BattleEngine
import kotlinx.coroutines.*

class BattleOverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settings: SettingsRepository
    private lateinit var gameRepo: GameDataRepository
    private var engine: BattleEngine? = null
    private var detector: BattleOcrDetector? = null
    private val frameDetector = BattleFrameEventDetector()
    private var projection: MediaProjection? = null
    private var capture: ScreenCapture? = null
    private var wm: WindowManager? = null
    private var overlay: BattleOverlayView? = null
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        gameRepo = GameDataRepository(this)
        startForeground(NOTIFICATION_ID, notification("Aguardando captura de tela"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_RESET -> {
                engine?.resetBattle()
                frameDetector.reset()
                redraw()
                return START_STICKY
            }
        }
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
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
                frameDetector.reset()
                val manager = getSystemService(MediaProjectionManager::class.java)
                projection = manager.getMediaProjection(resultCode, resultData).also { mp ->
                    mp.registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() { stopSelf() }
                    }, null)
                }
                capture = ScreenCapture(this@BattleOverlayService, projection!!).also { it.start() }
                withContext(Dispatchers.Main) { attachOverlay() }
                startScanLoop()
                startForeground(NOTIFICATION_ID, notification("Overlay ativo"))
            } catch (t: Throwable) {
                startForeground(NOTIFICATION_ID, notification("Erro: ${t.javaClass.simpleName}"))
                stopSelf()
            }
        }
    }

    private fun attachOverlay() {
        if (overlay != null) return
        wm = getSystemService(WindowManager::class.java)
        overlay = BattleOverlayView(this, settings).also { view ->
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
            wm?.addView(view, params)
        }
        redraw()
    }

    private fun startScanLoop() {
        scanJob?.cancel()
        scanJob = scope.launch {
            var lastOcrAt = 0L
            var errors = 0
            while (isActive) {
                val frame = capture?.acquireLatestBitmap()
                if (frame != null) {
                    try {
                        val e = engine
                        val event = frameDetector.analyze(frame, e?.currentFastDurationMs())
                        if (event.fastMoveHit) e?.onFastDetected()

                        val now = System.currentTimeMillis()
                        if (settings.autoRecognition && now - lastOcrAt >= OCR_INTERVAL_MS) {
                            val detection = detector?.detect(frame)
                            if (detection != null) {
                                e?.onDetection(detection)
                                if (detection.chargedIncoming) frameDetector.freezeForCharged(now)
                            }
                            lastOcrAt = now
                        }
                        errors = 0
                        withContext(Dispatchers.Main) { redraw() }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (_: Throwable) {
                        errors++
                    } finally {
                        frame.recycle()
                    }
                }
                delay(if (errors == 0) FRAME_INTERVAL_MS else 500L)
            }
        }
    }

    private fun redraw() {
        val view = overlay ?: return
        view.visibility = if (settings.overlayEnabled) android.view.View.VISIBLE else android.view.View.GONE
        engine?.uiState(settings.debugMode)?.let(view::render)
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
        runCatching { overlay?.let { wm?.removeView(it) } }
        overlay = null
        detector?.close(); detector = null
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
        private const val OCR_INTERVAL_MS = 700L
        private const val FRAME_INTERVAL_MS = 110L
    }
}
