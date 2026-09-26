package pvppokego.core.battle

/**
 * Feature flags intentionally remain independent so the overlay can expose a
 * separate toggle for fast attack, charged attack, and shield behavior.
 */
data class BattleAutomationSettings(
    val enabled: Boolean = false,
    val autoFastAttack: Boolean = false,
    val autoChargedAttack: Boolean = false,
    val chargedSelectionMode: ChargedSelectionMode = ChargedSelectionMode.SMART,
    val autoShield: Boolean = false,
    val shieldMode: ShieldMode = ShieldMode.SMART
)

data class BattleAutomationFrame(
    val mySpeciesId: String,
    val myChargedMoveIds: List<String>,
    val myEnergy: Int,
    val myHpFraction: Double,
    val myShieldsRemaining: Int,
    val enemySpeciesId: String,
    val enemyChargedMoveIdsCurrentlyPlausible: List<String> = emptyList(),
    val enemyHpFraction: Double,
    val enemyShieldsRemaining: Int,
    val fastAttackWindowOpen: Boolean = true,
    val shieldPromptVisible: Boolean = false
)

sealed interface BattleActionIntent {
    object TapFastAttack : BattleActionIntent

    data class TapChargedAttack(
        val buttonIndex: Int,
        val moveId: String,
        val score: Double,
        val reasons: List<String>
    ) : BattleActionIntent

    data class TapShield(
        val threateningMoveId: String?,
        val reason: String,
        val threatScore: Double
    ) : BattleActionIntent
}

data class BattleAutomationPlan(
    val actions: List<BattleActionIntent>,
    val chargedDecision: ChargedMoveDecision? = null,
    val shieldDecision: ShieldDecision? = null
)

/**
 * Converts a recognized battle frame into action intents.
 *
 * It deliberately does not know screen coordinates and does not dispatch
 * Android gestures. A future AccessibilityService adapter can map these intents
 * to actual taps, while the same controller can also power assist-only UI.
 */
class BattleAutomationController(
    private val decisionEngine: BattleDecisionEngine
) {
    fun plan(
        frame: BattleAutomationFrame,
        settings: BattleAutomationSettings
    ): BattleAutomationPlan {
        if (!settings.enabled) return BattleAutomationPlan(emptyList())

        var shieldDecision: ShieldDecision? = null
        if (settings.autoShield && frame.shieldPromptVisible) {
            shieldDecision = decisionEngine.recommendShield(
                defenderSpeciesId = frame.mySpeciesId,
                attackerSpeciesId = frame.enemySpeciesId,
                candidateChargedMoveIds = frame.enemyChargedMoveIdsCurrentlyPlausible,
                mode = settings.shieldMode,
                shieldsRemaining = frame.myShieldsRemaining,
                defenderHpFraction = frame.myHpFraction
            )
            if (shieldDecision.useShield) {
                return BattleAutomationPlan(
                    actions = listOf(
                        BattleActionIntent.TapShield(
                            threateningMoveId = shieldDecision.threateningMove?.moveId,
                            reason = shieldDecision.reason,
                            threatScore = shieldDecision.threatScore
                        )
                    ),
                    shieldDecision = shieldDecision
                )
            }
        }

        var chargedDecision: ChargedMoveDecision? = null
        if (settings.autoChargedAttack) {
            chargedDecision = decisionEngine.chooseChargedMove(
                attackerSpeciesId = frame.mySpeciesId,
                chargedMoveIds = frame.myChargedMoveIds,
                currentEnergy = frame.myEnergy,
                defenderSpeciesId = frame.enemySpeciesId,
                mode = settings.chargedSelectionMode,
                defenderHpFraction = frame.enemyHpFraction,
                defenderShieldsRemaining = frame.enemyShieldsRemaining
            )
            chargedDecision.selected?.let { selected ->
                return BattleAutomationPlan(
                    actions = listOf(
                        BattleActionIntent.TapChargedAttack(
                            buttonIndex = selected.buttonIndex,
                            moveId = selected.move.moveId,
                            score = selected.score,
                            reasons = selected.reasons
                        )
                    ),
                    chargedDecision = chargedDecision,
                    shieldDecision = shieldDecision
                )
            }
        }

        val actions = if (settings.autoFastAttack && frame.fastAttackWindowOpen) {
            listOf(BattleActionIntent.TapFastAttack)
        } else {
            emptyList()
        }

        return BattleAutomationPlan(
            actions = actions,
            chargedDecision = chargedDecision,
            shieldDecision = shieldDecision
        )
    }
}
