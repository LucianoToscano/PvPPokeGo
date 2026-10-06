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
}
