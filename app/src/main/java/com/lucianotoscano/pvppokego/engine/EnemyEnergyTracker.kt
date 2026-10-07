package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.EnemyEnergyHypothesisSnapshot
import com.lucianotoscano.pvppokego.data.EnemyEnergySnapshot
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.WeightedMove
import kotlin.math.abs
import kotlin.math.exp

/**
 * Discrete enemy-energy hypothesis tracker.
 *
 * Each hypothesis owns a legal Fast Move and exact residual energy. Unknown Fast/Charged
 * choices branch instead of collapsing to a guessed number. The state is small (normally
 * 1-4 Fast Moves) and is pruned/merged after every event.
 */
internal class EnemyEnergyTracker(
    private val maxEnergy: Int = 100,
    private val maxHypotheses: Int = 32
) {
    data class Hypothesis(
        val fastMove: MoveDef,
        val energy: Int,
        val completedFastMoves: Int,
        val elapsedTurns: Int,
        val weight: Float
    )

    private var hypotheses: List<Hypothesis> = emptyList()
    private var observedFastEvents: Int = 0
    private var lastObservedTurn: Int = 0
    private var lastFastEventId: Long? = null

    val isInitialized: Boolean get() = hypotheses.isNotEmpty()

    fun reset(candidates: List<WeightedMove>, snapshot: EnemyEnergySnapshot? = null) {
        observedFastEvents = snapshot?.observedFastEvents ?: 0
        lastObservedTurn = snapshot?.lastObservedTurn ?: 0
        lastFastEventId = null
        hypotheses = restoreCandidates(candidates, snapshot)
        normalizeAndPrune()
    }

    fun clear() {
        hypotheses = emptyList()
        observedFastEvents = 0
        lastObservedTurn = 0
        lastFastEventId = null
    }

    fun confirmFastMove(move: MoveDef) {
        val matching = hypotheses.filter { it.fastMove.moveId == move.moveId }
        hypotheses = if (matching.isNotEmpty()) {
            matching
        } else {
            val likelyEnergy = weightedMedianEnergy(hypotheses)
            listOf(
                Hypothesis(
                    fastMove = move,
                    energy = likelyEnergy,
                    completedFastMoves = hypotheses.maxOfOrNull { it.completedFastMoves } ?: 0,
                    elapsedTurns = hypotheses.maxOfOrNull { it.elapsedTurns } ?: 0,
                    weight = 1f
                )
            )
        }
        normalizeAndPrune()
    }

    /**
     * Register completed Fast Moves, never raw taps. eventId makes duplicate delivery
     * idempotent even if a detector/service callback is repeated.
     */
    fun observeFastCompletion(
        count: Int,
        eventId: Long,
        confidence: Float,
        observedIntervalMs: Long? = null
    ): Boolean {
        if (count <= 0 || hypotheses.isEmpty()) return false
        if (lastFastEventId == eventId) return false
        lastFastEventId = eventId

        val boundedConfidence = confidence.coerceIn(0.05f, 1f)
        hypotheses = hypotheses.map { h ->
            val gain = h.fastMove.energyGain.coerceAtLeast(0) * count
            val expectedMs = (h.fastMove.turns * TURN_MS * count).toLong()
            val cadenceLikelihood = observedIntervalMs
                ?.takeIf { it in 150L..12_000L && expectedMs > 0L }
                ?.let { observed ->
                    val relativeError = abs(observed - expectedMs).toDouble() / expectedMs.toDouble()
                    exp(-2.4 * relativeError).toFloat().coerceIn(0.04f, 1f)
                }
                ?: 1f
            val evidenceWeight = (
                (1f - boundedConfidence) + boundedConfidence * cadenceLikelihood
            ).coerceIn(0.02f, 1f)
            h.copy(
                energy = (h.energy + gain).coerceAtMost(maxEnergy),
                completedFastMoves = h.completedFastMoves + count,
                elapsedTurns = h.elapsedTurns + h.fastMove.turns * count,
                weight = h.weight * evidenceWeight
            )
        }
        observedFastEvents++
        lastObservedTurn = hypotheses.maxOfOrNull { it.elapsedTurns } ?: lastObservedTurn
        normalizeAndPrune()
        return true
    }

    /**
     * Exact Charged identification removes impossible states and subtracts the cost only
     * from states that could legally pay it. If all hypotheses fail, preserve a degraded
     * recovery state at zero energy instead of fabricating pre-move energy.
     */
    fun observeCharged(move: MoveDef): Boolean {
        val cost = move.chargedCost.coerceAtLeast(1)
        val valid = hypotheses
            .filter { it.energy >= cost }
            .map { it.copy(energy = it.energy - cost) }
        hypotheses = if (valid.isNotEmpty()) {
            valid
        } else {
            hypotheses.map { it.copy(energy = 0, weight = it.weight * RECOVERY_WEIGHT) }
        }
        normalizeAndPrune()
        return hypotheses.isNotEmpty()
    }

    /**
     * Unknown Charged identification branches across every legal affordable cost.
     * Duplicate residual states are merged immediately to cap state growth.
     */
    fun observeUnknownCharged(candidates: List<MoveDef>): Boolean {
        val costs = candidates
            .map { it.chargedCost }
            .filter { it > 0 }
            .distinct()
        if (costs.isEmpty() || hypotheses.isEmpty()) return false

        val branched = buildList {
            hypotheses.forEach { h ->
                val affordable = costs.filter { it <= h.energy }
                if (affordable.isEmpty()) {
                    add(h.copy(energy = 0, weight = h.weight * RECOVERY_WEIGHT))
                } else {
                    val branchWeight = h.weight / affordable.size.toFloat()
                    affordable.forEach { cost ->
                        add(h.copy(energy = h.energy - cost, weight = branchWeight))
                    }
                }
            }
        }
        hypotheses = branched
        normalizeAndPrune()
        return hypotheses.isNotEmpty()
    }

    fun snapshot(): EnemyEnergySnapshot = EnemyEnergySnapshot(
        hypotheses = hypotheses.map {
            EnemyEnergyHypothesisSnapshot(
                fastMoveId = it.fastMove.moveId,
                energy = it.energy,
                completedFastMoves = it.completedFastMoves,
                elapsedTurns = it.elapsedTurns,
                weight = it.weight
            )
        },
        observedFastEvents = observedFastEvents,
        lastObservedTurn = lastObservedTurn
    )

    fun hypotheses(): List<Hypothesis> = hypotheses

    fun energyMin(): Int = hypotheses.minOfOrNull { it.energy } ?: 0
    fun energyMax(): Int = hypotheses.maxOfOrNull { it.energy } ?: 0
    fun energyLikely(): Int = weightedMedianEnergy(hypotheses)
    fun hypothesisCount(): Int = hypotheses.size

    fun confidence(): Float {
        if (hypotheses.isEmpty()) return 0f
        val top = hypotheses.maxOf { it.weight }
        val spreadPenalty = (1f / hypotheses.size.toFloat().coerceAtLeast(1f)).coerceIn(.18f, 1f)
        val evidence = (observedFastEvents / 5f).coerceIn(.35f, 1f)
        return (top * .65f + spreadPenalty * .15f + evidence * .20f).coerceIn(0f, 1f)
    }

    private fun restoreCandidates(
        candidates: List<WeightedMove>,
        snapshot: EnemyEnergySnapshot?
    ): List<Hypothesis> {
        val legal = candidates.filter { it.move.energyGain > 0 }.distinctBy { it.move.moveId }
        if (legal.isEmpty()) return emptyList()
        val byId = legal.associateBy { it.move.moveId }
        val restored = snapshot?.hypotheses.orEmpty().mapNotNull { saved ->
            val candidate = byId[saved.fastMoveId] ?: return@mapNotNull null
            Hypothesis(
                fastMove = candidate.move,
                energy = saved.energy.coerceIn(0, maxEnergy),
                completedFastMoves = saved.completedFastMoves.coerceAtLeast(0),
                elapsedTurns = saved.elapsedTurns.coerceAtLeast(0),
                weight = saved.weight.coerceAtLeast(MIN_WEIGHT)
            )
        }
        if (restored.isNotEmpty()) return restored
        return legal.map { candidate ->
            Hypothesis(
                fastMove = candidate.move,
                energy = 0,
                completedFastMoves = 0,
                elapsedTurns = 0,
                weight = candidate.weight.coerceAtLeast(MIN_WEIGHT)
            )
        }
    }

    private fun normalizeAndPrune() {
        if (hypotheses.isEmpty()) return
        val merged = hypotheses
            .groupBy { listOf(it.fastMove.moveId, it.energy.toString(), it.completedFastMoves.toString(), it.elapsedTurns.toString()).joinToString("|") }
            .map { (_, group) ->
                val first = group.first()
                first.copy(weight = group.sumOf { it.weight.toDouble() }.toFloat())
            }
            .sortedByDescending { it.weight }
            .take(maxHypotheses)
        val total = merged.sumOf { it.weight.toDouble() }.toFloat()
        hypotheses = if (total > 0f) {
            merged.map { it.copy(weight = (it.weight / total).coerceAtLeast(MIN_WEIGHT)) }
                .let { renormalize(it) }
        } else {
            val uniform = 1f / merged.size.toFloat()
            merged.map { it.copy(weight = uniform) }
        }
    }

    private fun renormalize(values: List<Hypothesis>): List<Hypothesis> {
        val total = values.sumOf { it.weight.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
        return values.map { it.copy(weight = it.weight / total) }
    }

    private fun weightedMedianEnergy(values: List<Hypothesis>): Int {
        if (values.isEmpty()) return 0
        var running = 0f
        values.sortedBy { it.energy }.forEach { h ->
            running += h.weight
            if (running >= .5f) return h.energy
        }
        return values.maxByOrNull { it.weight }?.energy ?: 0
    }

    companion object {
        const val TURN_MS = 500
        private const val MIN_WEIGHT = 0.0001f
        private const val RECOVERY_WEIGHT = 0.35f
    }
}
