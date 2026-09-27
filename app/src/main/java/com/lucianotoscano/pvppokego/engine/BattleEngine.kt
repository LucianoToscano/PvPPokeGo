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
 *
 * Enemy energy follows Pokemon GO PvP rules: Fast Moves add their PvP energy gain up to 100;
 * a confirmed Charged Move subtracts its real PvP energy cost and preserves any residual energy.
 */
class BattleEngine(private val repo: GameDataRepository) {
    data class DetectionChanges(val playerChanged: Boolean, val enemyChanged: Boolean)
    data class BattleTextResult(
        val newChargedPrompt: Boolean = false,
        val chargedMoveConfirmed: Boolean = false
    )

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

    private val ownTeam = linkedMapOf<String, Int?>()
    private val reserveSlotVisible = arrayOfNulls<Boolean>(2)
    private val reserveMissingStreak = IntArray(2)
    private var ownSwitchReadyAtMs: Long = 0L
    private var opponentSwitchReadyAtMs: Long = 0L
    private var chargedPendingSinceMs: Long? = null
    private var lastChargedConfirmedAtMs: Long = 0L
    private var ignoreFastUntilMs: Long = 0L
    private var lastAutoSource: String = "-"
    private var lastDetectorConfidence = 0f
    private var lastHpRatio: Float? = null
    private var lastMotion = 0f

    fun resetBattle() {
        playerName = null
        playerCp = null
        enemyName = null
        enemyCp = null
        tracker.reset()
        activeState = null
        fast = null
        charge1 = null
        charge2 = null
        ownTeam.clear()
        reserveSlotVisible.fill(null)
        reserveMissingStreak.fill(0)
        ownSwitchReadyAtMs = 0L
        opponentSwitchReadyAtMs = 0L
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = 0L
        ignoreFastUntilMs = 0L
        lastAutoSource = "-"
        lastDetectorConfidence = 0f
        lastHpRatio = null
        lastMotion = 0f
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

        val playerChanged = oldPlayer != null && !oldPlayer.equals(playerName, ignoreCase = true)
        val enemyChanged = oldEnemy != null && !oldEnemy.equals(enemyName, ignoreCase = true)
        val now = System.currentTimeMillis()
        if (playerChanged) {
            reserveSlotVisible.fill(null)
            reserveMissingStreak.fill(0)
            ownSwitchReadyAtMs = now + SWITCH_COOLDOWN_MS
        }
        if (enemyChanged) opponentSwitchReadyAtMs = now + SWITCH_COOLDOWN_MS

        return DetectionChanges(
            playerChanged = playerChanged,
            enemyChanged = enemyChanged
        )
    }

    fun onOwnTeamDetected(team: List<DetectedPokemon>) {
        team.forEach { p ->
            val canonical = repo.canonicalPokemonName(p.name) ?: p.name
            if (!canonical.equals(enemyName, ignoreCase = true)) rememberOwnPokemon(canonical, p.cp)
        }
    }

