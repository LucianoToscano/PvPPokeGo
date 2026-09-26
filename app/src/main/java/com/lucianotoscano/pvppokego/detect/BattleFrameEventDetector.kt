package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max

/**
 * Lightweight frame detector for enemy Fast Moves and native reserve-card visibility.
 *
 * Enemy Fast Move energy is now advanced only from a discrete loss on the PLAYER HP bar.
 * Visual motion on the enemy side is kept only as diagnostic data because the player's own
 * attack effects also animate there and previously produced false enemy-energy increments.
 */
class BattleFrameEventDetector {
    data class Event(
        val fastMoveCount: Int = 0,
        val hpRatio: Float? = null,
        val motion: Float = 0f,
        val confidence: Float = 0f,
        val source: String = "none",
        val reserve1Visible: Boolean? = null,
        val reserve2Visible: Boolean? = null,
        val battleUiVisible: Boolean = false
    )

    private var previousHpRun: Int? = null
    private var maximumHpRun: Int = 0
    private var previousMotionSamples: IntArray? = null
    private var motionEma = 0f
    private var lastFastAtMs = 0L
    private var frozenUntilMs = 0L

    private val reserveVisibility = arrayOfNulls<Boolean>(2)
    private val reserveHitStreak = IntArray(2)
    private val reserveMissStreak = IntArray(2)

    fun reset() {
        previousHpRun = null
        maximumHpRun = 0
        previousMotionSamples = null
        motionEma = 0f
        lastFastAtMs = 0L
        frozenUntilMs = 0L
        reserveVisibility.fill(null)
        reserveHitStreak.fill(0)
        reserveMissStreak.fill(0)
    }

    fun freezeForCharged(nowMs: Long = System.currentTimeMillis(), durationMs: Long = 6_200L) {
        frozenUntilMs = max(frozenUntilMs, nowMs + durationMs)
        previousHpRun = null
        previousMotionSamples = null
    }

    fun analyze(frame: Bitmap, expectedFastDurationMs: Long, nowMs: Long = System.currentTimeMillis()): Event {
        if (frame.isRecycled) return Event()

        val reserve1 = updateReserveVisibility(0, hasNativeReserveHpBar(frame, 0))
        val reserve2 = updateReserveVisibility(1, hasNativeReserveHpBar(frame, 1))

        val hpRun = findPlayerHpFill(frame)
        val opponentHpRun = findOpponentHpFill(frame)
        val battleUiVisible = hpRun != null && opponentHpRun != null
        val hpRatio = if (hpRun != null) {
            maximumHpRun = max(maximumHpRun, hpRun)
            if (maximumHpRun > 0) hpRun.toFloat() / maximumHpRun.toFloat() else null
        } else null
        val motion = motionScore(frame)

        if (nowMs < frozenUntilMs) {
            if (hpRun != null) previousHpRun = hpRun
            return Event(
                hpRatio = hpRatio,
                motion = motion,
                source = "frozen",
                reserve1Visible = reserve1,
                reserve2Visible = reserve2,
                battleUiVisible = battleUiVisible
            )
        }

        val minGap = (expectedFastDurationMs * 0.42f).toLong().coerceIn(230L, 1_450L)
        if (hpRun != null) {
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
                        reserve2Visible = reserve2,
                        battleUiVisible = battleUiVisible
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
                        confidence = .98f,
                        source = "player-hp-drop",
                        reserve1Visible = reserve1,
                        reserve2Visible = reserve2,
                        battleUiVisible = battleUiVisible
                    )
                }
            }
        } else {
            // Never infer enemy energy from motion alone. Player attack VFX occur inside the
            // opponent ROI too, which was the cause of energy rising when the user attacked.
            previousHpRun = null
        }

        return Event(
            hpRatio = hpRatio,
            motion = motion,
            confidence = if (battleUiVisible) .72f else .18f,
            source = if (battleUiVisible) "battle-hud" else "no-battle-hud",
            reserve1Visible = reserve1,
            reserve2Visible = reserve2,
            battleUiVisible = battleUiVisible
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
            if (reserveMissStreak[index] >= 4) reserveVisibility[index] = false
        }
        return reserveVisibility[index]
    }

    /** Detect only the thin native HP line under each reserve card. */
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
                } else run = 0
            }
            y++
        }
        return best >= (frame.width * .034f).toInt().coerceAtLeast(24)
    }

    private fun isReserveHpColor(p: Int): Boolean = isHpColor(p)

    /** Longest saturated horizontal run in the player's HP-bar region. */
    private fun findPlayerHpFill(frame: Bitmap): Int? = findHpFill(
        frame = frame,
        leftFrac = .035f,
        rightFrac = .405f,
        topFrac = .345f,
        bottomFrac = .445f
    )

    /** Enemy HP bar is required as a second battle-HUD anchor so the overlay hides on the map. */
    private fun findOpponentHpFill(frame: Bitmap): Int? = findHpFill(
        frame = frame,
        leftFrac = .49f,
        rightFrac = .91f,
        topFrac = .345f,
        bottomFrac = .445f
    )

    private fun findHpFill(
        frame: Bitmap,
        leftFrac: Float,
        rightFrac: Float,
        topFrac: Float,
        bottomFrac: Float
    ): Int? {
        val left = (frame.width * leftFrac).toInt().coerceAtLeast(0)
        val right = (frame.width * rightFrac).toInt().coerceAtMost(frame.width)
        val top = (frame.height * topFrac).toInt().coerceAtLeast(0)
        val bottom = (frame.height * bottomFrac).toInt().coerceAtMost(frame.height)
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
                } else run = 0
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
