package pvppokego.core.battle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pvppokego.core.data.InMemoryPvpokeRepository

class BattleDecisionEngineTest {
    private val movesTsv = """
        move_id\tname\ttype\tpower\tenergy_gain\tenergy_cost\tcooldown_ms\tturns
        WATER_GUN\tWater Gun\twater\t3\t3\t0\t500\t1
        HYDRO_CANNON\tHydro Cannon\twater\t80\t0\t40\t500\t1
        ICE_BEAM\tIce Beam\tice\t90\t0\t55\t500\t1
        SKY_ATTACK\tSky Attack\tflying\t75\t0\t50\t500\t1
        MOONBLAST\tMoonblast\tfairy\t110\t0\t60\t500\t1
    """.trimIndent()

    private val pokemonTsv = """
        species_id\tspecies_name\ttypes\tfast_moves\tcharged_moves
        feraligatr\tFeraligatr\twater\tWATER_GUN\tHYDRO_CANNON|ICE_BEAM
        altaria\tAltaria\tdragon|flying\t\tSKY_ATTACK|MOONBLAST
    """.trimIndent()

    private val repo = InMemoryPvpokeRepository.fromTsv(
        pokemonTsv = pokemonTsv,
        movesTsv = movesTsv
    )

    private val engine = BattleDecisionEngine(repo)

    @Test
    fun smartModeCanChooseSecondChargedButton() {
        val decision = engine.chooseChargedMove(
            attackerSpeciesId = "feraligatr",
            chargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
            currentEnergy = 60,
            defenderSpeciesId = "altaria",
            mode = ChargedSelectionMode.SMART,
            defenderHpFraction = 1.0,
            defenderShieldsRemaining = 0
        )

        assertEquals("ICE_BEAM", decision.selected?.move?.moveId)
        assertEquals(1, decision.selected?.buttonIndex)
        assertTrue(decision.selected?.typeMultiplier ?: 0.0 > 1.0)
    }

    @Test
    fun firstReadyModePreservesClassicBehavior() {
        val decision = engine.chooseChargedMove(
            attackerSpeciesId = "feraligatr",
            chargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
            currentEnergy = 60,
            defenderSpeciesId = "altaria",
            mode = ChargedSelectionMode.FIRST_READY
        )

        assertEquals("HYDRO_CANNON", decision.selected?.move?.moveId)
        assertEquals(0, decision.selected?.buttonIndex)
    }

    @Test
    fun controllerUsesSmartSecondButtonInsteadOfFastTap() {
        val controller = BattleAutomationController(engine)
        val plan = controller.plan(
            frame = BattleAutomationFrame(
                mySpeciesId = "feraligatr",
                myChargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
                myEnergy = 60,
                myHpFraction = 0.8,
                myShieldsRemaining = 1,
                enemySpeciesId = "altaria",
                enemyHpFraction = 1.0,
                enemyShieldsRemaining = 0
            ),
            settings = BattleAutomationSettings(
                enabled = true,
                autoFastAttack = true,
                autoChargedAttack = true,
                chargedSelectionMode = ChargedSelectionMode.SMART
            )
        )

        val action = plan.actions.single() as BattleActionIntent.TapChargedAttack
        assertEquals(1, action.buttonIndex)
        assertEquals("ICE_BEAM", action.moveId)
    }

    @Test
    fun automationCanBeDisabledGlobally() {
        val controller = BattleAutomationController(engine)
        val plan = controller.plan(
            frame = BattleAutomationFrame(
                mySpeciesId = "feraligatr",
                myChargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
                myEnergy = 100,
                myHpFraction = 1.0,
                myShieldsRemaining = 2,
                enemySpeciesId = "altaria",
                enemyHpFraction = 1.0,
                enemyShieldsRemaining = 2
            ),
            settings = BattleAutomationSettings(enabled = false)
        )

        assertTrue(plan.actions.isEmpty())
    }

    @Test
    fun shieldOffNeverRequestsShield() {
        val decision = engine.recommendShield(
            defenderSpeciesId = "feraligatr",
            attackerSpeciesId = "altaria",
            candidateChargedMoveIds = listOf("MOONBLAST"),
            mode = ShieldMode.OFF,
            shieldsRemaining = 2,
            defenderHpFraction = 0.2
        )
        assertFalse(decision.useShield)
    }
    @Test
    fun shieldPromptWithAutoShieldOffNeverEmitsOffensiveActions() {
        val controller = BattleAutomationController(engine)
        val plan = controller.plan(
            frame = BattleAutomationFrame(
                mySpeciesId = "feraligatr",
                myChargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
                myEnergy = 100,
                myHpFraction = 0.7,
                myShieldsRemaining = 2,
                enemySpeciesId = "altaria",
                enemyHpFraction = 0.6,
                enemyShieldsRemaining = 0,
                shieldPromptVisible = true
            ),
            settings = BattleAutomationSettings(
                enabled = true,
                autoFastAttack = true,
                autoChargedAttack = true,
                autoShield = false
            )
        )
        assertTrue(plan.actions.isEmpty())
    }

    @Test
    fun shieldPromptWithUnknownThreatNeverEmitsOffensiveActions() {
        val controller = BattleAutomationController(engine)
        val plan = controller.plan(
            frame = BattleAutomationFrame(
                mySpeciesId = "feraligatr",
                myChargedMoveIds = listOf("HYDRO_CANNON", "ICE_BEAM"),
                myEnergy = 100,
                myHpFraction = 0.5,
                myShieldsRemaining = 2,
                enemySpeciesId = "altaria",
                enemyChargedMoveIdsCurrentlyPlausible = emptyList(),
                enemyHpFraction = 0.6,
                enemyShieldsRemaining = 0,
                shieldPromptVisible = true
            ),
            settings = BattleAutomationSettings(
                enabled = true,
                autoFastAttack = true,
                autoChargedAttack = true,
                autoShield = true
            )
        )
        assertTrue(plan.actions.isEmpty())
        assertEquals("no_known_charged_move", plan.shieldDecision?.reason)
    }

}
