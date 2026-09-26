package com.lucianotoscano.pvppokego.data

import kotlinx.serialization.Serializable

@Serializable
data class MoveDef(
    val moveId: String,
    val name: String = moveId,
    val type: String = "none",
    val power: Int = 0,
    val energyGain: Int = 0,
    val energy: Int = 0,
    val cooldown: Int = 500
) {
    val turns: Int get() = (cooldown / 500).coerceAtLeast(1)
    val isFast: Boolean get() = energyGain > 0
    val chargedCost: Int get() = if (energy < 0) -energy else energy
}

@Serializable
data class PokemonDef(
    val speciesId: String,
    val speciesName: String,
    val types: List<String> = emptyList()
)

@Serializable
data class GameMaster(
    val pokemon: List<PokemonDef> = emptyList(),
    val moves: List<MoveDef> = emptyList()
)

@Serializable
data class RankingMove(val moveId: String, val uses: Double = 0.0)

@Serializable
data class RankingMoves(
    val fastMoves: List<RankingMove> = emptyList(),
    val chargedMoves: List<RankingMove> = emptyList()
)

@Serializable
data class RankingEntry(
    val speciesId: String,
    val speciesName: String,
    val moves: RankingMoves = RankingMoves(),
    val moveset: List<String> = emptyList(),
    val score: Double = 0.0
)

data class DetectedPokemon(
    val name: String,
    val cp: Int?,
    val confidence: Float,
    val rawText: String
)

data class BattleDetection(
    val player: DetectedPokemon?,
    val opponent: DetectedPokemon?,
    val battleText: String = ""
)

enum class MatchupState { FAVORABLE, NEUTRAL, UNFAVORABLE, UNKNOWN }

data class ReserveState(
    val name: String?,
    val cp: Int?,
    val types: List<String>,
    val matchup: MatchupState,
    val visible: Boolean = true
)

data class BattlePokemonState(
    val key: String,
    val speciesName: String,
    var fastMoveId: String? = null,
    var chargedMove1Id: String? = null,
    var chargedMove2Id: String? = null,
    var fastMoveCount: Int = 0,
    var estimatedEnergy: Int = 0,
    var lastChargedMoveId: String? = null,
    var charge1Confirmed: Boolean = false,
    var charge2Confirmed: Boolean = false,
    var fastMoveManual: Boolean = false,
    var charge1Manual: Boolean = false,
    var charge2Manual: Boolean = false,
    var observedChargedMoveIds: MutableSet<String> = linkedSetOf(),
    var lastSeenAtMs: Long = 0L
)

data class ChargedPrediction(
    val move: MoveDef,
    val fastMovesRemaining: Int,
    val progress: Float,
    val ready: Boolean
)

data class BattleUiState(
    val playerName: String? = null,
    val playerCp: Int? = null,
    val opponentName: String? = null,
    val opponentCp: Int? = null,
    val opponentTypes: List<String> = emptyList(),
    val playerMatchup: MatchupState = MatchupState.UNKNOWN,
    val strongTypesAgainstOpponent: List<String> = emptyList(),
    val reserves: List<ReserveState> = emptyList(),
    val fastMove: MoveDef? = null,
    val charged1: ChargedPrediction? = null,
    val charged2: ChargedPrediction? = null,
    val ownSwitchSeconds: Int? = null,
    val opponentSwitchSeconds: Int? = null,
    val chargedIncoming: Boolean = false,
    val detectorConfidence: Float = 0f,
    val debugText: String? = null
)
