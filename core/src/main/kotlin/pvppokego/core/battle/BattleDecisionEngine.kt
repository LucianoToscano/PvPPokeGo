package pvppokego.core.battle

import kotlin.math.max
import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.MoveData

enum class ChargedSelectionMode {
    FIRST_READY,
    TYPE_PRIORITY,
    SMART
}

enum class ShieldMode {
    OFF,
    NORMAL,
    SMART,
    SAFE
}

data class ChargedMoveCandidate(
    val buttonIndex: Int,
    val move: MoveData,
    val ready: Boolean,
    val energyMissing: Int,
    val typeMultiplier: Double,
    val stabMultiplier: Double,
    val effectivePower: Double,
    val damagePerEnergy: Double,
    val score: Double,
    val reasons: List<String>
)

data class ChargedMoveDecision(
    val mode: ChargedSelectionMode,
    val selected: ChargedMoveCandidate?,
    val candidates: List<ChargedMoveCandidate>
)

data class ShieldDecision(
    val mode: ShieldMode,
    val useShield: Boolean,
    val threateningMove: MoveData?,
    val threatScore: Double,
    val typeMultiplier: Double,
    val reason: String
)

/**
 * Pure battle decision logic. It never performs taps itself.
 *
 * The Android/overlay layer supplies the currently observed state and may use
 * the result either as a visual recommendation or as input to an automation
 * executor. Keeping this pure makes it testable and independent of screen
 * coordinates and AccessibilityService details.
 */
