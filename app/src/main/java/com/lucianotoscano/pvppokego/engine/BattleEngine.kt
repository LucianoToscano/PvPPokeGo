package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.BattlePokemonState
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.DetectedPokemon
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.ReserveState
import java.text.Normalizer

/**
 * Battle-domain state. It never touches Pokémon GO and never sends input.
 * V03 connects the visual Fast-Move detector, charged-prompt reset and own-team memory.
 */
class BattleEngine(private val repo: GameDataRepository) {
    data class DetectionChanges(val playerChanged: Boolean, val enemyChanged: Boolean)
    data class BattleTextResult(val newChargedPrompt: Boolean = false, val chargedMoveConfirmed: Boolean = false)

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

    // LinkedHashMap preserves team order learned before/during battle.
    private val ownTeam = linkedMapOf<String, Int?>()
    private var chargedPendingSinceMs: Long? = null
    private var lastChargedConfirmedAtMs: Long = 0L
    private var lastAutoSource: String = "-"
    private var lastDetectorConfidence = 0f
    private var lastHpRatio: Float? = null
    private var lastMotion = 0f

    fun resetBattle() {
        playerName = null; playerCp = null
        enemyName = null; enemyCp = null
        tracker.reset(); activeState = null
        fast = null; charge1 = null; charge2 = null
        ownTeam.clear()
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = 0L
        lastAutoSource = "-"; lastDetectorConfidence = 0f; lastHpRatio = null; lastMotion = 0f
        enemyStates.clearBattle()
    }

    fun onDetection(d: BattleDetection): DetectionChanges {
        val oldPlayer = playerName
        val oldEnemy = enemyName

        d.player?.let {
            val canonical = repo.canonicalPokemonName(it.name) ?: it.name
            playerName = canonical
            playerCp = it.cp ?: playerCp
            rememberOwnPokemon(canonical, it.cp)
        }
        d.opponent?.let {
            val canonical = repo.canonicalPokemonName(it.name) ?: it.name
            enemyCp = it.cp ?: enemyCp
            if (!canonical.equals(enemyName, ignoreCase = true)) onEnemyChanged(canonical)
        }
        return DetectionChanges(
            playerChanged = oldPlayer != null && !oldPlayer.equals(playerName, ignoreCase = true),
            enemyChanged = oldEnemy != null && !oldEnemy.equals(enemyName, ignoreCase = true)
        )
    }

    fun onOwnTeamDetected(team: List<DetectedPokemon>) {
        team.forEach { p ->
            val canonical = repo.canonicalPokemonName(p.name) ?: p.name
            if (!canonical.equals(enemyName, ignoreCase = true)) rememberOwnPokemon(canonical, p.cp)
        }
    }

    fun needsOwnTeamScan(): Boolean = ownTeam.size < 3
    fun activePlayerName(): String? = playerName
    fun activeEnemyName(): String? = enemyName
    fun currentFastDurationMs(): Long = fast?.cooldown?.toLong()?.coerceIn(500L, 3_500L) ?: 1_000L

    private fun rememberOwnPokemon(name: String, cp: Int?) {
        val existingKey = ownTeam.keys.firstOrNull { it.equals(name, ignoreCase = true) }
        if (existingKey == null && ownTeam.size < 3) ownTeam[name] = cp
        else if (existingKey != null && cp != null) ownTeam[existingKey] = cp
    }

