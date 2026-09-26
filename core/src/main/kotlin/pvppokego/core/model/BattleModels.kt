package pvppokego.core.model

enum class PokemonBattleStatus {
    UNKNOWN,
    ACTIVE,
    BENCHED,
    FAINTED
}

enum class EvidenceLevel {
    PREDICTED,
    PROBABLE,
    CONFIRMED
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

data class EnergyRangeEstimate(
    val min: Int,
    val max: Int,
    val confidence: EvidenceLevel = EvidenceLevel.PROBABLE
) {
    init {
        require(min in 0..100)
        require(max in 0..100)
        require(min <= max)
    }

    val exact: Boolean
        get() = min == max
}

data class ChargedMoveProgress(
    val move: MoveData,
    val currentEnergy: Int,
    val fastMovesNeeded: Int,
    val ready: Boolean,
    val chargesReady: Int = 0,
    /** True only when two full charges of this move can fit inside the 100-energy cap. */
    val secondChargeStorable: Boolean = false,
    /** -1 means two full charges cannot be stored simultaneously. */
    val fastMovesNeededForSecond: Int = -1,
    val evidence: EvidenceLevel = EvidenceLevel.PREDICTED
)

data class EnemyPokemonState(
    val slot: Int,
    val speciesId: String? = null,
    val speciesEvidence: EvidenceLevel = EvidenceLevel.PREDICTED,
    val fastMoveId: String? = null,
    val fastMoveEvidence: EvidenceLevel = EvidenceLevel.PREDICTED,
    /**
     * Ordered list matching the two charged-move controls in the overlay.
     * Index 0 is the left button and index 1 is the right button.
     *
     * Values may initially be predictions from the top-ranked PvPoke moveset.
     */
    val selectedChargedMoveIds: List<String> = emptyList(),
    /**
     * Charged moves actually observed in battle (for example from the localized
     * "X usou Y" / "X used Y" banner). Confirmed moves must never be displaced
     * by a later prediction or by a different observed move.
     */
    val confirmedChargedMoveIds: Set<String> = emptySet(),
    val energy: Int = 0,
    /** Number of times the tracker had to recover from an impossible low-energy observation. */
    val energyCorrectionCount: Int = 0,
    /** Energy that had to be assumed immediately before the last charged move. */
    val lastEnergyCorrection: Int = 0,
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
