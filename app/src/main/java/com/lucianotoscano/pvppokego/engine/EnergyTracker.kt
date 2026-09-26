package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.MoveDef
import kotlin.math.ceil

/** Pure PvP energy math. No OCR/timer assumptions live here. */
class EnergyTracker(
    initialEnergy: Int = 0,
    private val maxEnergy: Int = 100
) {
    var energy: Int = initialEnergy.coerceIn(0, maxEnergy)
        private set
    var fastMoveCount: Int = 0
        private set

    fun reset() {
        energy = 0
        fastMoveCount = 0
    }

    fun restore(energy: Int, count: Int) {
        this.energy = energy.coerceIn(0, maxEnergy)
        fastMoveCount = count.coerceAtLeast(0)
    }

    fun onFastMove(fast: MoveDef, count: Int = 1) {
        if (count <= 0) return
        val gain = fast.energyGain.coerceAtLeast(0)
        energy = (energy + gain * count).coerceAtMost(maxEnergy)
        fastMoveCount += count
    }

    fun undoFastMove(fast: MoveDef, count: Int = 1) {
        if (count <= 0) return
        val gain = fast.energyGain.coerceAtLeast(0)
        energy = (energy - gain * count).coerceAtLeast(0)
        fastMoveCount = (fastMoveCount - count).coerceAtLeast(0)
    }

    /** Strict/manual confirmation: only spends energy that is actually tracked. */
    fun onChargedMove(charged: MoveDef): Boolean {
        val cost = charged.chargedCost.coerceAtLeast(0)
        if (energy < cost) return false
        energy = (energy - cost).coerceAtLeast(0)
        return true
    }

    /**
     * Visual confirmation means the move definitely happened. Preserve residual energy when our
     * estimate is sufficient; if the visual counter missed Fast Moves, use 0 as the conservative
     * residual instead of refusing to reset forever.
     */
    fun onObservedChargedMove(charged: MoveDef): Boolean {
        val cost = charged.chargedCost.coerceAtLeast(0)
        energy = if (energy >= cost) energy - cost else 0
        return true
    }

    fun predict(charged: MoveDef, fast: MoveDef): ChargedPrediction {
        val cost = charged.chargedCost.coerceAtLeast(1)
        val gain = fast.energyGain.coerceAtLeast(1)
        val missing = (cost - energy).coerceAtLeast(0)
        val remaining = if (missing == 0) 0 else ceil(missing.toDouble() / gain).toInt()
        return ChargedPrediction(
            move = charged,
            fastMovesRemaining = remaining,
            progress = (energy.toFloat() / cost.toFloat()).coerceIn(0f, 1f),
            ready = energy >= cost
        )
    }
}
