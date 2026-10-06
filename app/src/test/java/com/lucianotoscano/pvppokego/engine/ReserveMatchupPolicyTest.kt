package com.lucianotoscano.pvppokego.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReserveMatchupPolicyTest {
    @Test
    fun stableTeamIdentityCanBeEvaluatedBeforeNativeCardOrderIsPinned() {
        assertTrue(
            ReserveMatchupPolicy.canEvaluate(
                speciesId = "cradily",
                identityConfidence = 0.94f,
                types = listOf("rock", "grass"),
                enemyTypes = listOf("dark", "dragon"),
                cardMappingConfirmed = false
            )
        )
    }

    @Test
    fun weakFallbackWithoutConfirmedCardStaysUnknown() {
        assertFalse(
            ReserveMatchupPolicy.canEvaluate(
                speciesId = "cradily",
                identityConfidence = 0.45f,
                types = listOf("rock", "grass"),
                enemyTypes = listOf("dark", "dragon"),
                cardMappingConfirmed = false
            )
        )
    }

    @Test
    fun confirmedNativeCardCanBeEvaluatedWhenTypesAreKnown() {
        assertTrue(
            ReserveMatchupPolicy.canEvaluate(
                speciesId = null,
                identityConfidence = 0f,
                types = listOf("flying", "dragon"),
                enemyTypes = listOf("ground", "water"),
                cardMappingConfirmed = true
            )
        )
    }

    @Test
    fun missingEnemyTypesNeverProducesSpecificMatchup() {
        assertFalse(
            ReserveMatchupPolicy.canEvaluate(
                speciesId = "altaria",
                identityConfidence = 0.98f,
                types = listOf("flying", "dragon"),
                enemyTypes = emptyList(),
                cardMappingConfirmed = true
            )
        )
    }
}
