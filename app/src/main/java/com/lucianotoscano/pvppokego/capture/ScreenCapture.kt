package com.lucianotoscano.pvppokego.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.view.WindowManager
import java.util.concurrent.atomic.AtomicReference

class ScreenCapture(
    private val context: Context,
    private val projection: MediaProjection
) {
    private val latest = AtomicReference<Bitmap?>(null)
    /** Guards copy/replacement so the capture thread never recycles a bitmap mid-copy. */
    private val bitmapLock = Any()
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var thread: HandlerThread? = null

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun start() {
        val wm = context.getSystemService(WindowManager::class.java)
        val bounds = wm.currentWindowMetrics.bounds
        width = bounds.width()
        height = bounds.height()
        val density = context.resources.displayMetrics.densityDpi

        thread = HandlerThread("pvppokego-capture").also { it.start() }
        val handler = Handler(thread!!.looper)
        reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        reader!!.setOnImageAvailableListener({ r ->
            val image = runCatching { r.acquireLatestImage() }.getOrNull() ?: return@setOnImageAvailableListener
            try {
                val plane = image.planes.firstOrNull() ?: return@setOnImageAvailableListener
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width
                val paddedWidth = width + rowPadding / pixelStride
                val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
                padded.copyPixelsFromBuffer(buffer)
                val cropped = Bitmap.createBitmap(padded, 0, 0, width, height)
                padded.recycle()

                val old = synchronized(bitmapLock) {
                    latest.getAndSet(cropped)
                }
                old?.recycle()
            } finally {
                image.close()
            }
        }, handler)

        display = projection.createVirtualDisplay(
            "PVPPokeGoCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface,
            null,
            handler
        )
    }

    /** Caller owns and must recycle the returned copy. */
    fun acquireLatestBitmap(): Bitmap? = synchronized(bitmapLock) {
        val src = latest.get() ?: return@synchronized null
        if (src.isRecycled) null else src.copy(Bitmap.Config.ARGB_8888, false)
    }

    fun stop() {
        val last = synchronized(bitmapLock) { latest.getAndSet(null) }
        last?.recycle()
        display?.release(); display = null
        reader?.close(); reader = null
        thread?.quitSafely(); thread = null
    }
}
