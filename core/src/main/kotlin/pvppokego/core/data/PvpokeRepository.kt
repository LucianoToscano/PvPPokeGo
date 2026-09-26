package pvppokego.core.data

import pvppokego.core.model.MoveData
import pvppokego.core.model.PokemonData
import pvppokego.core.model.RecommendedMoveset

interface PvpokeRepository {
    fun pokemon(speciesId: String): PokemonData?
    fun move(moveId: String): MoveData?
    fun recommendedMovesets(speciesId: String, leagueCp: Int): List<RecommendedMoveset>
}

class InMemoryPvpokeRepository(
    private val pokemonById: Map<String, PokemonData>,
    private val movesById: Map<String, MoveData>,
    private val rankings: List<RecommendedMoveset> = emptyList()
) : PvpokeRepository {

    override fun pokemon(speciesId: String): PokemonData? =
        pokemonById[speciesId.normalizedId()]

    override fun move(moveId: String): MoveData? =
        movesById[moveId.normalizedMoveId()]

    override fun recommendedMovesets(
        speciesId: String,
        leagueCp: Int
    ): List<RecommendedMoveset> {
        val normalized = speciesId.normalizedId()
        return rankings
            .asSequence()
            .filter { it.leagueCp == leagueCp && it.speciesId.normalizedId() == normalized }
            .sortedBy { it.rank }
            .toList()
    }

    companion object {
        /**
         * Loads the compact TSV format produced by tools/update_pvpoke_data.py.
         *
         * The Android layer can read these files from assets and pass their
         * text directly here, keeping JSON details isolated from battle logic.
         */
        fun fromTsv(
            pokemonTsv: String,
            movesTsv: String,
            rankingsTsv: String? = null
        ): InMemoryPvpokeRepository {
            val moves = parseMoves(movesTsv)
            val pokemon = parsePokemon(pokemonTsv)
            val rankings = rankingsTsv?.let(::parseRankings).orEmpty()
            return InMemoryPvpokeRepository(pokemon, moves, rankings)
        }

        private fun parseMoves(tsv: String): Map<String, MoveData> =
            tsv.lineSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val c = line.split('\t')
                    if (c.size < 8) return@mapNotNull null
                    val move = MoveData(
                        moveId = c[0].normalizedMoveId(),
                        name = c[1],
                        type = c[2].lowercase(),
                        power = c[3].toIntOrNull() ?: 0,
                        energyGain = c[4].toIntOrNull() ?: 0,
                        energyCost = c[5].toIntOrNull() ?: 0,
                        cooldownMs = c[6].toIntOrNull() ?: 0,
                        turns = c[7].toIntOrNull() ?: 0
                    )
                    move.moveId to move
                }
                .toMap()

        private fun parsePokemon(tsv: String): Map<String, PokemonData> =
            tsv.lineSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val c = line.split('\t')
                    if (c.size < 5) return@mapNotNull null
                    val pokemon = PokemonData(
                        speciesId = c[0].normalizedId(),
                        speciesName = c[1],
                        types = splitPipe(c[2]).map(String::lowercase),
                        fastMoveIds = splitPipe(c[3]).map(String::normalizedMoveId).toSet(),
                        chargedMoveIds = splitPipe(c[4]).map(String::normalizedMoveId).toSet()
                    )
                    pokemon.speciesId to pokemon
                }
                .toMap()

        private fun parseRankings(tsv: String): List<RecommendedMoveset> =
            tsv.lineSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val c = line.split('\t')
                    if (c.size < 6) return@mapNotNull null
                    RecommendedMoveset(
                        leagueCp = c[0].toIntOrNull() ?: return@mapNotNull null,
                        rank = c[1].toIntOrNull() ?: Int.MAX_VALUE,
                        speciesId = c[2].normalizedId(),
                        fastMoveId = c[3].takeIf(String::isNotBlank)?.normalizedMoveId(),
                        chargedMoveIds = splitPipe(c[4]).map(String::normalizedMoveId),
                        score = c[5].toDoubleOrNull()
                    )
                }
                .toList()

        private fun splitPipe(value: String): List<String> =
            value.split('|').filter(String::isNotBlank)
    }
}

private fun String.normalizedId(): String = trim().lowercase()

private fun String.normalizedMoveId(): String =
    trim().uppercase().replace(' ', '_')
