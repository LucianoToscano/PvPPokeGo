package com.lucianotoscano.pvppokego

import android.content.Context
import java.text.Normalizer
import pvppokego.core.battle.PokemonGoTypeChart
import pvppokego.core.data.InMemoryPvpokeRepository
import pvppokego.core.data.PvpokeRepository

class LocalBattleData(private val context: Context) {
    private val pokemonTsv = readAssetOrFallback("pvpoke/pokemon.tsv", FALLBACK_POKEMON)
    private val movesTsv = readAssetOrFallback("pvpoke/moves.tsv", FALLBACK_MOVES)
    private val rankingsTsv = readAssetOrNull("pvpoke/rankings.tsv")

    val repository: PvpokeRepository = InMemoryPvpokeRepository.fromTsv(
        pokemonTsv = pokemonTsv,
        movesTsv = movesTsv,
        rankingsTsv = rankingsTsv
    )

    val moveAliases: Map<String, String> = parseAliases(
        readAssetOrNull("localization/moves_pt_BR.tsv").orEmpty()
    )

    private val speciesNames: List<Pair<String, String>> = pokemonTsv
        .lineSequence()
        .drop(1)
        .mapNotNull { line ->
            val c = line.split('\t')
            if (c.size < 2) null else c[0] to c[1]
        }
        .sortedByDescending { (_, name) -> normalize(name).length }
        .toList()

    fun resolveSpeciesFromText(text: String): String? {
        val normalized = " ${normalize(text)} "
        if (normalized.isBlank()) return null
        return speciesNames.firstOrNull { (_, name) ->
            val candidate = normalize(name)
            candidate.length >= 3 && " $candidate " in normalized
        }?.first
    }

    fun displayName(speciesId: String?): String =
        speciesId?.let(repository::pokemon)?.speciesName ?: speciesId ?: "Inimigo"

    fun strongTypesAgainst(speciesId: String): List<String> {
        val pokemon = repository.pokemon(speciesId) ?: return emptyList()
        return TYPE_NAMES
            .map { type -> type to PokemonGoTypeChart.multiplier(type, pokemon.types) }
            .filter { (_, multiplier) -> multiplier > 1.0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    private fun parseAliases(tsv: String): Map<String, String> =
        tsv.lineSequence()
            .drop(1)
            .mapNotNull { line ->
                val c = line.split('\t')
                if (c.size < 3) null else c[1] to c[2]
            }
            .toMap()

    private fun readAssetOrNull(path: String): String? = try {
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (_: Exception) {
        null
    }

    private fun readAssetOrFallback(path: String, fallback: String): String =
        readAssetOrNull(path) ?: fallback

    private fun normalize(value: String): String {
        val noAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return noAccents
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    companion object {
        val TYPE_NAMES = listOf(
            "bug", "dark", "dragon", "electric", "fairy", "fighting",
            "fire", "flying", "ghost", "grass", "ground", "ice",
            "normal", "poison", "psychic", "rock", "steel", "water"
        )

        private val FALLBACK_MOVES = """
            move_id\tname\ttype\tpower\tenergy_gain\tenergy_cost\tcooldown_ms\tturns
            SPARK\tSpark\telectric\t4\t8\t0\t1000\t2
            VOLT_SWITCH\tVolt Switch\telectric\t12\t16\t0\t2000\t4
            QUICK_ATTACK\tQuick Attack\tnormal\t5\t8\t0\t1000\t2
            THUNDERBOLT\tThunderbolt\telectric\t90\t0\t55\t500\t1
            DISCHARGE\tDischarge\telectric\t65\t0\t45\t500\t1
            GRASS_KNOT\tGrass Knot\tgrass\t90\t0\t50\t500\t1
            FOUL_PLAY\tFoul Play\tdark\t70\t0\t45\t500\t1
            AQUA_TAIL\tAqua Tail\twater\t55\t0\t35\t500\t1
            DRAGON_PULSE\tDragon Pulse\tdragon\t90\t0\t60\t500\t1
            BODY_SLAM\tBody Slam\tnormal\t50\t0\t35\t500\t1
            EARTHQUAKE\tEarthquake\tground\t110\t0\t65\t500\t1
        """.trimIndent()

        private val FALLBACK_POKEMON = """
            species_id\tspecies_name\ttypes\tfast_moves\tcharged_moves
            plusle\tPlusle\telectric\tSPARK|QUICK_ATTACK\tTHUNDERBOLT|DISCHARGE|GRASS_KNOT
            electrode\tElectrode\telectric\tVOLT_SWITCH|SPARK\tDISCHARGE|FOUL_PLAY
            dragonair\tDragonair\tdragon\tQUICK_ATTACK\tAQUA_TAIL|DRAGON_PULSE
            snorlax\tSnorlax\tnormal\tQUICK_ATTACK\tBODY_SLAM|EARTHQUAKE
        """.trimIndent()
    }
}
