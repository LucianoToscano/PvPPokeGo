package pvppokego.core.battle

import pvppokego.core.model.EvidenceLevel

/**
 * Manual driver for testing the battle core without Pokemon GO running.
 * UI can expose buttons for fast/charged/switch and inspect the same state used
 * by the live overlay.
 */
class BattleTrainingController(
    private val session: BattleSession,
    private val replay: BattleReplayRecorder = BattleReplayRecorder()
) {
    fun start(timestampMs: Long) {
        session.reset()
        replay.clear()
        replay.record(BattleReplayEvent(timestampMs, BattleReplayEventType.BATTLE_START))
    }

    fun identifyEnemy(
        slot: Int,
        speciesId: String,
        fastMoveId: String? = null,
        timestampMs: Long
    ) {
        session.enemyTracker.identifyPokemon(
            slot = slot,
            speciesId = speciesId,
            fastMoveId = fastMoveId,
            evidence = EvidenceLevel.CONFIRMED
        )
        replay.record(
            BattleReplayEvent(
                timestampMs = timestampMs,
                type = BattleReplayEventType.ENEMY_IDENTIFIED,
                slot = slot,
                speciesId = speciesId,
                moveId = fastMoveId
            )
        )
    }

    fun switchEnemy(slot: Int, timestampMs: Long) {
        val state = session.enemyTracker.switchTo(slot)
        replay.record(
            BattleReplayEvent(
                timestampMs = timestampMs,
                type = BattleReplayEventType.SWITCH,
                slot = slot,
                speciesId = state.speciesId
            )
        )
    }

    fun enemyFast(slot: Int, count: Int = 1, timestampMs: Long) {
        val state = session.enemyTracker.onFastMove(slot, count)
        replay.record(
            BattleReplayEvent(
                timestampMs = timestampMs,
                type = BattleReplayEventType.FAST_MOVE,
                slot = slot,
                speciesId = state.speciesId,
                moveId = state.fastMoveId,
                metadata = mapOf("count" to count.toString(), "energy" to state.energy.toString())
            )
        )
    }

    fun enemyCharged(slot: Int, moveId: String, timestampMs: Long) {
        val before = session.enemyTracker.state(slot)
        val after = session.enemyTracker.onChargedMove(slot, moveId)
        replay.record(
            BattleReplayEvent(
                timestampMs = timestampMs,
                type = BattleReplayEventType.CHARGED_MOVE,
                slot = slot,
                speciesId = after.speciesId,
                moveId = moveId,
                metadata = mapOf(
                    "energy_before" to before.energy.toString(),
                    "energy_after" to after.energy.toString(),
                    "energy_correction" to after.lastEnergyCorrection.toString()
                )
            )
        )
    }

    fun correctEnergy(slot: Int, energy: Int, timestampMs: Long) {
        session.enemyTracker.setEstimatedEnergy(slot, energy)
        replay.record(
            BattleReplayEvent(
                timestampMs = timestampMs,
                type = BattleReplayEventType.MANUAL_CORRECTION,
                slot = slot,
                detail = "energy=$energy"
            )
        )
    }

    fun end(timestampMs: Long) {
        replay.record(BattleReplayEvent(timestampMs, BattleReplayEventType.BATTLE_END))
    }

    fun replayEvents(): List<BattleReplayEvent> = replay.all()
}
