package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.MoveDef
import kotlin.math.ceil

class EnergyTracker(initialEnergy: Int = 0, private val maxEnergy: Int = 100) {
    var energy: Int = initialEnergy.coerceIn(0, maxEnergy)
        private set
    var fastMoveCount: Int = 0
        private set

    fun reset() { energy = 0; fastMoveCount = 0 }
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
    fun onChargedMove(charged: MoveDef): Boolean {
        val cost = charged.chargedCost.coerceAtLeast(0)
        if (energy < cost) return false
        energy = (energy - cost).coerceAtLeast(0)
        return true
    }

    /**
     * Used when the screen proves a Charged Move happened but some Fast Moves may have been
     * missed visually. We never blindly zero energy: we subtract the real move cost and preserve
     * any energy that had already been tracked. If tracking was below the cost, the minimum
     * consistent energy is assumed and the result becomes zero.
     */
    fun onObservedChargedMove(charged: MoveDef) {
        val cost = charged.chargedCost.coerceAtLeast(0)
        if (energy < cost) energy = cost
        energy = (energy - cost).coerceAtLeast(0)
    }

    fun predict(charged: MoveDef, fast: MoveDef): ChargedPrediction {
        val cost = charged.chargedCost.coerceAtLeast(1)
        val gain = fast.energyGain.coerceAtLeast(1)
        val missing = (cost - energy).coerceAtLeast(0)
        val remaining = if (missing == 0) 0 else ceil(missing.toDouble() / gain).toInt()
        return ChargedPrediction(charged, remaining, (energy.toFloat() / cost.toFloat()).coerceIn(0f, 1f), energy >= cost)
    }
}