class BattleDecisionEngine(
    private val repository: PvpokeRepository
) {
    fun chooseChargedMove(
        attackerSpeciesId: String,
        chargedMoveIds: List<String>,
        currentEnergy: Int,
        defenderSpeciesId: String,
        mode: ChargedSelectionMode = ChargedSelectionMode.SMART,
        defenderHpFraction: Double = 1.0,
        defenderShieldsRemaining: Int = 0
    ): ChargedMoveDecision {
        val attacker = repository.pokemon(attackerSpeciesId)
            ?: return ChargedMoveDecision(mode, null, emptyList())
        val defender = repository.pokemon(defenderSpeciesId)
            ?: return ChargedMoveDecision(mode, null, emptyList())

        val hp = defenderHpFraction.coerceIn(0.0, 1.0)
        val candidates = chargedMoveIds.mapIndexedNotNull { index, moveId ->
            val move = repository.move(moveId) ?: return@mapIndexedNotNull null
            if (!move.isChargedMove) return@mapIndexedNotNull null

            val typeMultiplier = PokemonGoTypeChart.multiplier(move.type, defender.types)
            val stab = if (attacker.types.any { it.equals(move.type, ignoreCase = true) }) 1.2 else 1.0
            val effectivePower = move.power * typeMultiplier * stab
            val dpe = if (move.energyCost > 0) effectivePower / move.energyCost else 0.0
            val missing = max(0, move.energyCost - currentEnergy)
            val ready = missing == 0
            val reasons = mutableListOf<String>()

            if (typeMultiplier > 1.0) reasons += if (typeMultiplier >= 2.5) "double_super_effective" else "super_effective"
            if (typeMultiplier < 1.0) reasons += "resisted"
            if (stab > 1.0) reasons += "stab"
            if (ready) reasons += "ready" else reasons += "needs_${missing}_energy"

            val typeBonus = when {
                typeMultiplier >= 2.5 -> 70.0
                typeMultiplier >= PokemonGoTypeChart.SUPER_EFFECTIVE -> 40.0
                typeMultiplier < 0.5 -> -40.0
                typeMultiplier < 1.0 -> -22.0
                else -> 0.0
            }
            val readinessScore = if (ready) 35.0 else -(missing * 2.0)
            val baitBonus = if (defenderShieldsRemaining > 0) {
                (60 - move.energyCost).coerceAtLeast(0) * 0.35
            } else 0.0

            // Approximate finisher logic. This is intentionally a heuristic,
            // not a fake damage simulator; exact damage can be layered on later.
            val finisherTarget = hp * 150.0
            val finisherBonus = if (defenderShieldsRemaining == 0 && effectivePower >= finisherTarget) {
                28.0 - (move.energyCost * 0.10)
            } else 0.0
            if (finisherBonus > 0) reasons += "likely_finisher"
            if (baitBonus > 0) reasons += "shield_pressure"

            val smartScore =
                (effectivePower * 0.45) +
                    (dpe * 28.0) +
                    typeBonus +
                    readinessScore +
                    baitBonus +
                    finisherBonus

            ChargedMoveCandidate(
                buttonIndex = index,
                move = move,
                ready = ready,
                energyMissing = missing,
                typeMultiplier = typeMultiplier,
                stabMultiplier = stab,
                effectivePower = effectivePower,
                damagePerEnergy = dpe,
                score = smartScore,
                reasons = reasons
            )
        }

        val ready = candidates.filter { it.ready }
        val selected = when (mode) {
            ChargedSelectionMode.FIRST_READY -> ready.minByOrNull { it.buttonIndex }
            ChargedSelectionMode.TYPE_PRIORITY -> ready.sortedWith(
                compareByDescending<ChargedMoveCandidate> { it.typeMultiplier }
                    .thenByDescending { it.effectivePower }
                    .thenByDescending { it.damagePerEnergy }
                    .thenBy { it.move.energyCost }
            ).firstOrNull()
            ChargedSelectionMode.SMART -> ready.sortedWith(
                compareByDescending<ChargedMoveCandidate> { it.score }
                    .thenByDescending { it.typeMultiplier }
                    .thenByDescending { it.damagePerEnergy }
                    .thenBy { it.move.energyCost }
            ).firstOrNull()
        }

        return ChargedMoveDecision(
            mode = mode,
            selected = selected,
            candidates = candidates.sortedBy { it.buttonIndex }
        )
    }

    /**
     * Evaluates the charged moves that are currently plausible for the enemy.
     * The caller should preferably pass only moves compatible with the enemy's
     * tracked energy at that instant.
     */
    fun recommendShield(
        defenderSpeciesId: String,
        attackerSpeciesId: String,
        candidateChargedMoveIds: Collection<String>,
        mode: ShieldMode = ShieldMode.SMART,
        shieldsRemaining: Int,
        defenderHpFraction: Double
    ): ShieldDecision {
        if (mode == ShieldMode.OFF) {
            return ShieldDecision(mode, false, null, 0.0, 1.0, "shield_disabled")
        }
        if (shieldsRemaining <= 0) {
            return ShieldDecision(mode, false, null, 0.0, 1.0, "no_shields_remaining")
        }

        val defender = repository.pokemon(defenderSpeciesId)
            ?: return ShieldDecision(mode, false, null, 0.0, 1.0, "unknown_defender")
        val attacker = repository.pokemon(attackerSpeciesId)
            ?: return ShieldDecision(mode, false, null, 0.0, 1.0, "unknown_attacker")

        val hp = defenderHpFraction.coerceIn(0.01, 1.0)
        val threats = candidateChargedMoveIds.mapNotNull { moveId ->
            val move = repository.move(moveId) ?: return@mapNotNull null
            if (!move.isChargedMove) return@mapNotNull null
            val multiplier = PokemonGoTypeChart.multiplier(move.type, defender.types)
            val stab = if (attacker.types.any { it.equals(move.type, ignoreCase = true) }) 1.2 else 1.0
            val effectivePower = move.power * multiplier * stab
            Triple(move, multiplier, effectivePower)
        }

        val worst = threats.maxByOrNull { it.third }
            ?: return ShieldDecision(mode, false, null, 0.0, 1.0, "no_known_charged_move")

        val (move, multiplier, effectivePower) = worst
        val hpThreat = (effectivePower / 140.0).coerceAtMost(1.5)
        val likelyKo = hpThreat >= hp * 0.85
        val superEffective = multiplier > 1.0

        val shouldShield = when (mode) {
            ShieldMode.OFF -> false
            ShieldMode.NORMAL -> superEffective && effectivePower >= 55.0
            ShieldMode.SMART -> likelyKo || (superEffective && effectivePower >= 75.0)
            ShieldMode.SAFE -> likelyKo || superEffective || hpThreat >= hp * 0.55
        }

        val reason = when {
            !shouldShield -> "acceptable_risk"
            likelyKo -> "likely_ko"
            superEffective -> "super_effective_threat"
            else -> "high_damage_threat"
        }

        return ShieldDecision(
            mode = mode,
            useShield = shouldShield,
            threateningMove = move,
            threatScore = hpThreat,
            typeMultiplier = multiplier,
            reason = reason
        )
    }
}
