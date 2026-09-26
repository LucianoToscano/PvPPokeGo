package pvppokego.core.battle

import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.EnemyPokemonState
import pvppokego.core.model.EnergyRangeEstimate
import pvppokego.core.model.EvidenceLevel
import pvppokego.core.model.MoveData

enum class BaitRiskLevel {
    NONE,
    POSSIBLE,
    HIGH
}

data class BaitRiskResult(
    val level: BaitRiskLevel,
    val cheapMove: MoveData? = null,
    val heavyMove: MoveData? = null,
    val reason: String = ""
)

class BaitRiskAdvisor(private val repository: PvpokeRepository) {
    fun assess(
        chargedMoveIds: Collection<String>,
        confirmedMoveIds: Set<String>,
        energy: EnergyRangeEstimate
    ): BaitRiskResult {
        val moves = chargedMoveIds
            .mapNotNull(repository::move)
            .filter(MoveData::isChargedMove)
            .distinctBy(MoveData::moveId)
            .sortedBy(MoveData::energyCost)

        if (moves.size < 2) return BaitRiskResult(BaitRiskLevel.NONE, reason = "single_known_move")
        val cheap = moves.first()
        val heavy = moves.last()
        if (cheap.energyCost == heavy.energyCost) {
            return BaitRiskResult(BaitRiskLevel.NONE, cheap, heavy, "same_energy_cost")
        }

        val cheapPossible = energy.max >= cheap.energyCost
        val heavyPossible = energy.max >= heavy.energyCost
        if (!cheapPossible || !heavyPossible) {
            return BaitRiskResult(BaitRiskLevel.NONE, cheap, heavy, "only_one_move_energy_plausible")
        }

        val bothConfirmed = cheap.moveId in confirmedMoveIds && heavy.moveId in confirmedMoveIds
        val heavyDefinitelyReady = energy.min >= heavy.energyCost
        return BaitRiskResult(
            level = if (bothConfirmed && heavyDefinitelyReady) BaitRiskLevel.HIGH else BaitRiskLevel.POSSIBLE,
            cheapMove = cheap,
            heavyMove = heavy,
            reason = when {
                bothConfirmed && heavyDefinitelyReady -> "cheap_and_heavy_confirmed_and_ready"
                heavyDefinitelyReady -> "cheap_and_heavy_energy_ready"
                else -> "energy_range_allows_bait_or_heavy"
            }
        )
    }
}

data class SwitchTimerState(
    val lastSwitchAtMs: Long?,
    val remainingMs: Long,
    val ready: Boolean
)

/**
 * Match rules can change, so the cooldown is supplied by the caller instead of
 * being hard-coded into the engine.
 */
class SwitchTimerTracker(private val cooldownMs: Long) {
    private var myLastSwitchAt: Long? = null
    private var enemyLastSwitchAt: Long? = null

    init {
        require(cooldownMs > 0L)
    }

    fun markMySwitch(timestampMs: Long) {
        myLastSwitchAt = timestampMs
    }

    fun markEnemySwitch(timestampMs: Long) {
        enemyLastSwitchAt = timestampMs
    }

    fun myState(nowMs: Long): SwitchTimerState = stateFor(myLastSwitchAt, nowMs)

    fun enemyState(nowMs: Long): SwitchTimerState = stateFor(enemyLastSwitchAt, nowMs)

    fun reset() {
        myLastSwitchAt = null
        enemyLastSwitchAt = null
    }

    private fun stateFor(last: Long?, now: Long): SwitchTimerState {
        if (last == null) return SwitchTimerState(null, 0L, true)
        val elapsed = (now - last).coerceAtLeast(0L)
        val remaining = (cooldownMs - elapsed).coerceAtLeast(0L)
        return SwitchTimerState(last, remaining, remaining == 0L)
    }
}

enum class BattleAlertKind {
    CHARGED_READY,
    DOUBLE_CHARGE,
    SWITCH_READY,
    BAIT_RISK,
    NEW_CONFIRMED_MOVE,
    ENERGY_DESYNC,
    LOW_CONFIDENCE
}

enum class BattleAlertPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class BattleAlert(
    val kind: BattleAlertKind,
    val priority: BattleAlertPriority,
    val title: String,
    val detail: String,
    val slot: Int? = null,
    val moveId: String? = null
)

