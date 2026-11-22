package pvppokego.core.battle

/** Offline-only interpretation of test frames; never dispatches Android gestures. */
enum class VisualBattleState {
    IDLE, PREPARATION, COMBAT_ACTIVE, CHARGED_SEQUENCE,
    SHIELD_PROMPT, SWITCH_PROMPT, OBSERVATION_UNCERTAIN,
    DESYNC_RECOVERY, ENDED
}

data class VisualBattleFrame(
    val timestampMs: Long,
    val confidence: Double,
    val frameSignature: String,
    val preparationVisible: Boolean = false,
    val combatVisible: Boolean = false,
    val chargedSequenceVisible: Boolean = false,
    val shieldPromptVisible: Boolean = false,
    val switchPromptVisible: Boolean = false,
    val battleEndVisible: Boolean = false
)

data class VisualBattleTransition(
    val from: VisualBattleState,
    val to: VisualBattleState,
    val accepted: Boolean,
    val reason: String
) {
    /** An unambiguous active-combat observation is required for recommendations. */
    val canRecommendCombatAction: Boolean get() = accepted && to == VisualBattleState.COMBAT_ACTIVE
}

/**
 * State reducer for synthetic/replayed video frames. A hash or fingerprint is
 * supplied by the caller; no screenshot, account or game process is accessed.
 * Loss of confidence and stalled observations fail closed to a non-active state.
 */
class BattleVisualStateTracker(
    private val minConfidence: Double = 0.75,
    private val frozenFrameLimitMs: Long = 3_000L,
    private val maxObservationGapMs: Long = 1_500L
) {
    init {
        require(minConfidence in 0.0..1.0)
        require(frozenFrameLimitMs > 0)
        require(maxObservationGapMs > 0)
    }

    var state: VisualBattleState = VisualBattleState.IDLE
        private set

    private var lastTimestampMs: Long? = null
    private var previousCombatSignature: String? = null
    private var unchangedSinceMs: Long? = null

    fun reset() {
        state = VisualBattleState.IDLE
        lastTimestampMs = null
        previousCombatSignature = null
        unchangedSinceMs = null
    }

    fun onFrame(frame: VisualBattleFrame): VisualBattleTransition {
        val previous = state
        val priorTime = lastTimestampMs
        if (frame.timestampMs < 0 || !frame.confidence.isFinite() ||
            frame.confidence !in 0.0..1.0 || frame.frameSignature.isBlank()
        ) {
            return VisualBattleTransition(previous, state, false, "invalid_observation")
        }
        if (priorTime != null && frame.timestampMs <= priorTime) {
            return VisualBattleTransition(previous, state, false, "non_monotonic_frame")
        }
        lastTimestampMs = frame.timestampMs

        if (frame.confidence < minConfidence) {
            // Once a match has ended, uncertain frames cannot revive it.
            // A new session requires a high-confidence preparation frame or reset().
            state = if (previous == VisualBattleState.ENDED) {
                VisualBattleState.ENDED
            } else {
                VisualBattleState.OBSERVATION_UNCERTAIN
            }
            clearCombatWatchdog()
            val reason = if (state == VisualBattleState.ENDED) "battle_ended" else "low_confidence"
            return VisualBattleTransition(previous, state, true, reason)
        }

        // Explicit dialogs outrank a generic combat indicator. A finished match
        // cannot reactivate without preparation or an explicit session reset.
        val next = when {
            frame.battleEndVisible -> VisualBattleState.ENDED
            state == VisualBattleState.ENDED && !frame.preparationVisible -> VisualBattleState.ENDED
            frame.shieldPromptVisible -> VisualBattleState.SHIELD_PROMPT
            frame.chargedSequenceVisible -> VisualBattleState.CHARGED_SEQUENCE
            frame.switchPromptVisible -> VisualBattleState.SWITCH_PROMPT
            frame.preparationVisible -> VisualBattleState.PREPARATION
            frame.combatVisible -> VisualBattleState.COMBAT_ACTIVE
            else -> VisualBattleState.IDLE
        }

        if (next != VisualBattleState.COMBAT_ACTIVE) {
            state = next
            clearCombatWatchdog()
            return VisualBattleTransition(previous, state, true, "phase_${next.name.lowercase()}")
        }

        // Long capture gaps or persistent identical frames are not proof of
        // successful gameplay progression. Pause recommendations until fresh
        // visual progress is confirmed.
        if (previous == VisualBattleState.COMBAT_ACTIVE && priorTime != null &&
            frame.timestampMs - priorTime > maxObservationGapMs
        ) {
            state = VisualBattleState.DESYNC_RECOVERY
            clearCombatWatchdog()
            previousCombatSignature = frame.frameSignature
            return VisualBattleTransition(previous, state, true, "observation_gap")
        }

        if (previous == VisualBattleState.DESYNC_RECOVERY &&
            frame.frameSignature == previousCombatSignature
        ) {
            return VisualBattleTransition(previous, state, true, "waiting_for_visual_progress")
        }

        if (previous != VisualBattleState.COMBAT_ACTIVE ||
            frame.frameSignature != previousCombatSignature
        ) {
            previousCombatSignature = frame.frameSignature
            unchangedSinceMs = frame.timestampMs
        } else if (unchangedSinceMs != null &&
            frame.timestampMs - unchangedSinceMs!! >= frozenFrameLimitMs
        ) {
            state = VisualBattleState.DESYNC_RECOVERY
            return VisualBattleTransition(previous, state, true, "frozen_frame")
        }
        state = VisualBattleState.COMBAT_ACTIVE
        return VisualBattleTransition(previous, state, true, "combat_visible")
    }

    private fun clearCombatWatchdog() {
        previousCombatSignature = null
        unchangedSinceMs = null
    }
}
