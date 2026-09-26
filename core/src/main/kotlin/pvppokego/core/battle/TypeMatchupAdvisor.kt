package pvppokego.core.battle

import pvppokego.core.data.PvpokeRepository

data class TeamMatchupRecommendation(
    val slot: Int,
    val speciesId: String,
    val bestStabMultiplier: Double,
    val worstIncomingStabMultiplier: Double,
    val typeScore: Double
)

/**
 * Pokemon GO type effectiveness helper.
 *
 * This is intentionally separate from PvPoke parsing. PvPoke supplies species
 * types; this class turns those types into a lightweight pre-battle/switch
 * recommendation. Move-specific simulation can be layered on top later.
 */
object PokemonGoTypeChart {
    const val SUPER_EFFECTIVE = 1.6
    const val RESISTED = 0.625
    const val IMMUNE = 0.390625

    private val superEffectiveAgainst = mapOf(
        "fire" to setOf("grass", "ice", "bug", "steel"),
        "water" to setOf("fire", "ground", "rock"),
        "electric" to setOf("water", "flying"),
        "grass" to setOf("water", "ground", "rock"),
        "ice" to setOf("grass", "ground", "flying", "dragon"),
        "fighting" to setOf("normal", "ice", "rock", "dark", "steel"),
        "poison" to setOf("grass", "fairy"),
        "ground" to setOf("fire", "electric", "poison", "rock", "steel"),
        "flying" to setOf("grass", "fighting", "bug"),
        "psychic" to setOf("fighting", "poison"),
        "bug" to setOf("grass", "psychic", "dark"),
        "rock" to setOf("fire", "ice", "flying", "bug"),
        "ghost" to setOf("psychic", "ghost"),
        "dragon" to setOf("dragon"),
        "dark" to setOf("psychic", "ghost"),
        "steel" to setOf("ice", "rock", "fairy"),
        "fairy" to setOf("fighting", "dragon", "dark")
    )

    private val resistedBy = mapOf(
        "normal" to setOf("rock", "steel"),
        "fire" to setOf("fire", "water", "rock", "dragon"),
        "water" to setOf("water", "grass", "dragon"),
        "electric" to setOf("electric", "grass", "dragon"),
        "grass" to setOf("fire", "grass", "poison", "flying", "bug", "dragon", "steel"),
        "ice" to setOf("fire", "water", "ice", "steel"),
        "fighting" to setOf("poison", "flying", "psychic", "bug", "fairy"),
        "poison" to setOf("poison", "ground", "rock", "ghost"),
        "ground" to setOf("grass", "bug"),
        "flying" to setOf("electric", "rock", "steel"),
        "psychic" to setOf("psychic", "steel"),
        "bug" to setOf("fire", "fighting", "poison", "flying", "ghost", "steel", "fairy"),
        "rock" to setOf("fighting", "ground", "steel"),
        "ghost" to setOf("dark"),
        "dragon" to setOf("steel"),
        "dark" to setOf("fighting", "dark", "fairy"),
        "steel" to setOf("fire", "water", "electric", "steel"),
        "fairy" to setOf("fire", "poison", "steel")
    )

    private val immuneBy = mapOf(
        "normal" to setOf("ghost"),
        "electric" to setOf("ground"),
        "fighting" to setOf("ghost"),
        "poison" to setOf("steel"),
        "ground" to setOf("flying"),
        "psychic" to setOf("dark"),
        "ghost" to setOf("normal"),
        "dragon" to setOf("fairy")
    )

    fun multiplier(attackingType: String, defenderTypes: List<String>): Double {
        val attack = attackingType.lowercase()
        return defenderTypes.fold(1.0) { multiplier, defender ->
            val defense = defender.lowercase()
            multiplier * when {
                immuneBy[attack]?.contains(defense) == true -> IMMUNE
                superEffectiveAgainst[attack]?.contains(defense) == true -> SUPER_EFFECTIVE
                resistedBy[attack]?.contains(defense) == true -> RESISTED
                else -> 1.0
            }
        }
    }
}

class TypeMatchupAdvisor(
    private val repository: PvpokeRepository
) {
    /**
     * Ranks the player's registered species by type-only advantage against an
     * identified enemy. It does not perform taps or automatic switches.
     */
    fun rankMyTeam(
        enemySpeciesId: String,
        myTeamSpeciesIds: List<String?>
    ): List<TeamMatchupRecommendation> {
        val enemy = repository.pokemon(enemySpeciesId) ?: return emptyList()

        return myTeamSpeciesIds.mapIndexedNotNull { slot, speciesId ->
            val mine = speciesId?.let(repository::pokemon) ?: return@mapIndexedNotNull null

            val bestOffense = mine.types
                .maxOfOrNull { PokemonGoTypeChart.multiplier(it, enemy.types) }
                ?: 1.0

            val worstIncoming = enemy.types
                .maxOfOrNull { PokemonGoTypeChart.multiplier(it, mine.types) }
                ?: 1.0

            TeamMatchupRecommendation(
                slot = slot,
                speciesId = mine.speciesId,
                bestStabMultiplier = bestOffense,
                worstIncomingStabMultiplier = worstIncoming,
                typeScore = bestOffense / worstIncoming
            )
        }.sortedByDescending { it.typeScore }
    }
}
