package com.lucianotoscano.pvppokego.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReserveScanPolicyTest {
    @Test
    fun activePokemonAloneIsEnoughToStartReserveDiscovery() {
        assertTrue(
            ReserveScanPolicy.shouldScan(
                hasActivePlayer = true,
                knownTeamSize = 1,
                cardCps = listOf(null, null),
                cardDexes = listOf(null, null),
                cardSpeciesIds = listOf(null, null)
            )
        )
    }

    @Test
    fun noActivePokemonDoesNotScanRandomScreenRegions() {
        assertFalse(
            ReserveScanPolicy.shouldScan(
                hasActivePlayer = false,
                knownTeamSize = 1,
                cardCps = listOf(null, null),
                cardDexes = listOf(null, null),
                cardSpeciesIds = listOf(null, null)
            )
        )
    }

    @Test
    fun bothCardsWithCpAndVisualIdentityStopScanning() {
        assertFalse(
            ReserveScanPolicy.shouldScan(
                hasActivePlayer = true,
                knownTeamSize = 3,
                cardCps = listOf(1485, 1494),
                cardDexes = listOf(845, 346),
                cardSpeciesIds = listOf("cramorant", "cradily")
            )
        )
    }

    @Test
    fun cpWithoutIdentityKeepsVisualScanAlive() {
        assertTrue(
            ReserveScanPolicy.shouldScan(
                hasActivePlayer = true,
                knownTeamSize = 1,
                cardCps = listOf(1485, 1494),
                cardDexes = listOf(null, null),
                cardSpeciesIds = listOf(null, null)
            )
        )
    }
}
