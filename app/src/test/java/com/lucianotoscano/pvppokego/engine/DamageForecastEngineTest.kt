package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.EstimatedBattleStats
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.PokemonBaseStats
import com.lucianotoscano.pvppokego.data.PokemonDef
import org.junit.Assert.assertTrue
import org.junit.Test

class DamageForecastEngineTest {
    private val fireAttacker = PokemonDef(
        speciesId = "test_fire",
        speciesName = "Test Fire",
        types = listOf("fire"),
        baseStats = PokemonBaseStats(200, 150, 150)
    )
    private val grassDefender = PokemonDef(
        speciesId = "test_grass",
        speciesName = "Test Grass",
        types = listOf("grass"),
        baseStats = PokemonBaseStats(150, 180, 180)
    )
    private val neutralDefender = grassDefender.copy(
        speciesId = "test_water",
        speciesName = "Test Water",
        types = listOf("water")
    )
    private val statsA = EstimatedBattleStats(140.0, 120.0, 130, 20.0, 0, 0, 0, 1490)
    private val statsD = EstimatedBattleStats(120.0, 135.0, 150, 20.0, 0, 0, 0, 1490)
    private val fireMove = MoveDef(
        moveId = "TEST_FIRE",
        name = "Test Fire",
        type = "fire",
        power = 90,
        energyGain = 0,
        energyDelta = -50,
        turns = 1
    )

    @Test
    fun superEffectiveStabForecastIsHigherThanResistedForecast() {
        val superEffective = DamageForecastEngine.modelDamagePercent(
            fireAttacker, statsA, grassDefender, statsD, fireMove
        )!!
        val resisted = DamageForecastEngine.modelDamagePercent(
            fireAttacker, statsA, neutralDefender, statsD, fireMove
        )!!
        assertTrue(superEffective > resisted)
        assertTrue(superEffective > 0f)
    }
}
