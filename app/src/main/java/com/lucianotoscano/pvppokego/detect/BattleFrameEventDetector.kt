package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max

/**
 * Lightweight frame detector for enemy Fast Moves.
 *
 * Primary signal: discrete shrink of the player's HP fill bar. This is much safer than
 * treating frames/timers as attacks. If the HP bar temporarily cannot be found, a tightly
 * gated animation-motion peak is available as a low-confidence fallback.
 */
class BattleFrameEventDetector {
    data class Event(
        val fastMoveCount: Int = 0,
        val hpRatio: Float? = null,
        val motion: Float = 0f,
        val confidence: Float = 0f,
        val source: String = "none"
    )

    private var previousHpRun: Int? = null
    private var maximumHpRun: Int = 0
    private var previousMotionSamples: IntArray? = null
    private var motionEma = 0f
    private var motionWasHigh = false
    private var lastFastAtMs = 0L
    private var lastHpSeenAtMs = 0L
    private var frozenUntilMs = 0L

    fun reset() {
        previousHpRun = null
        maximumHpRun = 0
        previousMotionSamples = null
        motionEma = 0f
        motionWasHigh = false
        lastFastAtMs = 0L
        lastHpSeenAtMs = 0L
        frozenUntilMs = 0L
    }

    fun freezeForCharged(nowMs: Long = System.currentTimeMillis(), durationMs: Long = 6_200L) {
        frozenUntilMs = max(frozenUntilMs, nowMs + durationMs)
        // Force a fresh HP baseline after the charge sequence; charged damage must not be counted.
        previousHpRun = null
        previousMotionSamples = null
        motionWasHigh = false
    }

    fun analyze(frame: Bitmap, expectedFastDurationMs: Long, nowMs: Long = System.currentTimeMillis()): Event {
        if (frame.isRecycled) return Event()
        val hpRun = findPlayerHpFill(frame)
        val hpRatio = if (hpRun != null) {
            maximumHpRun = max(maximumHpRun, hpRun)
            if (maximumHpRun > 0) hpRun.toFloat() / maximumHpRun.toFloat() else null
        } else null
        val motion = motionScore(frame)

        if (nowMs < frozenUntilMs) {
            if (hpRun != null) {
                previousHpRun = hpRun
                lastHpSeenAtMs = nowMs
            }
            return Event(hpRatio = hpRatio, motion = motion, source = "frozen")
        }

        val minGap = (expectedFastDurationMs * 0.42f).toLong().coerceIn(230L, 1_450L)
        if (hpRun != null) {
            lastHpSeenAtMs = nowMs
            val old = previousHpRun
            previousHpRun = hpRun
            if (old != null) {
                // A switch/heal-like jump establishes a new baseline instead of producing an event.
                if (hpRun > old + max(7, maximumHpRun / 30)) {
                    maximumHpRun = max(maximumHpRun, hpRun)
                    return Event(hpRatio = hpRatio, motion = motion, confidence = .4f, source = "hp-reset")
                }
                val drop = old - hpRun
                val threshold = max(2, maximumHpRun / 95) // ~1% of a full bar, minimum 2 px.
                if (drop >= threshold && nowMs - lastFastAtMs >= minGap) {
                    lastFastAtMs = nowMs
                    return Event(1, hpRatio, motion, .96f, "hp")
                }
            }
        }

        // Fallback only when the HP fill has not been located for a while. Motion is edge-triggered
        // and cadence-gated so ordinary video frames do not become Fast Moves.
        val hpUnavailable = nowMs - lastHpSeenAtMs > 1_700L
        val high = motion > max(17f, motionEma * 1.72f)
        val rising = high && !motionWasHigh
        motionWasHigh = high
        if (hpUnavailable && rising && nowMs - lastFastAtMs >= (expectedFastDurationMs * .68f).toLong().coerceAtLeast(360L)) {
            lastFastAtMs = nowMs
            return Event(1, null, motion, .58f, "motion")
        }
        return Event(hpRatio = hpRatio, motion = motion, confidence = if (hpRun != null) .65f else .25f)
    }

    /** Longest saturated horizontal run in the player's HP-bar region. */
    private fun findPlayerHpFill(frame: Bitmap): Int? {
        val left = (frame.width * .035f).toInt()
        val right = (frame.width * .405f).toInt().coerceAtMost(frame.width)
        val top = (frame.height * .345f).toInt()
        val bottom = (frame.height * .445f).toInt().coerceAtMost(frame.height)
        if (right <= left || bottom <= top) return null

        val roiW = right - left
        val row = IntArray(roiW)
        var best = 0
        // Every second row is enough and cuts the CPU work roughly in half.
        var y = top
        while (y < bottom) {
            frame.getPixels(row, 0, roiW, left, y, roiW, 1)
            var run = 0
            for (p in row) {
                if (isHpColor(p)) {
                    run++
                    if (run > best) best = run
                } else {
                    run = 0
                }
            }
            y += 2
        }
        return best.takeIf { it >= (frame.width * .055f).toInt().coerceAtLeast(28) }
    }

    private fun isHpColor(p: Int): Boolean {
        val r = Color.red(p); val g = Color.green(p); val b = Color.blue(p)
        // Healthy cyan/green used by GO, plus yellow/orange/red low-HP states.
        val cyanGreen = g >= 135 && g >= r + 24 && b >= 70 && b <= 245
        val yellowOrange = r >= 155 && g >= 75 && b <= 135 && r >= b + 45
        val red = r >= 155 && g <= 125 && b <= 135 && r >= g + 35
        return cyanGreen || yellowOrange || red
    }

    private fun motionScore(frame: Bitmap): Float {
        val left = (frame.width * .50f).toInt()
        val right = (frame.width * .82f).toInt().coerceAtMost(frame.width)
        val top = (frame.height * .27f).toInt()
        val bottom = (frame.height * .58f).toInt().coerceAtMost(frame.height)
        val step = max(6, frame.width / 120)
        val cols = max(1, (right - left) / step)
        val rows = max(1, (bottom - top) / step)
        val samples = IntArray(cols * rows)
        var index = 0
        var y = top
        while (y < bottom && index < samples.size) {
            var x = left
            while (x < right && index < samples.size) {
                val p = frame.getPixel(x, y)
                samples[index++] = (Color.red(p) * 3 + Color.green(p) * 6 + Color.blue(p)) / 10
                x += step
            }
            y += step
        }
        val previous = previousMotionSamples
        previousMotionSamples = samples
        if (previous == null || previous.size != samples.size) return 0f
        var sum = 0L
        for (i in samples.indices) sum += abs(samples[i] - previous[i])
        val score = if (samples.isNotEmpty()) sum.toFloat() / samples.size.toFloat() else 0f
        motionEma = if (motionEma == 0f) score else motionEma * .88f + score * .12f
        return score
    }
}
