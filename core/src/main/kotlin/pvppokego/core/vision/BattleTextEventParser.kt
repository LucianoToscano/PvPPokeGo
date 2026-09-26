package pvppokego.core.vision

import java.text.Normalizer
import kotlin.math.max
import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.MoveData

/**
 * Normalized result of the battle banner shown by Pokemon GO after a charged
 * attack (for example: "Plusle usou Relâmpago!").
 */
data class ChargedMoveBannerMatch(
    val speciesId: String,
    val moveId: String,
    val rawText: String,
    val normalizedMoveText: String,
    val confidence: Double
)

/**
 * Parses OCR text without depending on Android OCR APIs.
 *
 * PvPoke stores English move names, while the game banner is localized. The
 * optional [moveAliases] map accepts localized aliases (alias -> move id), so
 * the Android layer can load pt-BR names from an asset without changing battle
 * logic.
 */
class BattleTextEventParser(
    private val repository: PvpokeRepository,
    moveAliases: Map<String, String> = emptyMap()
) {
    private val normalizedAliases: Map<String, String> = moveAliases
        .mapKeys { (alias, _) -> normalizeForOcr(alias) }
        .mapValues { (_, moveId) -> normalizeMoveId(moveId) }

    fun parseChargedMoveBanner(
        rawText: String,
        activeEnemySpeciesId: String
    ): ChargedMoveBannerMatch? {
        val pokemon = repository.pokemon(activeEnemySpeciesId) ?: return null
        val line = normalizeForOcr(rawText)
        val moveText = extractMoveText(line) ?: return null
        if (moveText.isBlank()) return null

        val candidateMoves = pokemon.chargedMoveIds
            .mapNotNull(repository::move)
            .filter(MoveData::isChargedMove)
        if (candidateMoves.isEmpty()) return null

        resolveAlias(moveText, candidateMoves)?.let { move ->
            return ChargedMoveBannerMatch(
                speciesId = pokemon.speciesId,
                moveId = move.moveId,
                rawText = rawText,
                normalizedMoveText = moveText,
                confidence = 1.0
            )
        }

        val scored = candidateMoves
            .map { move ->
                val names = listOf(
                    normalizeForOcr(move.name),
                    normalizeForOcr(move.moveId.replace('_', ' '))
                ).filter(String::isNotBlank)

                val score = names.maxOfOrNull { candidate ->
                    when {
                        moveText == candidate -> 1.0
                        moveText.contains(candidate) || candidate.contains(moveText) -> 0.94
                        else -> similarity(moveText, candidate)
                    }
                } ?: 0.0
                move to score
            }
            .maxByOrNull { it.second }
            ?: return null

        if (scored.second < FUZZY_MATCH_THRESHOLD) return null

        return ChargedMoveBannerMatch(
            speciesId = pokemon.speciesId,
            moveId = scored.first.moveId,
            rawText = rawText,
            normalizedMoveText = moveText,
            confidence = scored.second
        )
    }

    private fun resolveAlias(moveText: String, candidates: List<MoveData>): MoveData? {
        val candidateIds = candidates.associateBy(MoveData::moveId)
        val exact = normalizedAliases[moveText]?.let(candidateIds::get)
        if (exact != null) return exact

        val fuzzyAlias = normalizedAliases.entries
            .asSequence()
            .map { entry -> entry to similarity(moveText, entry.key) }
            .filter { (_, score) -> score >= FUZZY_ALIAS_THRESHOLD }
            .maxByOrNull { (_, score) -> score }
            ?.first
            ?: return null

        return candidateIds[fuzzyAlias.value]
    }

    private fun extractMoveText(normalizedLine: String): String? {
        val padded = " $normalizedLine "
        val portugueseMarker = " usou "
        val englishMarker = " used "
        return when {
            portugueseMarker in padded -> padded.substringAfter(portugueseMarker).trim()
            englishMarker in padded -> padded.substringAfter(englishMarker).trim()
            else -> null
        }
    }

    private fun similarity(left: String, right: String): Double {
        if (left == right) return 1.0
        if (left.isBlank() || right.isBlank()) return 0.0
        val distance = levenshtein(left, right)
        return 1.0 - (distance.toDouble() / max(left.length, right.length))
    }

    private fun levenshtein(left: String, right: String): Int {
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)

        for (i in left.indices) {
            current[0] = i + 1
            for (j in right.indices) {
                val substitutionCost = if (left[i] == right[j]) 0 else 1
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + substitutionCost
                )
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[right.length]
    }

    companion object {
        private const val FUZZY_MATCH_THRESHOLD = 0.82
        private const val FUZZY_ALIAS_THRESHOLD = 0.86
    }
}

internal fun normalizeForOcr(value: String): String {
    val withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return withoutAccents
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun normalizeMoveId(value: String): String =
    value.trim().uppercase().replace(' ', '_')
