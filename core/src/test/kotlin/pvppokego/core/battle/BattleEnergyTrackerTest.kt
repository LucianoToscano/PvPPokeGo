package pvppokego.core.battle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pvppokego.core.data.InMemoryPvpokeRepository

class BattleEnergyTrackerTest {

    private val movesTsv = """
        move_id\tname\ttype\tpower\tenergy_gain\tenergy_cost\tcooldown_ms\tturns
        SHADOW_CLAW\tShadow Claw\tghost\t6\t8\t0\t1000\t2
        HYDRO_CANNON\tHydro Cannon\twater\t80\t0\t40\t500\t1
        ICE_BEAM\tIce Beam\tice\t90\t0\t55\t500\t1
        CRUNCH\tCrunch\tdark\t70\t0\t45\t500\t1
    """.trimIndent()

    private val pokemonTsv = """
        species_id\tspecies_name\ttypes\tfast_moves\tcharged_moves
        feraligatr\tFeraligatr\twater\tSHADOW_CLAW\tHYDRO_CANNON|ICE_BEAM|CRUNCH
    """.trimIndent()

    private val repo = InMemoryPvpokeRepository.fromTsv(
        pokemonTsv = pokemonTsv,
        movesTsv = movesTsv
    )

    @Test
    fun chargedMovePreservesResidualEnergy() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.setChargedMoves(0, listOf("HYDRO_CANNON", "ICE_BEAM"))
        tracker.switchTo(0)

        tracker.onFastMove(0, count = 8) // 64 energy
        tracker.onChargedMove(0, "HYDRO_CANNON")

        assertEquals(24, tracker.state(0).energy)
        val hydro = tracker.chargedMoveProgress(0).first { it.move.moveId == "HYDRO_CANNON" }
        assertEquals(2, hydro.fastMovesNeeded)
    }

    @Test
    fun readyCounterLeavesZeroAfterChargedMoveIsUsed() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.setChargedMoves(0, listOf("HYDRO_CANNON", "ICE_BEAM"))
        tracker.switchTo(0)

        tracker.onFastMove(0, count = 5) // exactly 40 energy
        val ready = tracker.chargedMoveProgress(0).first { it.move.moveId == "HYDRO_CANNON" }
        assertEquals(0, ready.fastMovesNeeded)
        assertTrue(ready.ready)

        tracker.onChargedMove(0, "HYDRO_CANNON")

        assertEquals(0, tracker.state(0).energy)
        val after = tracker.chargedMoveProgress(0).first { it.move.moveId == "HYDRO_CANNON" }
        assertEquals(5, after.fastMovesNeeded)
        assertTrue(!after.ready)
    }

    @Test
    fun switchingDoesNotResetEnergy() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.identifyPokemon(1, "feraligatr", "SHADOW_CLAW")

        tracker.switchTo(0)
        tracker.onFastMove(0, count = 5) // 40 energy
        tracker.switchTo(1)
        tracker.switchTo(0)

        assertEquals(40, tracker.state(0).energy)
        assertEquals(5, tracker.state(0).fastMovesObserved)
    }

    @Test
    fun chargedMoveButtonsCanBeChangedIndependently() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.setChargedMoves(0, listOf("HYDRO_CANNON", "ICE_BEAM"))

        tracker.setChargedMove(0, buttonIndex = 1, moveId = "CRUNCH")
        assertEquals(
            listOf("HYDRO_CANNON", "CRUNCH"),
            tracker.state(0).selectedChargedMoveIds
        )

        // Selecting the move already used by the other button swaps them.
        tracker.setChargedMove(0, buttonIndex = 1, moveId = "HYDRO_CANNON")
        assertEquals(
            listOf("CRUNCH", "HYDRO_CANNON"),
            tracker.state(0).selectedChargedMoveIds
        )
    }

    @Test
    fun faintingClearsEnergyAndLocksSlot() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.switchTo(0)
        tracker.onFastMove(0, count = 5)
        tracker.markFainted(0)

        assertEquals(0, tracker.state(0).energy)
        assertTrue(tracker.activeEnemySlot() == null)
    }
}
