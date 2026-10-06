package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.DetectedPokemon
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleContinuityPolicyTest {
    private fun p(name: String, cp: Int?, confidence: Float = .98f) =
        DetectedPokemon(name, cp, confidence, name)

    @Test
    fun completeTeamScreenEndsDormantPreviousMatch() {
        assertTrue(
            BattleContinuityPolicy.confirmedTeamSelectionBoundary(
                listOf(p("Houndoom",1499), p("Cramorant",1485), p("Rillaboom",1500)),
                battleActive=false,
                msSinceBattleEvidence=6_000L
            )
        )
    }

    @Test
    fun chargedAnimationGapDoesNotLookLikeTeamScreen() {
        assertFalse(
            BattleContinuityPolicy.confirmedTeamSelectionBoundary(
                listOf(p("Houndoom",1499)),
                battleActive=false,
                msSinceBattleEvidence=12_000L
            )
        )
    }

    @Test
    fun activeBattleNeverEndsFromIncidentalOcr() {
        assertFalse(
            BattleContinuityPolicy.confirmedTeamSelectionBoundary(
                listOf(p("Houndoom",1499), p("Cramorant",1485), p("Rillaboom",1500)),
                battleActive=true,
                msSinceBattleEvidence=9_000L
            )
        )
    }

    @Test
    fun incompleteCpOrWeakIdentityCannotCloseSession() {
        assertFalse(
            BattleContinuityPolicy.confirmedTeamSelectionBoundary(
                listOf(p("Houndoom",1499), p("Cramorant",null), p("Rillaboom",1500,.70f)),
                battleActive=false,
                msSinceBattleEvidence=9_000L
            )
        )
    }
    @Test
    fun oneSidedSwitchNeverCreatesNewBattle() {
        val current = BattleContinuityPolicy.ActiveIdentity("Houndoom", 1499, "Sneasel", 1465)
        val detected = BattleContinuityPolicy.DetectionIdentity("Houndoom", 1499, "Magcargo", 1486)
        assertFalse(
            BattleContinuityPolicy.confirmedNewBattleFromPairedCards(
                current = current,
                detected = detected,
                battleActive = false,
                switchPrompt = false,
                msSinceBattleEvidence = 8_000L
            )
        )
    }

    @Test
    fun bothActivesChangedAfterGapStartsFreshBattle() {
        val current = BattleContinuityPolicy.ActiveIdentity("Houndoom", 1499, "Sneasel", 1465)
        val detected = BattleContinuityPolicy.DetectionIdentity("Cramorant", 1485, "Cradily", 1470)
        assertTrue(
            BattleContinuityPolicy.confirmedNewBattleFromPairedCards(
                current = current,
                detected = detected,
                battleActive = false,
                switchPrompt = false,
                msSinceBattleEvidence = 7_000L
            )
        )
    }

    @Test
    fun chargedAnimationOrSwitchPromptPreservesSession() {
        val current = BattleContinuityPolicy.ActiveIdentity("Houndoom", 1499, "Sneasel", 1465)
        val detected = BattleContinuityPolicy.DetectionIdentity("Cramorant", 1485, "Cradily", 1470)
        assertFalse(
            BattleContinuityPolicy.confirmedNewBattleFromPairedCards(
                current = current,
                detected = detected,
                battleActive = false,
                switchPrompt = true,
                msSinceBattleEvidence = 9_000L
            )
        )
    }

    @Test
    fun truncatedCpAloneCannotSplitSession() {
        val current = BattleContinuityPolicy.ActiveIdentity("Raichu", 1498, "Magcargo", 1486)
        val detected = BattleContinuityPolicy.DetectionIdentity("Raichu", 149, "Magcargo", null)
        assertFalse(
            BattleContinuityPolicy.confirmedNewBattleFromPairedCards(
                current = current,
                detected = detected,
                battleActive = false,
                switchPrompt = false,
                msSinceBattleEvidence = 8_000L
            )
        )
    }

}
