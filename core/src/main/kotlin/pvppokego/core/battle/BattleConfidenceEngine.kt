package pvppokego.core.battle

import pvppokego.core.model.EnemyPokemonState
import pvppokego.core.model.EvidenceLevel

data class ConfidenceValue<T>(
    val value: T?,
    val evidence: EvidenceLevel,
    val score: Double
)

data class ChargedMoveConfidence(
    val moveId: String,
    val evidence: EvidenceLevel,
    val score: Double
)

data class EnemyConfidenceSnapshot(
    val slot: Int,
    val species: ConfidenceValue<String>,
    val fastMove: ConfidenceValue<String>,
    val chargedMoves: List<ChargedMoveConfidence>,
    val overallScore: Double
)

/**
 * Converts the battle state into user-facing confidence information.
 *
 * The overlay should distinguish predictions from observations instead of
 * presenting every field as equally certain.
 */
class BattleConfidenceEngine {
    fun snapshot(state: EnemyPokemonState): EnemyConfidenceSnapshot {
        val species = ConfidenceValue(
            value = state.speciesId,
            evidence = state.speciesEvidence,
            score = score(state.speciesEvidence, state.speciesId != null)
        )
        val fast = ConfidenceValue(
            value = state.fastMoveId,
            evidence = state.fastMoveEvidence,
            score = score(state.fastMoveEvidence, state.fastMoveId != null)
        )
        val charged = state.selectedChargedMoveIds.map { moveId ->
            val evidence = if (moveId in state.confirmedChargedMoveIds) {
                EvidenceLevel.CONFIRMED
            } else {
                EvidenceLevel.PREDICTED
            }
            ChargedMoveConfidence(moveId, evidence, score(evidence, true))
        }

        val scores = buildList {
            if (species.value != null) add(species.score)
            if (fast.value != null) add(fast.score)
            addAll(charged.map(ChargedMoveConfidence::score))
        }
        val overall = if (scores.isEmpty()) 0.0 else scores.average()

        return EnemyConfidenceSnapshot(
            slot = state.slot,
            species = species,
            fastMove = fast,
            chargedMoves = charged,
            overallScore = overall
        )
    }

    private fun score(level: EvidenceLevel, present: Boolean): Double {
        if (!present) return 0.0
        return when (level) {
            EvidenceLevel.PREDICTED -> 0.45
            EvidenceLevel.PROBABLE -> 0.75
            EvidenceLevel.CONFIRMED -> 1.0
        }
    }
}