    /** Native reserve card visibility comes from their own small HP bars on the right side. */
    fun onReserveSlotsDetected(firstVisible: Boolean?, secondVisible: Boolean?) {
        fun update(index: Int, value: Boolean?) {
            when (value) {
                true -> { reserveMissingStreak[index] = 0; reserveSlotVisible[index] = true }
                false -> {
                    reserveMissingStreak[index]++
                    if (reserveMissingStreak[index] >= RESERVE_MISSING_CONFIRM_FRAMES) reserveSlotVisible[index] = false
                }
                null -> Unit
            }
        }
        update(0, firstVisible)
        update(1, secondVisible)
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
        ignoreFastUntilMs = 0L

        val key = normalize(name)
        activeState = enemyStates.getOrCreate(key, name)
        val saved = activeState!!

        val likely = repo.likelyMoveset(name)
        fast = repo.move(saved.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        charge1 = repo.move(saved.chargedMove1Id) ?: likely.second ?: repo.possibleChargedMoves(name).getOrNull(0)
        charge2 = repo.move(saved.chargedMove2Id) ?: likely.third ?: repo.possibleChargedMoves(name).getOrNull(1)
        tracker.restore(saved.estimatedEnergy, saved.fastMoveCount)
        saveActiveState()
    }

    fun selectFast(move: MoveDef) {
        fast = move
        activeState?.apply {
            fastMoveId = move.moveId
            fastMoveManual = true
        }
        saveActiveState()
    }

    fun clearFastManualOverride() {
        val state = activeState ?: return
        state.fastMoveManual = false
        val recommended = enemyName?.let(repo::likelyMoveset)?.first ?: return
        fast = recommended
        state.fastMoveId = recommended.moveId
        saveActiveState()
    }

    fun selectCharged1(move: MoveDef) {
        charge1 = move
        activeState?.apply {
            chargedMove1Id = move.moveId
            charge1Manual = true
            charge1Confirmed = false
        }
        saveActiveState()
    }

    fun selectCharged2(move: MoveDef) {
        charge2 = move
        activeState?.apply {
            chargedMove2Id = move.moveId
            charge2Manual = true
            charge2Confirmed = false
        }
        saveActiveState()
    }

    fun clearChargedManualOverride(index: Int) {
        val state = activeState ?: return
        val likely = enemyName?.let(repo::likelyMoveset) ?: return
        when (index) {
            0 -> {
                state.charge1Manual = false
                val recommended = likely.second ?: repo.possibleChargedMoves(state.speciesName).getOrNull(0)
                if (recommended != null && recommended.moveId != charge2?.moveId) {
                    charge1 = recommended
                    state.chargedMove1Id = recommended.moveId
                    state.charge1Confirmed = recommended.moveId in state.observedChargedMoveIds
                }
            }
            1 -> {
                state.charge2Manual = false
                val recommended = likely.third ?: repo.possibleChargedMoves(state.speciesName).getOrNull(1)
                if (recommended != null && recommended.moveId != charge1?.moveId) {
                    charge2 = recommended
                    state.chargedMove2Id = recommended.moveId
                    state.charge2Confirmed = recommended.moveId in state.observedChargedMoveIds
                }
            }
        }
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

        if (System.currentTimeMillis() < ignoreFastUntilMs) {
            lastAutoSource = "charged-guard"
            return
        }

        val f = fast ?: return
        tracker.onFastMove(f, count)
        saveActiveState()
    }

    fun chargedUsed(move: MoveDef): Boolean {
        val ok = tracker.onChargedMove(move)
        if (ok) recordChargedMove(move, observed = false)
        return ok
    }

    private fun observedChargedUsed(move: MoveDef, nowMs: Long = System.currentTimeMillis()): Boolean {
        tracker.onObservedChargedMove(move)
        recordChargedMove(move, observed = true)
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = nowMs
        ignoreFastUntilMs = maxOf(ignoreFastUntilMs, nowMs + CHARGED_FAST_GUARD_MS)
        lastAutoSource = "charged:${move.moveId}"
        saveActiveState()
        return true
    }

    private fun recordChargedMove(move: MoveDef, observed: Boolean) {
        val state = activeState
        if (state != null) {
            state.lastChargedMoveId = move.moveId
            if (observed) state.observedChargedMoveIds += move.moveId

            when {
                move.moveId == charge1?.moveId -> state.charge1Confirmed = observed || state.charge1Confirmed
                move.moveId == charge2?.moveId -> state.charge2Confirmed = observed || state.charge2Confirmed
                observed -> autoLearnObservedCharged(state, move)
            }
        }
        saveActiveState()
    }

    private fun autoLearnObservedCharged(state: BattlePokemonState, move: MoveDef) {
        val replaceIndex = when {
            !state.charge1Manual && !state.charge1Confirmed -> 0
            !state.charge2Manual && !state.charge2Confirmed -> 1
            !state.charge1Manual && charge1?.moveId !in state.observedChargedMoveIds -> 0
            !state.charge2Manual && charge2?.moveId !in state.observedChargedMoveIds -> 1
            else -> -1
        }
        when (replaceIndex) {
            0 -> {
                charge1 = move
                state.chargedMove1Id = move.moveId
                state.charge1Confirmed = true
            }
            1 -> {
                charge2 = move
                state.chargedMove2Id = move.moveId
                state.charge2Confirmed = true
            }
        }
    }

    fun onBattleText(raw: String, nowMs: Long = System.currentTimeMillis()): BattleTextResult {
        if (raw.isBlank()) return BattleTextResult()
        val text = normalizeText(raw)
        var newPrompt = false

        if (CHARGED_PROMPTS.any { text.contains(it) }) {
            if (chargedPendingSinceMs == null && nowMs - lastChargedConfirmedAtMs > CHARGED_DEBOUNCE_MS) {
                chargedPendingSinceMs = nowMs
                newPrompt = true
            }
        }

        val actionVerbPresent = ACTION_VERBS.any { text.contains(it) }
        if (actionVerbPresent) {
            val candidates = possibleChargedMoves().ifEmpty { listOfNotNull(charge1, charge2) }
            val matched = candidates
                .map { it to normalizedMoveNames(it) }
                .firstOrNull { (_, names) -> names.any { n -> n.length >= 4 && text.contains(n) } }
                ?.first

            if (matched != null) {
                if (nowMs - lastChargedConfirmedAtMs > CHARGED_DEBOUNCE_MS) {
                    observedChargedUsed(matched, nowMs)
                    return BattleTextResult(newPrompt, true)
                }
                return BattleTextResult(newPrompt, false)
            }

            if (chargedPendingSinceMs == null && nowMs - lastChargedConfirmedAtMs > CHARGED_DEBOUNCE_MS) {
                chargedPendingSinceMs = nowMs
                newPrompt = true
            }
        }

        return BattleTextResult(newPrompt, false)
    }

    fun maybeConfirmPendingCharged(nowMs: Long = System.currentTimeMillis()): Boolean {
        val since = chargedPendingSinceMs ?: return false
        if (nowMs - since < PENDING_CONFIRM_DELAY_MS) return false
        if (nowMs - lastChargedConfirmedAtMs <= CHARGED_DEBOUNCE_MS) {
            chargedPendingSinceMs = null
            return false
        }

        val candidates = listOfNotNull(charge1, charge2).distinctBy { it.moveId }
        if (candidates.isEmpty()) {
            chargedPendingSinceMs = null
            return false
        }

        val affordable = candidates.filter { tracker.energy >= it.chargedCost }
        val observed = activeState?.observedChargedMoveIds.orEmpty()
        val selected = when {
            affordable.size == 1 -> affordable.first()
            affordable.isNotEmpty() -> affordable.firstOrNull { it.moveId in observed }
                ?: affordable.minByOrNull { it.chargedCost }
            else -> candidates.firstOrNull { it.moveId in observed }
                ?: candidates.minByOrNull { it.chargedCost }
        } ?: return false

        return observedChargedUsed(selected, nowMs)
    }

    fun possibleFastMoves(): List<MoveDef> = enemyName?.let(repo::possibleFastMoves).orEmpty()
    fun possibleChargedMoves(): List<MoveDef> = enemyName?.let(repo::possibleChargedMoves).orEmpty()

    private fun switchSeconds(readyAtMs: Long, nowMs: Long = System.currentTimeMillis()): Int? {
        val remaining = readyAtMs - nowMs
        if (remaining <= 0L) return null
        return ((remaining + 999L) / 1000L).toInt()
    }

    fun uiState(debug: Boolean = false): BattleUiState {
        val pTypes = repo.pokemon(playerName)?.types.orEmpty()
        val eTypes = repo.pokemon(enemyName)?.types.orEmpty()
        val matchup = classify(pTypes, eTypes)
        val f = fast

        val reserveStates = ownTeam.entries
            .filterNot { it.key.equals(playerName, ignoreCase = true) }
            .take(2)
            .mapIndexed { index, (name, cp) ->
                val types = repo.pokemon(name)?.types.orEmpty()
                ReserveState(
                    name = name,
                    cp = cp,
                    types = types,
                    matchup = classify(types, eTypes),
                    visible = reserveSlotVisible.getOrNull(index) != false
                )
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
            ownSwitchSeconds = switchSeconds(ownSwitchReadyAtMs),
            opponentSwitchSeconds = switchSeconds(opponentSwitchReadyAtMs),
            chargedIncoming = chargedPendingSinceMs != null,
            detectorConfidence = lastDetectorConfidence,
            debugText = if (debug) buildString {
                val s = activeState
                appendLine("Jogador: ${playerName ?: "?"} PC ${playerCp ?: "?"} | time=${ownTeam.keys.joinToString()}")
                appendLine("Inimigo: ${enemyName ?: "?"} PC ${enemyCp ?: "?"}")
                appendLine("Fast: ${f?.name ?: "?"} | contagem=${tracker.fastMoveCount} manual=${s?.fastMoveManual == true}")
                appendLine("Energia=${tracker.energy}/100 | fonte=$lastAutoSource conf=${"%.2f".format(lastDetectorConfidence)}")
                appendLine("HP=${lastHpRatio?.let { "%.3f".format(it) } ?: "?"} | movimento=${"%.1f".format(lastMotion)}")
                appendLine("Reservas vis=${reserveSlotVisible.joinToString { it?.toString() ?: "?" }}")
                appendLine("C1=${charge1?.name ?: "?"} conf=${s?.charge1Confirmed} manual=${s?.charge1Manual}")
                appendLine("C2=${charge2?.name ?: "?"} conf=${s?.charge2Confirmed} manual=${s?.charge2Manual}")
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
        state.fastMoveId = fast?.moveId
        state.chargedMove1Id = charge1?.moveId
        state.chargedMove2Id = charge2?.moveId
        state.fastMoveCount = tracker.fastMoveCount
        state.estimatedEnergy = tracker.energy
        state.lastSeenAtMs = System.currentTimeMillis()
    }

    private fun normalizedMoveNames(move: MoveDef): Set<String> = buildSet {
        add(normalizeText(repo.localizedMoveName(move)))
        add(normalizeText(move.name))
        add(normalizeText(move.moveId.replace('_', ' ')))
    }.filterTo(linkedSetOf()) { it.isNotBlank() }

    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z0-9]+"), "")

    private fun normalizeText(s: String): String = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9]+"), "")

    companion object {
        private const val CHARGED_DEBOUNCE_MS = 4_200L
        private const val SWITCH_COOLDOWN_MS = 50_000L
        private const val RESERVE_MISSING_CONFIRM_FRAMES = 12
        private const val CHARGED_FAST_GUARD_MS = 2_200L
        private const val PENDING_CONFIRM_DELAY_MS = 2_400L

        private val CHARGED_PROMPTS = listOf(
            "ataquecarregado", "ataquechegando", "ataquecarregadoagora",
            "attackincoming", "chargemoveincoming", "usarumescudo", "useumescudo", "protectshield"
        )
        private val ACTION_VERBS = listOf("usou", "used", "utilizou")
    }
}
