package com.lucianotoscano.pvppokego.engine

import android.graphics.Color
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MatchupState

/**
 * Read-only PvP copilot. It recommends the next action but never sends input to Pokémon GO.
 */
object BattleAssistAdvisor {
    data class Advice(
        val title: String,
        val detail: String,
        val color: Int,
        val priority: Int
    )

    fun advise(state: BattleUiState, repo: GameDataRepository): Advice? {
        val playerTypes = repo.pokemon(state.playerName)?.types.orEmpty()
        val charged = listOfNotNull(state.charged1, state.charged2)

        if (state.chargedIncoming && playerTypes.isNotEmpty() && charged.isNotEmpty()) {
            val plausible = charged.filter { it.ready || it.progress >= .72f }.ifEmpty { charged }
            val threat = plausible.maxByOrNull {
                it.move.power.toDouble() * TypeChart.multiplier(it.move.type, playerTypes)
            }
            if (threat != null) {
                val eff = TypeChart.multiplier(threat.move.type, playerTypes)
                val score = threat.move.power.toDouble() * eff
                return when {
                    eff >= 1.5 || score >= 100.0 -> Advice(
                        "ESCUDO",
                        "${shortName(threat.move.name)} • ${if (eff > 1.01) "SUPEREFICAZ • " else ""}ameaça alta",
                        Color.rgb(232, 61, 72),
                        100
                    )
                    eff <= .65 && score < 70.0 -> Advice(
                        "NÃO ESCUDE",
                        "${shortName(threat.move.name)} • pouco eficaz",
                        Color.rgb(46, 188, 105),
                        100
                    )
                    else -> Advice(
                        "ESCUDO OPCIONAL",
                        "${shortName(threat.move.name)} • dano moderado",
                        Color.rgb(245, 175, 52),
                        95
                    )
                }
            }
        }

        val visibleReserves = state.reserves.filter { it.visible }
        val bestReserve = visibleReserves.firstOrNull { it.matchup == MatchupState.FAVORABLE }
        val neutralReserve = visibleReserves.firstOrNull { it.matchup == MatchupState.NEUTRAL }

        if (state.playerMatchup == MatchupState.UNFAVORABLE && bestReserve != null) {
            return Advice(
                "TROQUE",
                bestReserve.name?.let { "Melhor opção: $it" } ?: "Há uma reserva favorável",
                Color.rgb(232, 61, 72),
                90
            )
        }

        val enemyReady = charged.filter { it.ready }
        if (enemyReady.isNotEmpty()) {
            val cheapest = enemyReady.minByOrNull { it.energyCost }
            return Advice(
                "ATENÇÃO",
                cheapest?.let { "Inimigo já pode usar ${shortName(it.move.name)}" } ?: "Charged inimigo disponível",
                Color.rgb(245, 175, 52),
                75
            )
        }

        if (state.playerMatchup == MatchupState.UNFAVORABLE && neutralReserve != null) {
            return Advice(
                "PREPARE A TROCA",
                neutralReserve.name?.let { "$it melhora o matchup" } ?: "Reserva neutra disponível",
                Color.rgb(245, 175, 52),
                65
            )
        }

        return when (state.playerMatchup) {
            MatchupState.FAVORABLE -> Advice(
                "CONTINUE FAST",
                "Matchup favorável • mantenha pressão",
                Color.rgb(46, 188, 105),
                35
            )
            MatchupState.NEUTRAL -> Advice(
                "CONTINUE FAST",
                "Matchup neutro • acompanhe energia",
                Color.rgb(110, 170, 235),
                30
            )
            MatchupState.UNFAVORABLE -> Advice(
                "CUIDADO",
                "Matchup desfavorável • preserve recursos",
                Color.rgb(245, 175, 52),
                40
            )
            MatchupState.UNKNOWN -> null
        }
    }

    private fun shortName(name: String): String = if (name.length <= 18) name else name.take(17) + "…"
}
