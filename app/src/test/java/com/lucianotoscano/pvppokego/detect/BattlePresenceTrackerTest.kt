package com.lucianotoscano.pvppokego.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattlePresenceTrackerTest {
    @Test
    fun strongVisualEvidenceActivatesBattle() {
        val tracker = BattlePresenceTracker()
        assertTrue(tracker.update(visualConfidence = .80f, nowMs = 1_000L))
        assertTrue(tracker.active)
    }

    @Test
    fun weakSingleSignalDoesNotActivateBattle() {
        val tracker = BattlePresenceTracker()
        assertFalse(tracker.update(visualConfidence = .35f, nowMs = 1_000L))
        assertFalse(tracker.active)
    }

    @Test
    fun activeBattleUsesHysteresisBeforeDisappearing() {
        val tracker = BattlePresenceTracker()
        assertTrue(tracker.update(pairedPokemonCards = true, nowMs = 1_000L))

        // One second without evidence should not immediately hide the HUD.
        assertTrue(tracker.tick(nowMs = 2_000L))

        // Continued lack of evidence eventually deactivates it.
        tracker.tick(nowMs = 3_000L)
        assertFalse(tracker.tick(nowMs = 4_000L))
    }

    @Test
    fun switchPromptIsStrongBattleEvidence() {
        val tracker = BattlePresenceTracker()
        assertTrue(tracker.update(switchPrompt = true, nowMs = 1_000L))
    }
}
