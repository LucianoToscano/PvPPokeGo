package com.lucianotoscano.pvppokego.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class BattleContinuityMatcherTest {
    private val current = BattleContinuityMatcher.ActiveIdentity(
        playerName = "Houndoom",
        playerCp = 1499,
        opponentName = "Sneasel",
        opponentCp = 1465
    )

    @Test
    fun chargedAnimationGapNeverSplitsBattle() {
        val d = BattleContinuityMatcher.fromPairedCards(
            hasSession = true,
            battleActive = false,
            gapSinceBattleEvidenceMs = 3_200L,
            current = current,
            detected = BattleContinuityMatcher.DetectionIdentity(
                "Houndoom", 1499, "Sneasel", 1465
            ),
            switchPrompt = false
        )
        assertEquals(BattleContinuityMatcher.Decision.KEEP, d)
    }

    @Test
    fun oneSidedOpponentSwitchStaysInSameBattle() {
        val d = BattleContinuityMatcher.fromPairedCards(
            hasSession = true,
            battleActive = false,
            gapSinceBattleEvidenceMs = 8_000L,
            current = current,
            detected = BattleContinuityMatcher.DetectionIdentity(
                "Houndoom", 1499, "Magcargo", 1486
            ),
            switchPrompt = false
        )
        assertEquals(BattleContinuityMatcher.Decision.KEEP, d)
    }

    @Test
    fun switchPromptAlwaysKeepsSession() {
        val d = BattleContinuityMatcher.fromPairedCards(
            hasSession = true,
            battleActive = false,
            gapSinceBattleEvidenceMs = 9_000L,
            current = current,
            detected = BattleContinuityMatcher.DetectionIdentity(
                "Rillaboom", 1500, "Magcargo", 1486
            ),
            switchPrompt = true
        )
        assertEquals(BattleContinuityMatcher.Decision.KEEP, d)
    }

    @Test
    fun stableTeamScreenAfterBattleGapCreatesBoundary() {
        assertEquals(
            BattleContinuityMatcher.Decision.NEW_MATCH,
            BattleContinuityMatcher.fromTeamScreen(
                hasSession = true,
                battleActive = false,
                gapSinceBattleEvidenceMs = 3_000L,
                stableThreePokemonTeam = true
            )
        )
    }

    @Test
    fun bothActiveIdentitiesChangingAfterLongGapStartsNewMatch() {
        val d = BattleContinuityMatcher.fromPairedCards(
            hasSession = true,
            battleActive = false,
            gapSinceBattleEvidenceMs = 7_000L,
            current = current,
            detected = BattleContinuityMatcher.DetectionIdentity(
                "Cramorant", 1485, "Cradily", 1470
            ),
            switchPrompt = false
        )
        assertEquals(BattleContinuityMatcher.Decision.NEW_MATCH, d)
    }

    @Test
    fun missingOrTruncatedCpCannotCreateBoundaryByItself() {
        val d = BattleContinuityMatcher.fromPairedCards(
            hasSession = true,
            battleActive = false,
            gapSinceBattleEvidenceMs = 8_000L,
            current = current,
            detected = BattleContinuityMatcher.DetectionIdentity(
                "Houndoom", 149, "Sneasel", null
            ),
            switchPrompt = false
        )
        assertEquals(BattleContinuityMatcher.Decision.KEEP, d)
    }
}