    private fun onEnemyChanged(name: String) {
        saveActiveState()
        enemyName = name
        chargedPendingSinceMs = null
        val key = normalize(name)
        activeState = enemyStates.getOrCreate(key, name)
        val saved = activeState!!
        val likely = repo.likelyMoveset(name)
        fast = repo.move(saved.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        charge1 = repo.move(saved.chargedMove1Id) ?: likely.second ?: repo.possibleChargedMoves(name).getOrNull(0)
        charge2 = repo.move(saved.chargedMove2Id) ?: likely.third ?: repo.possibleChargedMoves(name).getOrNull(1)
        tracker.restore(saved.estimatedEnergy, saved.fastMoveCount)
    }

    fun selectFast(move: MoveDef) {
        fast = move
        activeState?.fastMoveId = move.moveId
        saveActiveState()
    }

    fun selectCharged1(move: MoveDef) {
        charge1 = move
        activeState?.chargedMove1Id = move.moveId
        saveActiveState()
    }

    fun selectCharged2(move: MoveDef) {
        charge2 = move
        activeState?.chargedMove2Id = move.moveId
        saveActiveState()
    }

    fun manualFastDelta(delta: Int) {
        val f = fast ?: return
        if (delta > 0) tracker.onFastMove(f, delta)
        if (delta < 0) tracker.undoFastMove(f, -delta)
        lastAutoSource = "manual"
        saveActiveState()
    }

    fun onFastDetected(count: Int, confidence: Float, source: String, hpRatio: Float?, motion: Float) {
        lastDetectorConfidence = confidence
        lastAutoSource = source
        lastHpRatio = hpRatio
        lastMotion = motion
        if (count <= 0) return
        val f = fast ?: return
        tracker.onFastMove(f, count)
        saveActiveState()
    }

    /** Manual/known charged move. */
    fun chargedUsed(move: MoveDef): Boolean {
        val ok = tracker.onChargedMove(move)
        if (ok) recordChargedMove(move)
        return ok
    }

    /** Visual confirmation from battle text or charged-prompt fallback. */
    private fun observedChargedUsed(move: MoveDef): Boolean {
        tracker.onObservedChargedMove(move)
        recordChargedMove(move)
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = System.currentTimeMillis()
        return true
    }

    private fun recordChargedMove(move: MoveDef) {
        activeState?.lastChargedMoveId = move.moveId
        if (move.moveId == charge1?.moveId) activeState?.charge1Confirmed = true
        if (move.moveId == charge2?.moveId) activeState?.charge2Confirmed = true
        saveActiveState()
    }

    fun onBattleText(raw: String, nowMs: Long = System.currentTimeMillis()): BattleTextResult {
        if (raw.isBlank()) return BattleTextResult()
        val text = normalizeText(raw)
        var newPrompt = false
        if (CHARGED_PROMPTS.any { text.contains(it) }) {
            if (chargedPendingSinceMs == null && nowMs - lastChargedConfirmedAtMs > 1_800L) {
                chargedPendingSinceMs = nowMs
                newPrompt = true
            }
        }

        // Do not simply search move labels: the overlay itself may be part of the captured frame.
        // Only accept a move name when OCR also sees a game action verb such as "usou"/"used".
        val actionVerbPresent = text.contains("usou") || text.contains("used") || text.contains("utilizou")
        if (actionVerbPresent) {
            val candidates = possibleChargedMoves().ifEmpty { listOfNotNull(charge1, charge2) }
            val matched = candidates.firstOrNull { move ->
                val moveName = normalizeText(repo.localizedMoveName(move))
                moveName.length >= 4 && text.contains(moveName)
            }
            if (matched != null && nowMs - lastChargedConfirmedAtMs > 1_800L) {
                observedChargedUsed(matched)
                return BattleTextResult(newPrompt, true)
            }
        }
        return BattleTextResult(newPrompt, false)
    }

    /**
     * If the charge prompt was visually confirmed but its exact move name was not readable,
     * spend the most likely selected charged move after the animation window. This fixes the
     * counter reset while retaining residual energy whenever the tracked estimate allows it.
     */
    fun maybeConfirmPendingCharged(nowMs: Long = System.currentTimeMillis()): Boolean {
        val since = chargedPendingSinceMs ?: return false
        if (nowMs - since < 4_900L) return false
        val candidates = listOfNotNull(charge1, charge2)
        if (candidates.isEmpty()) {
            chargedPendingSinceMs = null
            return false
        }
        val selected = candidates.firstOrNull { tracker.energy >= it.chargedCost }
            ?: candidates.minByOrNull { it.chargedCost }
            ?: return false
        return observedChargedUsed(selected)
    }

    fun possibleFastMoves(): List<MoveDef> = enemyName?.let(repo::possibleFastMoves).orEmpty()
    fun possibleChargedMoves(): List<MoveDef> = enemyName?.let(repo::possibleChargedMoves).orEmpty()

    fun uiState(debug: Boolean = false): BattleUiState {
        val pTypes = repo.pokemon(playerName)?.types.orEmpty()
        val eTypes = repo.pokemon(enemyName)?.types.orEmpty()
        val matchup = classify(pTypes, eTypes)
        val f = fast
        val reserveStates = ownTeam.entries
            .filterNot { it.key.equals(playerName, ignoreCase = true) }
            .take(2)
            .map { (name, cp) ->
                val types = repo.pokemon(name)?.types.orEmpty()
                ReserveState(name, cp, types, classify(types, eTypes))
            }
        return BattleUiState(
            playerName = playerName,
            playerCp = playerCp,
            opponentName = enemyName,
            opponentCp = enemyCp,
            opponentTypes = eTypes,
            playerMatchup = matchup,
            strongTypesAgainstOpponent = if (eTypes.isEmpty()) emptyList() else TypeChart.strongTypesAgainst(eTypes),
            reserves = reserveStates,
            fastMove = f,
            charged1 = if (f != null && charge1 != null) tracker.predict(charge1!!, f) else null,
            charged2 = if (f != null && charge2 != null) tracker.predict(charge2!!, f) else null,
            chargedIncoming = chargedPendingSinceMs != null,
            detectorConfidence = lastDetectorConfidence,
            debugText = if (debug) buildString {
                appendLine("Jogador: ${playerName ?: "?"} PC ${playerCp ?: "?"} | time=${ownTeam.keys.joinToString()}")
                appendLine("Inimigo: ${enemyName ?: "?"} PC ${enemyCp ?: "?"}")
                appendLine("Fast: ${f?.name ?: "?"} | contagem=${tracker.fastMoveCount}")
                appendLine("Energia=${tracker.energy}/100 | fonte=$lastAutoSource conf=${"%.2f".format(lastDetectorConfidence)}")
                appendLine("HP=${lastHpRatio?.let { "%.3f".format(it) } ?: "?"} | movimento=${"%.1f".format(lastMotion)}")
                appendLine("C1=${charge1?.name ?: "?"} | C2=${charge2?.name ?: "?"}")
                append("Charged pendente=${chargedPendingSinceMs != null}")
            } else null
        )
    }

    private fun classify(myTypes: List<String>, enemyTypes: List<String>): MatchupState {
        if (myTypes.isEmpty() || enemyTypes.isEmpty()) return MatchupState.UNKNOWN
        return when (TypeChart.classify(myTypes, enemyTypes)) {
            1 -> MatchupState.FAVORABLE
            -1 -> MatchupState.UNFAVORABLE
            else -> MatchupState.NEUTRAL
        }
    }

    private fun saveActiveState() {
        val state = activeState ?: return
        // BattleStateStore keeps the same mutable BattlePokemonState instance returned
        // by getOrCreate(), so updating it in place is enough to persist this opponent.
        state.fastMoveId = fast?.moveId
        state.chargedMove1Id = charge1?.moveId
        state.chargedMove2Id = charge2?.moveId
        state.fastMoveCount = tracker.fastMoveCount
        state.estimatedEnergy = tracker.energy
        state.lastSeenAtMs = System.currentTimeMillis()
    }

    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z0-9]+"), "")

    private fun normalizeText(s: String): String = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9]+"), "")

    companion object {
        private val CHARGED_PROMPTS = listOf(
            "ataquecarregado", "ataquechegando", "ataquecarregadoagora",
            "attackincoming", "chargemoveincoming", "usarumescudo", "useumescudo", "protectshield"
        )
    }
}
