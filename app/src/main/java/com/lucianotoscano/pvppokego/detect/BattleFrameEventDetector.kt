package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max

/**
 * Lightweight frame detector for enemy Fast Moves and native reserve-card visibility.
 *
 * Primary Fast signal: discrete shrink of the player's HP fill bar. Reserve visibility is
 * anchored to a narrow strip of the native HP bars. The strip deliberately avoids the
 * PvPPokeGo reserve badges themselves so our own overlay cannot keep a dead slot "alive".
 */
class BattleFrameEventDetector {
    data class Event(
        val fastMoveCount: Int = 0,
        val hpRatio: Float? = null,
        val motion: Float = 0f,
        val confidence: Float = 0f,
        val source: String = "none",
        val reserve1Visible: Boolean? = null,
        val reserve2Visible: Boolean? = null
    )

    private var previousHpRun: Int? = null
    private var maximumHpRun: Int = 0
    private var previousMotionSamples: IntArray? = null
    private var motionEma = 0f
    private var motionWasHigh = false
    private var lastFastAtMs = 0L
    private var lastHpSeenAtMs = 0L
    private var frozenUntilMs = 0L

    private val reserveVisibility = arrayOfNulls<Boolean>(2)
    private val reserveHitStreak = IntArray(2)
    private val reserveMissStreak = IntArray(2)

    fun reset() {
        previousHpRun = null
        maximumHpRun = 0
        previousMotionSamples = null
        motionEma = 0f
        motionWasHigh = false
        lastFastAtMs = 0L
        lastHpSeenAtMs = 0L
        frozenUntilMs = 0L
        reserveVisibility.fill(null)
        reserveHitStreak.fill(0)
        reserveMissStreak.fill(0)
    }

    fun freezeForCharged(nowMs: Long = System.currentTimeMillis(), durationMs: Long = 6_200L) {
        frozenUntilMs = max(frozenUntilMs, nowMs + durationMs)
        previousHpRun = null
        previousMotionSamples = null
        motionWasHigh = false
    }

    fun analyze(frame: Bitmap, expectedFastDurationMs: Long, nowMs: Long = System.currentTimeMillis()): Event {
        if (frame.isRecycled) return Event()

        val reserve1 = updateReserveVisibility(0, hasNativeReserveHpBar(frame, 0))
        val reserve2 = updateReserveVisibility(1, hasNativeReserveHpBar(frame, 1))

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
            return Event(
                hpRatio = hpRatio,
                motion = motion,
                source = "frozen",
                reserve1Visible = reserve1,
                reserve2Visible = reserve2
            )
        }

        val minGap = (expectedFastDurationMs * 0.42f).toLong().coerceIn(230L, 1_450L)
        if (hpRun != null) {
            lastHpSeenAtMs = nowMs
            val old = previousHpRun
            previousHpRun = hpRun
            if (old != null) {
                if (hpRun > old + max(7, maximumHpRun / 30)) {
                    maximumHpRun = max(maximumHpRun, hpRun)
                    return Event(
                        hpRatio = hpRatio,
                        motion = motion,
                        confidence = .4f,
                        source = "hp-reset",
                        reserve1Visible = reserve1,
                        reserve2Visible = reserve2
                    )
                }
                val drop = old - hpRun
                val threshold = max(2, maximumHpRun / 95)
                if (drop >= threshold && nowMs - lastFastAtMs >= minGap) {
                    lastFastAtMs = nowMs
                    return Event(
                        fastMoveCount = 1,
                        hpRatio = hpRatio,
                        motion = motion,
                        confidence = .96f,
                        source = "hp",
                        reserve1Visible = reserve1,
                        reserve2Visible = reserve2
                    )
                }
            }
        }

        val hpUnavailable = nowMs - lastHpSeenAtMs > 1_700L
        val high = motion > max(17f, motionEma * 1.72f)
        val rising = high && !motionWasHigh
        motionWasHigh = high
        if (hpUnavailable && rising && nowMs - lastFastAtMs >= (expectedFastDurationMs * .68f).toLong().coerceAtLeast(360L)) {
            lastFastAtMs = nowMs
            return Event(
                fastMoveCount = 1,
                hpRatio = null,
                motion = motion,
                confidence = .58f,
                source = "motion",
                reserve1Visible = reserve1,
                reserve2Visible = reserve2
            )
        }

        return Event(
            hpRatio = hpRatio,
            motion = motion,
            confidence = if (hpRun != null) .65f else .25f,
            reserve1Visible = reserve1,
            reserve2Visible = reserve2
        )
    }

    private fun updateReserveVisibility(index: Int, detected: Boolean): Boolean? {
        if (detected) {
            reserveHitStreak[index]++
            reserveMissStreak[index] = 0
            if (reserveHitStreak[index] >= 2) reserveVisibility[index] = true
        } else {
            reserveMissStreak[index]++
            reserveHitStreak[index] = 0
            // ~0.4 s at the normal frame cadence: fast enough to remove a fainted card,
            // but long enough to ignore a couple of battle flashes/animation frames.
            if (reserveMissStreak[index] >= 4) reserveVisibility[index] = false
        }
        return reserveVisibility[index]
    }

    /**
     * Detect only the thin native HP line between our matchup badge and our type chips.
     * The previous broad ROI overlapped our own colored circles and could self-detect forever.
     */
    private fun hasNativeReserveHpBar(frame: Bitmap, slot: Int): Boolean {
        val left = (frame.width * .82f).toInt().coerceAtLeast(0)
        val right = (frame.width * .94f).toInt().coerceAtMost(frame.width)
        val topFrac = if (slot == 0) .604f else .700f
        val bottomFrac = if (slot == 0) .617f else .713f
        val top = (frame.height * topFrac).toInt().coerceAtLeast(0)
        val bottom = (frame.height * bottomFrac).toInt().coerceAtMost(frame.height)
        if (right <= left || bottom <= top) return false

        val roiW = right - left
        val row = IntArray(roiW)
        var best = 0
        var y = top
        while (y < bottom) {
            frame.getPixels(row, 0, roiW, left, y, roiW, 1)
            var run = 0
            for (p in row) {
                if (isReserveHpColor(p)) {
                    run++
                    if (run > best) best = run
                } else {
                    run = 0
                }
            }
            y++
        }
        return best >= (frame.width * .034f).toInt().coerceAtLeast(24)
    }

    private fun isReserveHpColor(p: Int): Boolean {
        val r = Color.red(p); val g = Color.green(p); val b = Color.blue(p)
        val cyanGreen = g >= 145 && g >= r + 28 && b >= 65 && b <= 245
        val yellowOrange = r >= 160 && g >= 80 && b <= 135 && r >= b + 45
        val red = r >= 165 && g <= 125 && b <= 135 && r >= g + 35
        return cyanGreen || yellowOrange || red
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
