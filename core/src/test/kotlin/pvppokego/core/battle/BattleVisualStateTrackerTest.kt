package pvppokego.core.battle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BattleVisualStateTrackerTest {
    private fun frame(
        time: Long,
        signature: String = "frame",
        confidence: Double = 0.99,
        preparation: Boolean = false,
        combat: Boolean = false,
        shield: Boolean = false,
        charged: Boolean = false,
        switch: Boolean = false,
        ended: Boolean = false
    ) = VisualBattleFrame(
        timestampMs = time,
        confidence = confidence,
        frameSignature = signature,
        preparationVisible = preparation,
        combatVisible = combat,
        shieldPromptVisible = shield,
        chargedSequenceVisible = charged,
        switchPromptVisible = switch,
        battleEndVisible = ended
    )

    @Test
    fun preparationOverridesGenericCombatAndDoesNotRecommendAttack() {
        val tracker = BattleVisualStateTracker()
        val start = tracker.onFrame(frame(100L, "start", preparation = true, combat = true))
        assertEquals(VisualBattleState.PREPARATION, start.to)
        assertFalse(start.canRecommendCombatAction)
        val combat = tracker.onFrame(frame(200L, "combat", combat = true))
        assertEquals(VisualBattleState.COMBAT_ACTIVE, combat.to)
        assertTrue(combat.canRecommendCombatAction)
    }

    @Test
    fun modalStatesAlwaysOverrideCombat() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "active", combat = true))
        assertEquals(
            VisualBattleState.SHIELD_PROMPT,
            tracker.onFrame(frame(200L, "shield", combat = true, charged = true, shield = true)).to
        )
        assertEquals(
            VisualBattleState.CHARGED_SEQUENCE,
            tracker.onFrame(frame(300L, "charged", combat = true, charged = true)).to
        )
        assertEquals(
            VisualBattleState.SWITCH_PROMPT,
            tracker.onFrame(frame(400L, "switch", combat = true, switch = true)).to
        )
    }

    @Test
    fun battleEndIsStickyUntilFreshPreparation() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, combat = true))
        val ended = tracker.onFrame(frame(200L, "end", combat = true, ended = true))
        assertEquals(VisualBattleState.ENDED, ended.to)
        assertFalse(ended.canRecommendCombatAction)
        assertEquals(
            VisualBattleState.ENDED,
            tracker.onFrame(frame(300L, "replay", combat = true)).to
        )
        assertEquals(
            VisualBattleState.PREPARATION,
            tracker.onFrame(frame(400L, "new match", preparation = true)).to
        )
    }

    @Test
    fun uncertainObservationFailsClosedUntilConfirmed() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "a", combat = true))
        val uncertain = tracker.onFrame(frame(200L, "noise", confidence = 0.55, combat = true))
        assertEquals(VisualBattleState.OBSERVATION_UNCERTAIN, uncertain.to)
        assertFalse(uncertain.canRecommendCombatAction)
        assertTrue(tracker.onFrame(frame(300L, "clean", combat = true)).canRecommendCombatAction)
    }

    @Test
    fun frozenCombatFrameTriggersDesyncAndWaitsForVisualProgress() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "same", combat = true))
        tracker.onFrame(frame(1000L, "same", combat = true))
        tracker.onFrame(frame(3000L, "same", combat = true))
        val desync = tracker.onFrame(frame(3100L, "same", combat = true))
        assertEquals(VisualBattleState.DESYNC_RECOVERY, desync.to)
        assertEquals("frozen_frame", desync.reason)
        assertFalse(desync.canRecommendCombatAction)
        assertEquals(
            VisualBattleState.DESYNC_RECOVERY,
            tracker.onFrame(frame(3200L, "same", combat = true)).to
        )
        assertEquals(
            VisualBattleState.COMBAT_ACTIVE,
            tracker.onFrame(frame(3300L, "changed", combat = true)).to
        )
    }

    @Test
    fun longObservationGapTriggersDesync() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "a", combat = true))
        val gap = tracker.onFrame(frame(2000L, "b", combat = true))
        assertEquals(VisualBattleState.DESYNC_RECOVERY, gap.to)
        assertEquals("observation_gap", gap.reason)
        assertFalse(gap.canRecommendCombatAction)
    }

    @Test
    fun staleAndDuplicateTimestampsAreIgnoredWithoutStateMutation() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "start", preparation = true))
        assertFalse(tracker.onFrame(frame(100L, "end", ended = true)).accepted)
        assertFalse(tracker.onFrame(frame(90L, "end", ended = true)).accepted)
        assertEquals(VisualBattleState.PREPARATION, tracker.state)
    }

    @Test
    fun invalidConfidenceAndBlankSignatureAreRejected() {
        val tracker = BattleVisualStateTracker()
        assertFalse(tracker.onFrame(frame(100L, "x", confidence = Double.NaN, combat = true)).accepted)
        assertFalse(tracker.onFrame(frame(100L, " ", combat = true)).accepted)
        assertFalse(tracker.onFrame(frame(-1L, "x", combat = true)).accepted)
        assertEquals(VisualBattleState.IDLE, tracker.state)
    }

    @Test
    fun resetClearsWatchdogAndSession() {
        val tracker = BattleVisualStateTracker()
        tracker.onFrame(frame(100L, "a", combat = true))
        tracker.onFrame(frame(3100L, "a", combat = true))
        tracker.reset()
        assertEquals(VisualBattleState.IDLE, tracker.state)
        assertTrue(tracker.onFrame(frame(0L, "a", combat = true)).canRecommendCombatAction)
    }
}
