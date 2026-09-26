package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.BattlePokemonState
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.ReserveState

class BattleEngine(private val repo: GameDataRepository) {
    private val enemyStates = BattleStateStore()
    private var playerName: String? = null
    private var playerCp: Int? = null
    private var enemyName: String? = null
    private var enemyCp: Int? = null
    private var tracker = EnergyTracker()
    private var activeState: BattlePokemonState? = null
    private var fast: MoveDef? = null
    private var charge1: MoveDef? = null
    private var charge2: MoveDef? = null
    private var reserves: List<ReserveState> = emptyList()

    fun resetBattle() {
        playerName = null; playerCp = null
        enemyName = null; enemyCp = null
        tracker.reset(); activeState = null
        fast = null; charge1 = null; charge2 = null
        reserves = emptyList(); enemyStates.clearBattle()
    }

    fun onDetection(d: BattleDetection) {
        d.player?.let { playerName = repo.canonicalPokemonName(it.name) ?: it.name; playerCp = it.cp ?: playerCp }
        d.opponent?.let {
            val canonical = repo.canonicalPokemonName(it.name) ?: it.name
            enemyCp = it.cp ?: enemyCp
            if (!canonical.equals(enemyName, ignoreCase = true)) onEnemyChanged(canonical)
        }
    }

    private fun onEnemyChanged(name: String) {
        saveActiveState(); enemyName = name
        val saved = enemyStates.getOrCreate(normalize(name), name)
        activeState = saved
        val likely = repo.likelyMoveset(name)
        fast = repo.move(saved.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        charge1 = repo.move(saved.chargedMove1Id) ?: likely.second ?: repo.possibleChargedMoves(name).getOrNull(0)
        charge2 = repo.move(saved.chargedMove2Id) ?: likely.third ?: repo.possibleChargedMoves(name).getOrNull(1)
        tracker.restore(saved.estimatedEnergy, saved.fastMoveCount)
    }

    fun selectFast(move: MoveDef) { fast = move; activeState?.fastMoveId = move.moveId; saveActiveState() }
    fun selectCharged1(move: MoveDef) { charge1 = move; activeState?.chargedMove1Id = move.moveId; saveActiveState() }
    fun selectCharged2(move: MoveDef) { charge2 = move; activeState?.chargedMove2Id = move.moveId; saveActiveState() }

    fun manualFastDelta(delta: Int) {
        val f = fast ?: return
        if (delta > 0) tracker.onFastMove(f, delta)
        if (delta < 0) tracker.undoFastMove(f, -delta)
        saveActiveState()
    }

    fun chargedUsed(move: MoveDef): Boolean {
        val ok = tracker.onChargedMove(move)
        if (ok) {
            activeState?.lastChargedMoveId = move.moveId
            if (move.moveId == charge1?.moveId) activeState?.charge1Confirmed = true
            if (move.moveId == charge2?.moveId) activeState?.charge2Confirmed = true
            saveActiveState()
        }
        return ok
    }

    fun possibleFastMoves(): List<MoveDef> = enemyName?.let(repo::possibleFastMoves).orEmpty()
    fun possibleChargedMoves(): List<MoveDef> = enemyName?.let(repo::possibleChargedMoves).orEmpty()
    fun setReserves(value: List<ReserveState>) { reserves = value.take(2) }

    fun uiState(debug: Boolean = false): BattleUiState {
        val pTypes = repo.pokemon(playerName)?.types.orEmpty()
        val eTypes = repo.pokemon(enemyName)?.types.orEmpty()
        val matchup = if (pTypes.isEmpty() || eTypes.isEmpty()) MatchupState.UNKNOWN else when (TypeChart.classify(pTypes, eTypes)) { 1 -> MatchupState.FAVORABLE; -1 -> MatchupState.UNFAVORABLE; else -> MatchupState.NEUTRAL }
        val f = fast
        return BattleUiState(
            playerName = playerName, playerCp = playerCp, opponentName = enemyName, opponentCp = enemyCp,
            playerMatchup = matchup,
            strongTypesAgainstOpponent = if (eTypes.isEmpty()) emptyList() else TypeChart.strongTypesAgainst(eTypes),
            reserves = reserves, fastMove = f,
            charged1 = if (f != null && charge1 != null) tracker.predict(charge1!!, f) else null,
            charged2 = if (f != null && charge2 != null) tracker.predict(charge2!!, f) else null,
            debugText = if (debug) buildString {
                appendLine("Jogador: ${playerName ?: "?"} PC ${playerCp ?: "?"}")
                appendLine("Inimigo: ${enemyName ?: "?"} PC ${enemyCp ?: "?"}")
                appendLine("Fast: ${f?.name ?: "?"} | contagem=${tracker.fastMoveCount}")
                appendLine("Energia estimada=${tracker.energy}/100")
                appendLine("C1=${charge1?.name ?: "?"} | C2=${charge2?.name ?: "?"}")
                append("Auto Fast: DESLIGADO (Fase 6)")
            } else null
        )
    }

    private fun saveActiveState() {
        activeState?.let { s ->
            s.fastMoveId = fast?.moveId; s.chargedMove1Id = charge1?.moveId; s.chargedMove2Id = charge2?.moveId
            s.fastMoveCount = tracker.fastMoveCount; s.estimatedEnergy = tracker.energy; s.lastSeenAtMs = System.currentTimeMillis()
            enemyStates.put(s)
        }
    }
    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z0-9]+"), "")
}
