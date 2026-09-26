package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/**
 * Lightweight detector that watches the player's HP bar instead of OCR'ing every frame.
 * A small HP reduction is treated as one enemy Fast Move. Large reductions are ignored here
 * because they are normally Charged Moves and are handled by the charged-attack OCR state.
 */
class BattleFrameEventDetector {
    data class Event(val fastMoveHit: Boolean = false, val hpRatio: Float? = null)

    private var lastStableRatio: Float? = null
    private var lastFastAtMs: Long = 0L
    private var freezeUntilMs: Long = 0L

    fun reset() {
        lastStableRatio = null
        lastFastAtMs = 0L
        freezeUntilMs = 0L
    }

    fun freezeForCharged(nowMs: Long = System.currentTimeMillis(), durationMs: Long = 4_500L) {
        freezeUntilMs = maxOf(freezeUntilMs, nowMs + durationMs)
    }

    fun analyze(frame: Bitmap, expectedFastDurationMs: Long?): Event {
        if (frame.isRecycled) return Event()
        val ratio = findPlayerHpFill(frame) ?: return Event()
        val now = System.currentTimeMillis()
        val previous = lastStableRatio
        if (previous == null) {
            lastStableRatio = ratio
            return Event(hpRatio = ratio)
        }

        // Switching to a healthier Pokemon makes the bar jump up. Rebase without counting.
        if (ratio > previous + 0.035f) {
            lastStableRatio = ratio
            return Event(hpRatio = ratio)
        }

        val drop = previous - ratio
        if (drop <= 0f) {
            if (abs(drop) < 0.01f) lastStableRatio = ratio
            return Event(hpRatio = ratio)
        }

        // HP animations can span several frames. Debounce using the selected Fast Move duration.
        val debounce = ((expectedFastDurationMs ?: 500L) * 0.62f).toLong().coerceIn(260L, 1_500L)
        val smallEnoughForFast = drop in 0.0025f..0.115f
        val canCount = now >= freezeUntilMs && now - lastFastAtMs >= debounce

        if (smallEnoughForFast) {
            // HP animations can arrive in several tiny frame-to-frame steps. Always move the baseline
            // forward so one visual hit can never be counted twice after the debounce expires.
            lastStableRatio = ratio
            if (canCount) {
                lastFastAtMs = now
                return Event(fastMoveHit = true, hpRatio = ratio)
            }
            return Event(hpRatio = ratio)
        }

        // Large HP loss or a transition: rebase, but never count it as a Fast Move.
        if (drop > 0.115f || now < freezeUntilMs) lastStableRatio = ratio
        return Event(hpRatio = ratio)
    }

    private fun findPlayerHpFill(frame: Bitmap): Float? {
        val w = frame.width
        val h = frame.height
        if (w < 50 || h < 100) return null

        val x0 = (w * 0.035f).toInt()
        val x1 = (w * 0.49f).toInt()
        val y0 = (h * 0.245f).toInt()
        val y1 = (h * 0.50f).toInt()
        val stepY = maxOf(1, (y1 - y0) / 95)
        val stepX = 2

        var bestRun = 0
        for (y in y0 until y1 step stepY) {
            var run = 0
            var rowBest = 0
            var x = x0
            while (x < x1) {
                val c = frame.getPixel(x, y)
                if (isHpColor(c)) {
                    run += stepX
                    if (run > rowBest) rowBest = run
                } else {
                    run = 0
                }
                x += stepX
            }
            if (rowBest > bestRun) bestRun = rowBest
        }

        // Ignore accidental green/blue scenery unless it looks like a real horizontal HP segment.
        if (bestRun < w * 0.055f) return null
        return (bestRun.toFloat() / w.toFloat()).coerceIn(0f, 1f)
    }

    private fun isHpColor(c: Int): Boolean {
        val r = Color.red(c)
        val g = Color.green(c)
        val b = Color.blue(c)
        val bright = maxOf(r, g, b) >= 120
        val cyanGreen = g >= r + 20 && (b >= r + 8 || g >= 175)
        val yellow = r >= 150 && g >= 115 && b <= 105
        val red = r >= 155 && g <= 120 && b <= 110
        return bright && (cyanGreen || yellow || red)
    }
}
