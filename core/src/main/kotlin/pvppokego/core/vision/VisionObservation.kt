package pvppokego.core.vision

import java.util.ArrayDeque

/**
 * Origem de uma leitura visual. O core de batalha não precisa conhecer Android,
 * OCR ou MediaProjection: ele recebe somente observações já normalizadas.
 */
enum class ObservationSource {
    OCR,
    IMAGE_MATCH,
    GEOMETRY,
    MANUAL
}

/**
 * Leitura de um campo individual com confiança independente.
 */
data class FieldObservation<T>(
    val value: T,
    val confidence: Double,
    val timestampMs: Long,
    val source: ObservationSource
) {
    init {
        require(confidence in 0.0..1.0) { "confidence must be between 0.0 and 1.0" }
    }
}

/**
 * Observações brutas extraídas de um único frame.
 * Campos ausentes simplesmente permanecem nulos.
 */
data class BattleFrameObservation(
    val timestampMs: Long,
    val enemySpeciesId: FieldObservation<String>? = null,
    val enemySlot: FieldObservation<Int>? = null,
    val enemyFastMoveId: FieldObservation<String>? = null,
    val enemyChargedMoveId: FieldObservation<String>? = null,
    val chargedMoveStarted: FieldObservation<Boolean>? = null,
    val enemySwapDetected: FieldObservation<Boolean>? = null,
    val enemyFaintedDetected: FieldObservation<Boolean>? = null,
    val mySpeciesId: FieldObservation<String>? = null,
    val myHpRatio: FieldObservation<Double>? = null,
    val enemyHpRatio: FieldObservation<Double>? = null
)

data class StableObservation<T>(
    val value: T,
    val confidence: Double,
    val confirmations: Int,
    val newestTimestampMs: Long
)

/**
 * Reduz ruído entre frames. Um valor só é considerado estável quando aparece
 * repetidamente dentro da janela e acima da confiança mínima.
 *
 * A classe é propositalmente pura e sem dependências Android para permitir
 * testes unitários e replay de sessões gravadas.
 */
class StableObservationReducer<T>(
    private val minConfidence: Double = 0.65,
    private val windowMs: Long = 1_500L,
    private val minConfirmations: Int = 2
) {
    private val history = ArrayDeque<FieldObservation<T>>()

    init {
        require(minConfidence in 0.0..1.0)
        require(windowMs > 0)
        require(minConfirmations > 0)
    }

    fun add(observation: FieldObservation<T>): StableObservation<T>? {
        history.addLast(observation)
        trim(observation.timestampMs)
        return stableValue()
    }

    fun stableValue(): StableObservation<T>? {
        val candidates = history.filter { it.confidence >= minConfidence }
        if (candidates.isEmpty()) return null

        val grouped = candidates.groupBy { it.value }
        val winner = grouped.entries.maxWithOrNull(
            compareBy<Map.Entry<T, List<FieldObservation<T>>>>(
                { it.value.size },
                { it.value.map(FieldObservation<T>::confidence).average() },
                { it.value.maxOf(FieldObservation<T>::timestampMs) }
            )
        ) ?: return null

        if (winner.value.size < minConfirmations) return null

        return StableObservation(
            value = winner.key,
            confidence = winner.value.map(FieldObservation<T>::confidence).average(),
            confirmations = winner.value.size,
            newestTimestampMs = winner.value.maxOf(FieldObservation<T>::timestampMs)
        )
    }

    fun reset() {
        history.clear()
    }

    fun sampleCount(): Int = history.size

    private fun trim(nowMs: Long) {
        val cutoff = nowMs - windowMs
        while (history.isNotEmpty() && history.first.timestampMs < cutoff) {
            history.removeFirst()
        }
    }
}