class BattleAlertEngine(
    private val baitRiskAdvisor: BaitRiskAdvisor
) {
    fun evaluateEnemy(
        state: EnemyPokemonState,
        energyRange: EnergyRangeEstimate,
        progress: List<ChargedMoveProgress>,
        confidence: EnemyConfidenceSnapshot,
        newlyConfirmedMoveId: String? = null
    ): List<BattleAlert> {
        val alerts = mutableListOf<BattleAlert>()

        progress.filter { it.ready }.forEach { p ->
            alerts += BattleAlert(
                kind = BattleAlertKind.CHARGED_READY,
                priority = BattleAlertPriority.HIGH,
                title = "Charged ready",
                detail = p.move.name,
                slot = state.slot,
                moveId = p.move.moveId
            )
            if (p.chargesReady >= 2) {
                alerts += BattleAlert(
                    kind = BattleAlertKind.DOUBLE_CHARGE,
                    priority = BattleAlertPriority.CRITICAL,
                    title = "Double charge",
                    detail = "${p.move.name} x${p.chargesReady}",
                    slot = state.slot,
                    moveId = p.move.moveId
                )
            }
        }

        val bait = baitRiskAdvisor.assess(
            chargedMoveIds = state.selectedChargedMoveIds,
            confirmedMoveIds = state.confirmedChargedMoveIds,
            energy = energyRange
        )
        if (bait.level != BaitRiskLevel.NONE) {
            alerts += BattleAlert(
                kind = BattleAlertKind.BAIT_RISK,
                priority = if (bait.level == BaitRiskLevel.HIGH) BattleAlertPriority.HIGH else BattleAlertPriority.NORMAL,
                title = "Bait possível",
                detail = listOfNotNull(bait.cheapMove?.name, bait.heavyMove?.name).joinToString(" / "),
                slot = state.slot
            )
        }

        if (newlyConfirmedMoveId != null) {
            alerts += BattleAlert(
                kind = BattleAlertKind.NEW_CONFIRMED_MOVE,
                priority = BattleAlertPriority.NORMAL,
                title = "Golpe confirmado",
                detail = newlyConfirmedMoveId,
                slot = state.slot,
                moveId = newlyConfirmedMoveId
            )
        }

        if (state.lastEnergyCorrection > 0) {
            alerts += BattleAlert(
                kind = BattleAlertKind.ENERGY_DESYNC,
                priority = BattleAlertPriority.NORMAL,
                title = "Energia resincronizada",
                detail = "+${state.lastEnergyCorrection} de energia inferida por ataque perdido",
                slot = state.slot
            )
        }

        if (confidence.overallScore in 0.0001..0.59) {
            alerts += BattleAlert(
                kind = BattleAlertKind.LOW_CONFIDENCE,
                priority = BattleAlertPriority.LOW,
                title = "Baixa confiança",
                detail = "Algumas informações ainda são previsões",
                slot = state.slot
            )
        }

        return alerts.distinctBy { listOf(it.kind, it.slot, it.moveId, it.detail) }
    }

    fun switchAlert(state: SwitchTimerState, enemy: Boolean): BattleAlert? {
        if (!state.ready || state.lastSwitchAtMs == null) return null
        return BattleAlert(
            kind = BattleAlertKind.SWITCH_READY,
            priority = BattleAlertPriority.NORMAL,
            title = if (enemy) "Troca inimiga disponível" else "Sua troca disponível",
            detail = "Cooldown de troca concluído"
        )
    }
}

enum class OverlayDisplayMode {
    FULL,
    COMPACT,
    ALERTS_ONLY,
    HIDDEN
}

data class OverlayPolicyInput(
    val preferredMode: OverlayDisplayMode,
    val hasActiveBattle: Boolean,
    val hasImportantAlert: Boolean,
    val userManuallyHidden: Boolean,
    val interactionInProgress: Boolean = false
)

/** Pure visibility policy; Android only needs to render the resulting mode. */
class OverlayVisibilityPolicy {
    fun resolve(input: OverlayPolicyInput): OverlayDisplayMode {
        if (input.userManuallyHidden && !input.interactionInProgress) return OverlayDisplayMode.HIDDEN
        if (!input.hasActiveBattle) return OverlayDisplayMode.HIDDEN
        if (input.interactionInProgress) return OverlayDisplayMode.FULL
        return when (input.preferredMode) {
            OverlayDisplayMode.ALERTS_ONLY -> if (input.hasImportantAlert) OverlayDisplayMode.COMPACT else OverlayDisplayMode.HIDDEN
            OverlayDisplayMode.HIDDEN -> if (input.hasImportantAlert) OverlayDisplayMode.COMPACT else OverlayDisplayMode.HIDDEN
            else -> input.preferredMode
        }
    }
}
