package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.WeightedMove
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyEnergyTrackerTest {
    private fun fast(id: String, gain: Int, turns: Int) = MoveDef(
        moveId = id,
        name = id,
        energyGain = gain,
        cooldown = turns * 500
    )

    private fun charged(id: String, cost: Int) = MoveDef(
        moveId = id,
        name = id,
        energy = cost,
        cooldown = 500
    )

    @Test
    fun completedFastMovesUseTurnsAndEnergyForOneTwoThreeAndFourTurns() {
        listOf(1, 2, 3, 4).forEach { turns ->
            val tracker = EnemyEnergyTracker()
            val move = fast("F$turns", 7 + turns, turns)
            tracker.reset(listOf(WeightedMove(move, 1f)))
            assertTrue(tracker.observeFastCompletion(3, eventId = turns.toLong(), confidence = 1f))
            val h = tracker.hypotheses().single()
            assertEquals(3 * (7 + turns), h.energy)
            assertEquals(3, h.completedFastMoves)
            assertEquals(3 * turns, h.elapsedTurns)
        }
    }

    @Test
    fun duplicateFastEventCannotGenerateEnergyTwice() {
        val tracker = EnemyEnergyTracker()
        tracker.reset(listOf(WeightedMove(fast("F", 9, 2), 1f)))
        assertTrue(tracker.observeFastCompletion(1, 1001L, 1f))
        assertFalse(tracker.observeFastCompletion(1, 1001L, 1f))
        assertEquals(9, tracker.energyLikely())
        assertEquals(1, tracker.hypotheses().single().completedFastMoves)
    }

    @Test
    fun exactChargedPrunesImpossibleHypothesesAndPreservesResidualEnergy() {
        val tracker = EnemyEnergyTracker()
        tracker.reset(
            listOf(
                WeightedMove(fast("LOW", 7, 2), .5f),
                WeightedMove(fast("HIGH", 12, 3), .5f)
            )
        )
        tracker.observeFastCompletion(4, 10L, 1f)
        // LOW=28 cannot pay 35. HIGH=48 can and must remain with 13.
        assertTrue(tracker.observeCharged(charged("C35", 35)))
        assertEquals(1, tracker.hypothesisCount())
        assertEquals("HIGH", tracker.hypotheses().single().fastMove.moveId)
        assertEquals(13, tracker.energyLikely())
    }

    @Test
    fun unknownChargedBranchesAffordableCostsWithoutInventingOne() {
        val tracker = EnemyEnergyTracker()
        tracker.reset(listOf(WeightedMove(fast("F", 10, 2), 1f)))
        tracker.observeFastCompletion(7, 11L, 1f)
        assertTrue(tracker.observeUnknownCharged(listOf(charged("C35",35), charged("C50",50))))
        assertEquals(setOf(20, 35), tracker.hypotheses().map { it.energy }.toSet())
    }

    @Test
    fun overfarmTwoConsecutiveChargedAndSnapshotReturnArePreserved() {
        val move = fast("F", 10, 2)
        val tracker = EnemyEnergyTracker()
        tracker.reset(listOf(WeightedMove(move, 1f)))
        tracker.observeFastCompletion(10, 12L, 1f) // cap 100
        assertEquals(100, tracker.energyLikely())
        assertTrue(tracker.observeCharged(charged("C45",45)))
        assertEquals(55, tracker.energyLikely())
        assertTrue(tracker.observeCharged(charged("C45",45)))
        assertEquals(10, tracker.energyLikely())

        val restored = EnemyEnergyTracker()
        restored.reset(listOf(WeightedMove(move, 1f)), tracker.snapshot())
        assertEquals(10, restored.energyLikely())
        assertEquals(10, restored.hypotheses().single().completedFastMoves)
    }

    @Test
    fun cadenceEvidenceReweightsUnknownFastMoveHypotheses() {
        val twoTurn = fast("TWO", 9, 2)
        val fourTurn = fast("FOUR", 16, 4)
        val tracker = EnemyEnergyTracker()
        tracker.reset(listOf(WeightedMove(twoTurn,.5f), WeightedMove(fourTurn,.5f)))
        tracker.observeFastCompletion(1, 21L, .95f, observedIntervalMs = 1000L)
        val best = tracker.hypotheses().maxByOrNull { it.weight }!!
        assertEquals("TWO", best.fastMove.moveId)
    }
}
