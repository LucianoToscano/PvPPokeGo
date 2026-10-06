package com.lucianotoscano.pvppokego.detect

import kotlin.math.max

/**
 * Pure decision helper used by BattleFrameEventDetector.
 * Fuses HP movement, expected Fast Move cadence and visual motion.
 */
object FastMoveSignalFuser {
    data class Decision(
        val confirm: Boolean,
        val source: String,
        val confidence: Float
    )

    fun decide(
        pendingDropPx: Int,
        hpThresholdPx: Int,
        elapsedMs: Long,
        minGapMs: Long,
        expectedDurationMs: Long,
        motion: Float,
        motionBaseline: Float,
        battleUiVisible: Boolean,
        allowMotionFallback: Boolean
    ): Decision {
        if (elapsedMs < minGapMs) return Decision(false, "gap", 0f)

        if (pendingDropPx >= hpThresholdPx.coerceAtLeast(1)) {
            return Decision(true, "hp-drop", .98f)
        }

        val cadenceReady = elapsedMs >=
            (expectedDurationMs * .72f).toLong().coerceAtLeast(minGapMs)

        if (pendingDropPx > 0 && cadenceReady) {
            return Decision(true, "microdrop", .91f)
        }

        if (allowMotionFallback && battleUiVisible && cadenceReady) {
            val baseline = motionBaseline.coerceAtLeast(1f)
            val strongMotion = motion >= max(16f, baseline * 1.72f)
            val longEnough = elapsedMs >=
                (expectedDurationMs * .88f).toLong().coerceAtLeast(minGapMs)
            if (strongMotion && longEnough) {
                return Decision(true, "motion-cadence", .82f)
            }
        }

        return Decision(false, "none", 0f)
    }
}
