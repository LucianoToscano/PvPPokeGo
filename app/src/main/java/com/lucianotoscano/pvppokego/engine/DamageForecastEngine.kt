package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.DamageForecast
import com.lucianotoscano.pvppokego.data.DamageForecastConfidence
import com.lucianotoscano.pvppokego.data.EstimatedBattleStats
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.ObservedDamageEstimate
import com.lucianotoscano.pvppokego.data.PokemonDef
import kotlin.math.floor

/**
 * Read-only damage forecast based on PvPoke's battle formula and the PvPoke snapshot
 * already packaged in the APK. Exact IVs/levels are not visible in battle, so model
 * forecasts are deliberately emitted as ranges. Real observed damage always wins.
 */
internal object DamageForecastEngine {
    private const val PVP_BONUS = 1.2999999523162842
    private const val STAB = 1.2000000476837158
    private const val SHADOW_ATK = 1.2
    private const val SHADOW_DEF = 0.83333331

    fun forecast(
        repo: GameDataRepository,
        attackerId: String?,
        attackerCp: Int?,
        defenderId: String?,
        defenderCp: Int?,
        move: MoveDef,
        observed: ObservedDamageEstimate?,
        currentHpRatio: Float?
    ): DamageForecast? {
        observed?.let { value ->
            val confidence = if (value.samples >= 5) {
                DamageForecastConfidence.OBSERVED_STABLE
            } else {
                DamageForecastConfidence.OBSERVED_ESTIMATED
            }
            return DamageForecast(
                moveName = move.name,
                averagePercent = value.averagePercent,
                minPercent = value.minPercent,
                maxPercent = value.maxPercent,
                projectedRemainingPercent = currentHpRatio?.let {
                    (it * 100f - value.averagePercent).coerceIn(0f, 100f)
                },
                source = "dano observado",
                confidence = confidence
            )
        }

        val attacker = repo.pokemon(attackerId) ?: return null
        val defender = repo.pokemon(defenderId) ?: return null
        val attackerStats = repo.estimatedBattleStats(attacker.speciesId, attackerCp) ?: return null
        val defenderStats = repo.estimatedBattleStats(defender.speciesId, defenderCp) ?: return null
        val average = modelDamagePercent(attacker, attackerStats, defender, defenderStats, move)
            ?: return null

        // CP is visible but IVs/level are not. Keep the first model forecast wide enough
        // that the HUD communicates risk without pretending it knows exact hidden stats.
        val relativeUncertainty = when {
            attackerCp != null && defenderCp != null -> 0.14f
            attackerCp != null || defenderCp != null -> 0.20f
            else -> 0.26f
        }
        val min = (average * (1f - relativeUncertainty)).coerceIn(0f, 100f)
        val max = (average * (1f + relativeUncertainty)).coerceIn(min, 100f)
        return DamageForecast(
            moveName = move.name,
            averagePercent = average,
            minPercent = min,
            maxPercent = max,
            projectedRemainingPercent = currentHpRatio?.let {
                (it * 100f - average).coerceIn(0f, 100f)
            },
            source = "modelo PvPoke",
            confidence = DamageForecastConfidence.MODEL
        )
    }

    internal fun modelDamagePercent(
        attacker: PokemonDef,
        attackerStats: EstimatedBattleStats,
        defender: PokemonDef,
        defenderStats: EstimatedBattleStats,
        move: MoveDef
    ): Float? {
        if (move.power <= 0 || attackerStats.attack <= 0.0 || defenderStats.defense <= 0.0 || defenderStats.hp <= 0) {
            return null
        }
        val stab = if (attacker.types.any { it.equals(move.type, ignoreCase = true) }) STAB else 1.0
        val effectiveness = TypeChart.multiplier(move.type, defender.types)
        val attack = attackerStats.attack * if (attacker.tags.any { it.equals("shadow", true) }) SHADOW_ATK else 1.0
        val defense = defenderStats.defense * if (defender.tags.any { it.equals("shadow", true) }) SHADOW_DEF else 1.0

        val damage = floor(
            move.power * stab * (attack / defense) * effectiveness * 0.5 * PVP_BONUS
        ).toInt() + 1
        return (damage.toFloat() / defenderStats.hp.toFloat() * 100f).coerceIn(0f, 100f)
    }
}
