package pvppokego.core.battle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pvppokego.core.data.InMemoryPvpokeRepository
import pvppokego.core.model.EvidenceLevel

class BattleAssistEngineTest {
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

    private val rankingsTsv = """
        league_cp\trank\tspecies_id\tfast_move\tcharged_moves\tscore
        1500\t1\tferaligatr\tSHADOW_CLAW\tHYDRO_CANNON|ICE_BEAM\t95.0
    """.trimIndent()

    private val repo = InMemoryPvpokeRepository.fromTsv(
        pokemonTsv = pokemonTsv,
        movesTsv = movesTsv,
        rankingsTsv = rankingsTsv
    )

    @Test
    fun predictedMovesBecomeConfirmedWithoutLosingTheOtherSlot() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr")
        assertEquals(listOf("HYDRO_CANNON", "ICE_BEAM"), tracker.state(0).selectedChargedMoveIds)

        tracker.onChargedMove(0, "CRUNCH")
        assertEquals(listOf("CRUNCH", "ICE_BEAM"), tracker.state(0).selectedChargedMoveIds)
        assertEquals(setOf("CRUNCH"), tracker.state(0).confirmedChargedMoveIds)
    }

    @Test
    fun energyRangeWidensWhenVisionMayHaveMissedFastMoves() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.onFastMove(0, 3) // exact 24

        val range = tracker.notePossibleMissedFastMoves(0, maxAdditionalFastMoves = 2)
        assertEquals(24, range.min)
        assertEquals(40, range.max)
        assertEquals(EvidenceLevel.PROBABLE, range.confidence)
    }

    @Test
    fun impossibleChargedObservationSelfCorrectsEnergy() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.onFastMove(0, 2) // tracker sees only 16

        val after = tracker.onChargedMove(0, "HYDRO_CANNON") // cost 40 proves detections were missed
        assertEquals(0, after.energy)
        assertEquals(24, after.lastEnergyCorrection)
        assertEquals(1, after.energyCorrectionCount)
        assertTrue(tracker.energyRange(0).exact)
    }

    @Test
    fun doubleChargeIsReported() {
        val tracker = BattleEnergyTracker(repo)
        tracker.identifyPokemon(0, "feraligatr", "SHADOW_CLAW")
        tracker.setChargedMoves(0, listOf("HYDRO_CANNON", "ICE_BEAM"))
        tracker.onFastMove(0, 10) // 80 energy

        val hydro = tracker.chargedMoveProgress(0).first { it.move.moveId == "HYDRO_CANNON" }
        assertTrue(hydro.ready)
        assertEquals(2, hydro.chargesReady)
        assertEquals(0, hydro.fastMovesNeededForSecond)
    }

    @Test
    fun baitRiskUsesEnergyRangeAndKnownMoves() {
        val advisor = BaitRiskAdvisor(repo)
        val result = advisor.assess(
            chargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
            confirmedMoveIds = setOf("HYDRO_CANNON", "ICE_BEAM"),
            energy = pvppokego.core.model.EnergyRangeEstimate(60, 70, EvidenceLevel.PROBABLE)
        )
        assertEquals(BaitRiskLevel.HIGH, result.level)
    }

    @Test
    fun switchTimerUsesCallerSuppliedRules() {
        val timer = SwitchTimerTracker(cooldownMs = 50_000L)
        timer.markEnemySwitch(1_000L)
        assertEquals(20_000L, timer.enemyState(31_000L).remainingMs)
        assertTrue(timer.enemyState(51_000L).ready)
    }

    @Test
    fun alertsOnlyOverlayStaysHiddenUntilSomethingImportantHappens() {
        val policy = OverlayVisibilityPolicy()
        val hidden = policy.resolve(
            OverlayPolicyInput(
                preferredMode = OverlayDisplayMode.ALERTS_ONLY,
                hasActiveBattle = true,
                hasImportantAlert = false,
                userManuallyHidden = false
            )
        )
        val compact = policy.resolve(
            OverlayPolicyInput(
                preferredMode = OverlayDisplayMode.ALERTS_ONLY,
                hasActiveBattle = true,
                hasImportantAlert = true,
                userManuallyHidden = false
            )
        )
        assertEquals(OverlayDisplayMode.HIDDEN, hidden)
        assertEquals(OverlayDisplayMode.COMPACT, compact)
    }
}
