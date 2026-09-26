package pvppokego.core.battle

import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.vision.BattleTextEventParser

/**
 * Outcome of applying an OCR charged-move banner to the live battle state.
 */
data class ChargedMoveObservationResult(
    val applied: Boolean,
    val duplicate: Boolean = false,
    val slot: Int? = null,
    val speciesId: String? = null,
    val moveId: String? = null,
    val energyBefore: Int? = null,
    val energyAfter: Int? = null,
    val progressAfter: List<ChargedMoveProgress> = emptyList()
)

/**
 * Bridges stabilized visual/OCR observations into [BattleSession].
 *
 * This component deliberately does not care whether the player shielded. The
 * game banner is the source of truth: once "X usou Y" / "X used Y" is seen,
 * the charged cost is subtracted and the counters are recalculated.
 */
class BattleObservationProcessor(
    private val session: BattleSession,
    repository: PvpokeRepository,
    moveAliases: Map<String, String> = emptyMap(),
    private val chargedBannerDedupeWindowMs: Long = 2_200L
) {
    private val textParser = BattleTextEventParser(repository, moveAliases)
    private val lastAppliedBySlotAndMove = mutableMapOf<String, Long>()

    init {
        require(chargedBannerDedupeWindowMs >= 0L)
    }

    /**
     * Apply a charged-move banner detected by OCR.
     *
     * OCR typically returns the same banner for several consecutive frames, so
     * identical slot/move events inside [chargedBannerDedupeWindowMs] are
     * ignored to avoid subtracting the charged cost multiple times.
     */
    fun onEnemyChargedMoveBanner(
        rawText: String,
        timestampMs: Long
    ): ChargedMoveObservationResult {
        val slot = session.enemyTracker.activeEnemySlot()
            ?: return ChargedMoveObservationResult(applied = false)
        val before = session.enemyTracker.state(slot)
        val speciesId = before.speciesId
            ?: return ChargedMoveObservationResult(applied = false, slot = slot)

        val parsed = textParser.parseChargedMoveBanner(rawText, speciesId)
            ?: return ChargedMoveObservationResult(
                applied = false,
                slot = slot,
                speciesId = speciesId
            )

        val key = "$slot:${parsed.moveId}"
        val lastAppliedAt = lastAppliedBySlotAndMove[key]
        if (
            lastAppliedAt != null &&
            timestampMs >= lastAppliedAt &&
            timestampMs - lastAppliedAt <= chargedBannerDedupeWindowMs
        ) {
            return ChargedMoveObservationResult(
                applied = false,
                duplicate = true,
                slot = slot,
                speciesId = speciesId,
                moveId = parsed.moveId,
                energyBefore = before.energy,
                energyAfter = before.energy,
                progressAfter = session.enemyTracker.chargedMoveProgress(slot)
            )
        }

        val after = session.enemyTracker.onChargedMove(slot, parsed.moveId)
        lastAppliedBySlotAndMove[key] = timestampMs

        return ChargedMoveObservationResult(
            applied = true,
            slot = slot,
            speciesId = speciesId,
            moveId = parsed.moveId,
            energyBefore = before.energy,
            energyAfter = after.energy,
            progressAfter = session.enemyTracker.chargedMoveProgress(slot)
        )
    }

    fun reset() {
        lastAppliedBySlotAndMove.clear()
    }
}
