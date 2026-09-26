package com.lucianotoscano.pvppokego

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs

class ScreenCaptureAnalyzer(
    context: Context,
    private val projection: MediaProjection,
    private val callback: Callback
) {
    interface Callback {
        fun onOcr(fullText: String, enemyHeaderText: String, timestampMs: Long)
        fun onMotion(score: Double, timestampMs: Long)
    }

    private val thread = HandlerThread("PvPPokeGoCapture").apply { start() }
    private val handler = Handler(thread.looper)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val metrics = DisplayMetrics().also {
        @Suppress("DEPRECATION")
        context.getSystemService(WindowManager::class.java).defaultDisplay.getRealMetrics(it)
    }
    private val reader = ImageReader.newInstance(
        metrics.widthPixels,
        metrics.heightPixels,
        PixelFormat.RGBA_8888,
        2
    )

    private var virtualDisplay: VirtualDisplay? = null
    private var lastFrameAt = 0L
    private var lastOcrAt = 0L
    private var ocrBusy = false
    private var previousMotion: IntArray? = null

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() = stopInternal()
    }

    fun start() {
        projection.registerCallback(projectionCallback, handler)
        virtualDisplay = projection.createVirtualDisplay(
            "PvPPokeGoCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            handler
        )

        reader.setOnImageAvailableListener({ source ->
            val now = SystemClock.elapsedRealtime()
            if (now - lastFrameAt < FRAME_INTERVAL_MS) {
                source.acquireLatestImage()?.close()
                return@setOnImageAvailableListener
            }
            lastFrameAt = now
            val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
            val bitmap = try {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * image.width
                val padded = Bitmap.createBitmap(
                    image.width + rowPadding / pixelStride,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
                padded.copyPixelsFromBuffer(buffer)
                val crop = Bitmap.createBitmap(padded, 0, 0, image.width, image.height)
                if (crop !== padded) padded.recycle()
                crop
            } finally {
                image.close()
            }

            analyzeMotion(bitmap, now)
            if (!ocrBusy && now - lastOcrAt >= OCR_INTERVAL_MS) {
                lastOcrAt = now
                analyzeOcr(bitmap.copy(Bitmap.Config.ARGB_8888, false), now)
            }
            bitmap.recycle()
        }, handler)
    }

    private fun analyzeOcr(bitmap: Bitmap, timestampMs: Long) {
        ocrBusy = true
        val input = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(input)
            .addOnSuccessListener { result ->
                val enemyHeader = result.textBlocks
                    .filter { block ->
                        val box = block.boundingBox ?: return@filter false
                        box.centerX() > bitmap.width * 0.48 && box.top < bitmap.height * 0.18
                    }
                    .joinToString(" ") { it.text }
                callback.onOcr(result.text, enemyHeader, timestampMs)
            }
            .addOnCompleteListener {
                ocrBusy = false
                bitmap.recycle()
            }
    }

    /**
     * Samples the opponent battle area. OverlayService turns clean motion pulses
     * into fast-move events using the selected move cooldown as a refractory
     * window; this avoids blindly adding energy every captured frame.
     */
    private fun analyzeMotion(bitmap: Bitmap, timestampMs: Long) {
        val sample = IntArray(MOTION_GRID * MOTION_GRID)
        val left = (bitmap.width * 0.42f).toInt()
        val top = (bitmap.height * 0.24f).toInt()
        val right = (bitmap.width * 0.94f).toInt()
        val bottom = (bitmap.height * 0.66f).toInt()
        var index = 0
        for (gy in 0 until MOTION_GRID) {
            val y = top + ((bottom - top - 1) * gy / (MOTION_GRID - 1))
            for (gx in 0 until MOTION_GRID) {
                val x = left + ((right - left - 1) * gx / (MOTION_GRID - 1))
                val c = bitmap.getPixel(x, y)
                sample[index++] = (Color.red(c) * 30 + Color.green(c) * 59 + Color.blue(c) * 11) / 100
            }
        }

        previousMotion?.let { previous ->
            var total = 0.0
            for (i in sample.indices) total += abs(sample[i] - previous[i])
            callback.onMotion(total / sample.size, timestampMs)
        }
        previousMotion = sample
    }

    fun stop() {
        try { projection.unregisterCallback(projectionCallback) } catch (_: Exception) {}
        stopInternal()
        try { projection.stop() } catch (_: Exception) {}
    }

    private fun stopInternal() {
        reader.setOnImageAvailableListener(null, null)
        virtualDisplay?.release()
        virtualDisplay = null
        try { reader.close() } catch (_: Exception) {}
        try { recognizer.close() } catch (_: Exception) {}
        thread.quitSafely()
    }

    private companion object {
        const val FRAME_INTERVAL_MS = 220L
        const val OCR_INTERVAL_MS = 650L
        const val MOTION_GRID = 12
    }
}
