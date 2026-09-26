package pvppokego.core.battle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pvppokego.core.data.InMemoryPvpokeRepository

class BattleObservationProcessorTest {

    private val movesTsv = """
        move_id\tname\ttype\tpower\tenergy_gain\tenergy_cost\tcooldown_ms\tturns
        QUICK_ATTACK\tQuick Attack\tnormal\t5\t8\t0\t1000\t2
        THUNDERBOLT\tThunderbolt\telectric\t90\t0\t55\t500\t1
        DISCHARGE\tDischarge\telectric\t65\t0\t45\t500\t1
    """.trimIndent()

    private val pokemonTsv = """
        species_id\tspecies_name\ttypes\tfast_moves\tcharged_moves
        plusle\tPlusle\telectric\tQUICK_ATTACK\tTHUNDERBOLT|DISCHARGE
    """.trimIndent()

    private val repo = InMemoryPvpokeRepository.fromTsv(
        pokemonTsv = pokemonTsv,
        movesTsv = movesTsv
    )

    @Test
    fun portugueseNoShieldBannerSpendsEnergyAndRecalculatesCounter() {
        val session = BattleSession(repo)
        session.enemyTracker.identifyPokemon(0, "plusle", "QUICK_ATTACK")
        session.enemyTracker.setChargedMoves(0, listOf("THUNDERBOLT", "DISCHARGE"))
        session.enemyTracker.switchTo(0)
        session.enemyTracker.onFastMove(0, count = 7) // 56 energy

        val before = session.enemyTracker.chargedMoveProgress(0)
            .first { it.move.moveId == "THUNDERBOLT" }
        assertEquals(0, before.fastMovesNeeded)

        val processor = BattleObservationProcessor(
            session = session,
            repository = repo,
            moveAliases = mapOf("Relâmpago" to "THUNDERBOLT")
        )

        val result = processor.onEnemyChargedMoveBanner(
            rawText = "Plusle usou Relâmpago!",
            timestampMs = 10_000L
        )

        assertTrue(result.applied)
        assertEquals("THUNDERBOLT", result.moveId)
        assertEquals(56, result.energyBefore)
        assertEquals(1, result.energyAfter)
        val thunderbolt = result.progressAfter.first { it.move.moveId == "THUNDERBOLT" }
        assertEquals(7, thunderbolt.fastMovesNeeded)
    }

    @Test
    fun repeatedOcrFramesDoNotSpendSameChargedMoveTwice() {
        val session = BattleSession(repo)
        session.enemyTracker.identifyPokemon(0, "plusle", "QUICK_ATTACK")
        session.enemyTracker.setChargedMoves(0, listOf("THUNDERBOLT", "DISCHARGE"))
        session.enemyTracker.switchTo(0)
        session.enemyTracker.onFastMove(0, count = 13) // capped at 100

        val processor = BattleObservationProcessor(
            session = session,
            repository = repo,
            moveAliases = mapOf("Relâmpago" to "THUNDERBOLT")
        )

        val first = processor.onEnemyChargedMoveBanner(
            "Plusle usou Relâmpago!",
            20_000L
        )
        val duplicate = processor.onEnemyChargedMoveBanner(
            "Plusle usou Relâmpago!",
            20_800L
        )

        assertTrue(first.applied)
        assertEquals(45, session.enemyTracker.state(0).energy)
        assertTrue(!duplicate.applied)
        assertTrue(duplicate.duplicate)
        assertEquals(45, session.enemyTracker.state(0).energy)
    }

    @Test
    fun fuzzyAliasHandlesMinorOcrError() {
        val session = BattleSession(repo)
        session.enemyTracker.identifyPokemon(0, "plusle", "QUICK_ATTACK")
        session.enemyTracker.switchTo(0)
        session.enemyTracker.onFastMove(0, count = 7)

        val processor = BattleObservationProcessor(
            session = session,
            repository = repo,
            moveAliases = mapOf("Relâmpago" to "THUNDERBOLT")
        )

        val result = processor.onEnemyChargedMoveBanner(
            rawText = "Plusle usou Relampag0!",
            timestampMs = 30_000L
        )

        assertTrue(result.applied)
        assertEquals("THUNDERBOLT", result.moveId)
    }
}
