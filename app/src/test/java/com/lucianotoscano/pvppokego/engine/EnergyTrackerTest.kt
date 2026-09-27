package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.EnergyConfidence
import com.lucianotoscano.pvppokego.data.MoveDef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnergyTrackerTest {
    private val fast = MoveDef(
        moveId = "FAST_TEST",
        name = "Fast Test",
        energyGain = 9,
        cooldown = 1000
    )

    private fun charged(id: String, cost: Int) = MoveDef(
        moveId = id,
        name = id,
        energy = -cost
    )

    @Test
    fun exactChargedMovePreservesResidualEnergy() {
        val tracker = EnergyTracker()
        tracker.onFastMove(fast, 6) // 54 energy

        assertTrue(tracker.onObservedChargedMove(charged("C45", 45)))
        assertEquals(9, tracker.minEnergy)
        assertEquals(9, tracker.maxEnergy)
        assertFalse(tracker.isRange)
    }

    @Test
    fun unknownChargedMoveCreatesPlausibleResidualRange() {
        val tracker = EnergyTracker()
        tracker.restoreRange(70, 70, 8)

        assertTrue(tracker.onObservedUnknownCharged(listOf(charged("C35", 35), charged("C55", 55))))
        assertEquals(15, tracker.minEnergy)
        assertEquals(35, tracker.maxEnergy)
        assertTrue(tracker.isRange)
    }

    @Test
    fun predictionDistinguishesPossibleFromDefinitelyReady() {
        val tracker = EnergyTracker()
        tracker.restoreRange(30, 50, 5)
        val prediction = tracker.predict(charged("C45", 45), fast, EnergyConfidence.ESTIMATED)

        assertTrue(prediction.ready)
        assertFalse(prediction.definitelyReady)
        assertEquals(EnergyConfidence.RANGE, prediction.confidence)
        assertEquals(0, prediction.fastMovesRemaining)
    }

    @Test
    fun energyNeverExceedsPokemonGoCap() {
        val tracker = EnergyTracker()
        tracker.onFastMove(fast, 99)
        assertEquals(100, tracker.minEnergy)
        assertEquals(100, tracker.maxEnergy)
    }
}
