package pvppokego.core.battle

enum class BattlePhase {
    PRE_BATTLE,
    ACTIVE,
    CHARGED_SEQUENCE,
    POST_BATTLE
}

data class BattleRulesProfile(
    val id: String,
    val displayName: String,
    val leagueCp: Int,
    val cupId: String? = null,
    val switchCooldownMs: Long
) {
    init {
        require(id.isNotBlank())
        require(displayName.isNotBlank())
        require(leagueCp > 0)
        require(switchCooldownMs > 0)
    }
}

data class OverlayLayoutProfile(
    val id: String,
    val displayName: String,
    val preferredMode: OverlayDisplayMode = OverlayDisplayMode.COMPACT,
    val scale: Float = 1f,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val opacity: Float = 1f,
    val autoHideDelayMs: Long = 2_500L,
    val debugVisuals: Boolean = false
) {
    init {
        require(id.isNotBlank())
        require(displayName.isNotBlank())
        require(scale in 0.5f..2.0f)
        require(opacity in 0.15f..1.0f)
        require(autoHideDelayMs >= 0L)
    }
}

data class LastBattleAction(
    val timestampMs: Long,
    val title: String,
    val detail: String,
    val slot: Int? = null,
    val moveId: String? = null
)

data class BattleUiState(
    val phase: BattlePhase = BattlePhase.PRE_BATTLE,
    val layoutProfileId: String? = null,
    val rulesProfileId: String? = null,
    val manuallyHidden: Boolean = false,
    val lastAction: LastBattleAction? = null,
    val activeAlerts: List<BattleAlert> = emptyList()
)
