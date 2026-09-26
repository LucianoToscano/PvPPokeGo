package pvppokego.core.model

enum class PokemonBattleStatus {
    UNKNOWN,
    ACTIVE,
    BENCHED,
    FAINTED
}

data class MoveData(
    val moveId: String,
    val name: String,
    val type: String,
    val power: Int = 0,
    val energyGain: Int = 0,
    val energyCost: Int = 0,
    val cooldownMs: Int = 0,
    val turns: Int = 0
) {
    val isFastMove: Boolean
        get() = energyGain > 0

    val isChargedMove: Boolean
        get() = energyCost > 0
}

data class PokemonData(
    val speciesId: String,
    val speciesName: String,
    val types: List<String>,
    val fastMoveIds: Set<String>,
    val chargedMoveIds: Set<String>
)

data class RecommendedMoveset(
    val leagueCp: Int,
    val rank: Int,
    val speciesId: String,
    val fastMoveId: String?,
    val chargedMoveIds: List<String>,
    val score: Double? = null
)

data class ChargedMoveProgress(
    val move: MoveData,
    val currentEnergy: Int,
    val fastMovesNeeded: Int,
    val ready: Boolean
)

data class EnemyPokemonState(
    val slot: Int,
    val speciesId: String? = null,
    val fastMoveId: String? = null,
    /** Ordered list: index 0 = left charged control, index 1 = right. */
    val selectedChargedMoveIds: List<String> = emptyList(),
    /** True only when the user explicitly changed the fast move in the overlay. */
    val fastMoveManuallyOverridden: Boolean = false,
    /** Button indexes explicitly changed by the user. Automatic OCR never replaces them. */
    val manualChargedMoveButtonIndexes: Set<Int> = emptySet(),
    /**
     * Charged moves actually observed in battle banners. This is distinct from
     * the PvPoke rank #1 defaults so a first observed move can be locked and a
     * second different observed move can replace the other, still-unverified slot.
     */
    val observedChargedMoveIds: Set<String> = emptySet(),
    val energy: Int = 0,
    val fastMovesObserved: Int = 0,
    val chargedMovesUsed: Int = 0,
    val lastChargedMoveId: String? = null,
    val status: PokemonBattleStatus = PokemonBattleStatus.UNKNOWN
)

data class MyPokemonState(
    val slot: Int,
    val speciesId: String? = null,
    val status: PokemonBattleStatus = PokemonBattleStatus.UNKNOWN
)

data class BattleSnapshot(
    val myTeam: List<MyPokemonState>,
    val enemyTeam: List<EnemyPokemonState>,
    val activeEnemySlot: Int?
)
