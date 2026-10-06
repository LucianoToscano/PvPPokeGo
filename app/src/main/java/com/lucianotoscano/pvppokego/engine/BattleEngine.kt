package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.BattlePokemonState
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.DetectedPokemon
import com.lucianotoscano.pvppokego.data.EnergyConfidence
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.MoveKnowledgeConfidence
import com.lucianotoscano.pvppokego.data.ReserveCardEvidence
import com.lucianotoscano.pvppokego.data.ReserveState
import java.text.Normalizer

/**
 * Battle-domain state. Read-only relative to Pokemon GO: this class never sends input.
 * Enemy and own energy are preserved per Pokemon through switches.
 */
class BattleEngine(private val repo: GameDataRepository) {
    data class DetectionChanges(val playerChanged: Boolean, val enemyChanged: Boolean)
    data class BattleTextResult(
        val newChargedPrompt: Boolean = false,
        val chargedMoveConfirmed: Boolean = false,
        val ownChargedMoveConfirmed: Boolean = false,
        val enemyChargedMove: MoveDef? = null,
        val ownChargedMove: MoveDef? = null
    )

    private val enemyStates = BattleStateStore()
    private val ownStates = BattleStateStore()

    private var playerName: String? = null
    private var playerCp: Int? = null
    private var enemyName: String? = null
    private var enemyCp: Int? = null
    private val playerCpTracker = StableCpTracker()
    private val enemyCpTracker = StableCpTracker()

    private var tracker = EnergyTracker()
    private var activeState: BattlePokemonState? = null
    private var fast: MoveDef? = null
    private var charge1: MoveDef? = null
    private var charge2: MoveDef? = null

    private var ownTracker = EnergyTracker()
    private var ownActiveState: BattlePokemonState? = null
    private var ownFast: MoveDef? = null
    private var ownCharge1: MoveDef? = null
    private var ownCharge2: MoveDef? = null

    private data class OwnTeamSlot(
        val slot: Int,
        var name: String,
        var cp: Int?
    )

    private val ownTeam = linkedMapOf<String, Int?>()
    private val ownTeamSlots = mutableListOf<OwnTeamSlot>()
    private val reserveSlotVisible = arrayOfNulls<Boolean>(2)
    private val reserveMissingStreak = IntArray(2)
    private val reserveCardCps = arrayOfNulls<Int>(2)
    private val reserveCpCandidate = arrayOfNulls<Int>(2)
    private val reserveCpStreak = IntArray(2)
    private val reserveCardDexes = arrayOfNulls<Int>(2)
    private val reserveDexCandidate = arrayOfNulls<Int>(2)
    private val reserveDexStreak = IntArray(2)
    private val reserveCardSpeciesIds = arrayOfNulls<String>(2)
    private val reserveSpeciesCandidate = arrayOfNulls<String>(2)
    private val reserveSpeciesStreak = IntArray(2)
    private var ownSwitchReadyAtMs: Long = 0L
    private var opponentSwitchReadyAtMs: Long = 0L
    private var chargedPendingSinceMs: Long? = null
    private var lastChargedConfirmedAtMs: Long = 0L
    private val enemyChargedCycle = ChargedAnimationCycleGate()
    private val ownChargedCycle = ChargedAnimationCycleGate()
    private var ignoreFastUntilMs: Long = 0L
    private var lastAutoSource: String = "-"
    private var lastOwnAutoSource: String = "-"
    private var lastDetectorConfidence = 0f
    private var lastBattleConfidence = 0f
    private var lastHpRatio: Float? = null
    private var lastOpponentHpRatio: Float? = null
    private var lastMotion = 0f

    fun resetBattle() {
        playerName = null
        playerCp = null
        enemyName = null
        enemyCp = null
        playerCpTracker.reset()
        enemyCpTracker.reset()
        tracker.reset()
        ownTracker.reset()
        activeState = null
        ownActiveState = null
        fast = null
        charge1 = null
        charge2 = null
        ownFast = null
        ownCharge1 = null
        ownCharge2 = null
        ownTeam.clear()
        ownTeamSlots.clear()
        reserveSlotVisible.fill(null)
        reserveMissingStreak.fill(0)
        reserveCardCps.fill(null)
        reserveCpCandidate.fill(null)
        reserveCpStreak.fill(0)
        reserveCardDexes.fill(null)
        reserveDexCandidate.fill(null)
        reserveDexStreak.fill(0)
        reserveCardSpeciesIds.fill(null)
        reserveSpeciesCandidate.fill(null)
        reserveSpeciesStreak.fill(0)
        reserveCardSpeciesIds.fill(null)
        reserveSpeciesCandidate.fill(null)
        reserveSpeciesStreak.fill(0)
        ownSwitchReadyAtMs = 0L
        opponentSwitchReadyAtMs = 0L
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = 0L
        enemyChargedCycle.reset()
        ownChargedCycle.reset()
        ignoreFastUntilMs = 0L
        lastAutoSource = "-"
        lastOwnAutoSource = "-"
        lastDetectorConfidence = 0f
        lastBattleConfidence = 0f
        lastHpRatio = null
        lastOpponentHpRatio = null
        lastMotion = 0f
        enemyStates.clearBattle()
        ownStates.clearBattle()
    }

    fun onBattleConfidence(value: Float) {
        lastBattleConfidence = value.coerceIn(0f, 1f)
    }

    fun onDetection(d: BattleDetection): DetectionChanges {
        val oldPlayer = playerName
        val oldEnemy = enemyName

        d.player?.let {
            val canonical = repo.canonicalPokemonName(it.name) ?: it.name
            val stableCp = playerCpTracker.observe(normalize(canonical), it.cp)
            rememberOwnPokemon(canonical, stableCp)
            if (!canonical.equals(playerName, ignoreCase = true)) onPlayerChanged(canonical, stableCp)
            else if (stableCp != null) playerCp = stableCp
        }
        d.opponent?.let {
            val canonical = repo.canonicalPokemonName(it.name) ?: it.name
            val stableCp = enemyCpTracker.observe(normalize(canonical), it.cp)
            if (!canonical.equals(enemyName, ignoreCase = true)) {
                onEnemyChanged(canonical)
                enemyCp = stableCp
            } else if (stableCp != null) {
                enemyCp = stableCp
            }
        }

        val playerChanged = oldPlayer != null && !oldPlayer.equals(playerName, ignoreCase = true)
        val enemyChanged = oldEnemy != null && !oldEnemy.equals(enemyName, ignoreCase = true)
        val now = System.currentTimeMillis()
        if (playerChanged) {
            reserveSlotVisible.fill(null)
            reserveMissingStreak.fill(0)
            clearReserveCardIdentity()
            ownSwitchReadyAtMs = now + SWITCH_COOLDOWN_MS
        }
        if (enemyChanged) opponentSwitchReadyAtMs = now + SWITCH_COOLDOWN_MS
        return DetectionChanges(playerChanged, enemyChanged)
    }

    fun onOwnTeamDetected(team: List<DetectedPokemon>) {
        val canonical = team
            .map { p -> Triple(repo.canonicalPokemonName(p.name) ?: p.name, p.cp, p.confidence) }
            // Same species can exist on both sides (e.g. enemy Cramorant PC1483 and
            // own reserve Cramorant PC1485). Never discard an own Pokemon by name alone.
            .filterNot { (name, cp, _) ->
                val sameSpecies = name.equals(enemyName, ignoreCase = true)
                val sameExactCard = enemyCp != null && cp != null && enemyCp == cp
                sameSpecies && sameExactCard
            }
            .distinctBy { (name, cp, _) -> "${normalize(name)}:${cp ?: 0}" }
            .take(3)

        // A complete team scan defines stable slot identities for the whole battle.
        // Require name+CP for all three cards before replacing the stable mapping;
        // a partial/uncertain OCR result may enrich existing slots but may not reorder them.
        val completeTeam = canonical.size >= 3 && canonical.take(3).all { (_, cp, confidence) ->
            cp != null && confidence >= 0.90f
        }
        if (completeTeam) {
            ownTeamSlots.clear()
            canonical.forEachIndexed { index, (name, cp, _) ->
                ownTeamSlots += OwnTeamSlot(index, name, cp)
                rememberOwnPokemon(name, cp)
            }
        } else {
            canonical.forEach { (name, cp, _) ->
                rememberOwnPokemon(name, cp)
                val existing = ownTeamSlots.firstOrNull { it.name.equals(name, true) && (cp == null || it.cp == null || it.cp == cp) }
                if (existing != null) {
                    if (cp != null) existing.cp = cp
                } else if (ownTeamSlots.size < 3) {
                    ownTeamSlots += OwnTeamSlot(ownTeamSlots.size, name, cp)
                }
            }
        }
        rebindOwnActiveStateToStableSlot()
    }

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

    /**
     * Fuses OCR CP and visual National Dex evidence from Pokemon GO's two reserve cards.
     * CP always has priority. Image evidence is accepted only when it is strong and maps
     * to exactly one known team member.
     */
    fun onReserveCardEvidence(top: ReserveCardEvidence, bottom: ReserveCardEvidence) {
        fun updateCp(index: Int, cp: Int?) {
            if (cp == null) return

            val uniqueKnownCp = ownTeamSlots.count { it.cp == cp } == 1
            if (uniqueKnownCp) {
                reserveCardCps[index] = cp
                reserveCpCandidate[index] = cp
                reserveCpStreak[index] = 2
                return
            }

            if (reserveCpCandidate[index] == cp) {
                reserveCpStreak[index]++
            } else {
                reserveCpCandidate[index] = cp
                reserveCpStreak[index] = 1
            }
            if (reserveCpStreak[index] >= 2) reserveCardCps[index] = cp
        }

        fun updateSpecies(index: Int, evidence: ReserveCardEvidence) {
            val speciesId = evidence.visualSpeciesId?.takeIf { it.isNotBlank() } ?: return
            val confidence = evidence.visualConfidence ?: return
            if (confidence < RESERVE_VISUAL_MIN_CONFIDENCE) return

            val matchingMembers = ownTeamSlots.filter { slot ->
                repo.pokemon(slot.name)?.speciesId.equals(speciesId, ignoreCase = true)
            }
            if (matchingMembers.size != 1) return

            if (reserveSpeciesCandidate[index].equals(speciesId, ignoreCase = true)) {
                reserveSpeciesStreak[index]++
            } else {
                reserveSpeciesCandidate[index] = speciesId
                reserveSpeciesStreak[index] = 1
            }

            if (
                confidence >= RESERVE_VISUAL_IMMEDIATE_CONFIDENCE ||
                reserveSpeciesStreak[index] >= RESERVE_VISUAL_CONFIRM_FRAMES
            ) {
                reserveCardSpeciesIds[index] = speciesId
            }
        }

        fun updateDex(index: Int, evidence: ReserveCardEvidence) {
            val dex = evidence.visualDex ?: return
            val confidence = evidence.visualConfidence ?: return
            if (confidence < RESERVE_VISUAL_MIN_CONFIDENCE) return

            val matchingMembers = ownTeamSlots.filter { slot ->
                repo.pokemon(slot.name)?.dex?.takeIf { it > 0 } == dex
            }
            if (matchingMembers.size != 1) return

            if (reserveDexCandidate[index] == dex) {
                reserveDexStreak[index]++
            } else {
                reserveDexCandidate[index] = dex
                reserveDexStreak[index] = 1
            }

            if (
                confidence >= RESERVE_VISUAL_IMMEDIATE_CONFIDENCE ||
                reserveDexStreak[index] >= RESERVE_VISUAL_CONFIRM_FRAMES
            ) {
                reserveCardDexes[index] = dex
            }
        }

        listOf(top, bottom).forEachIndexed { index, evidence ->
            updateCp(index, evidence.cp)
            updateSpecies(index, evidence)
            updateDex(index, evidence)
        }
    }

    fun onReserveCardCps(topCp: Int?, bottomCp: Int?) =
        onReserveCardEvidence(ReserveCardEvidence(topCp), ReserveCardEvidence(bottomCp))

    fun needsReserveCardScan(): Boolean =
        ownTeamSlots.size >= 2 &&
            reserveCardCps.indices.any { index ->
                reserveCardCps[index] == null &&
                    reserveCardSpeciesIds[index] == null &&
                    reserveCardDexes[index] == null
            }

    private fun clearReserveCardIdentity() {
        reserveCardCps.fill(null)
        reserveCpCandidate.fill(null)
        reserveCpStreak.fill(0)
        reserveCardDexes.fill(null)
        reserveDexCandidate.fill(null)
        reserveDexStreak.fill(0)
    }

    fun needsOwnTeamScan(): Boolean =
        ownTeamSlots.size < 3 || ownTeamSlots.any { it.cp == null }
    fun battleCorrespondenceContext(
        inactiveForMs: Long,
        sessionAgeMs: Long
    ): BattleCorrespondenceTracker.Context {
        fun identity(name: String?, cp: Int?): BattleCorrespondenceTracker.Identity? {
            if (name.isNullOrBlank()) return null
            val def = repo.pokemon(name)
            return BattleCorrespondenceTracker.Identity(
                name = name,
                speciesId = def?.speciesId,
                cp = cp,
                confidence = 1f
            )
        }

        val team = if (ownTeamSlots.isNotEmpty()) {
            ownTeamSlots.mapNotNull { identity(it.name, it.cp) }
        } else {
            ownTeam.mapNotNull { (name, cp) -> identity(name, cp) }
        }
        val opponents = enemyStates.all()
            .mapNotNull { state -> identity(state.speciesName, null) }
            .distinctBy { it.stableKey() }

        return BattleCorrespondenceTracker.Context(
            activePlayer = identity(playerName, playerCp),
            activeOpponent = identity(enemyName, enemyCp),
            ownTeam = team,
            knownOpponents = opponents,
            inactiveForMs = inactiveForMs.coerceAtLeast(0L),
            sessionAgeMs = sessionAgeMs.coerceAtLeast(0L)
        )
    }

    fun battleCorrespondenceObservation(
        detection: BattleDetection,
        pairedCards: Boolean,
        battleTextEvidence: Boolean,
        switchPrompt: Boolean
    ): BattleCorrespondenceTracker.Observation {
        fun identity(pokemon: DetectedPokemon?): BattleCorrespondenceTracker.Identity? {
            pokemon ?: return null
            val def = pokemon.visualSpeciesId
                ?.let(repo::pokemon)
                ?: repo.pokemon(pokemon.name)
            val confidence = maxOf(
                pokemon.confidence,
                pokemon.visualConfidence ?: 0f
            ).coerceIn(0f, 1f)
            return BattleCorrespondenceTracker.Identity(
                name = def?.speciesName ?: pokemon.name,
                speciesId = pokemon.visualSpeciesId ?: def?.speciesId,
                cp = pokemon.cp,
                confidence = confidence
            )
        }
        return BattleCorrespondenceTracker.Observation(
            player = identity(detection.player),
            opponent = identity(detection.opponent),
            pairedCards = pairedCards,
            battleTextEvidence = battleTextEvidence,
            switchPrompt = switchPrompt
        )
    }

    fun activePlayerName(): String? = playerName
    fun activeEnemyName(): String? = enemyName
    fun latestPlayerHpRatio(): Float? = lastHpRatio
    fun latestOpponentHpRatio(): Float? = lastOpponentHpRatio
    fun currentFastDurationMs(): Long = effectiveEnemyFastMove()?.cooldown?.toLong()?.coerceIn(500L, 3_500L) ?: 1_000L
    fun currentOwnFastDurationMs(): Long = ownFast?.cooldown?.toLong()?.coerceIn(500L, 3_500L) ?: 1_000L

    private fun rememberOwnPokemon(name: String, cp: Int?) {
        val existingKey = ownTeam.keys.firstOrNull { it.equals(name, ignoreCase = true) }
        if (existingKey == null && ownTeam.size < 3) ownTeam[name] = cp
        else if (existingKey != null && cp != null) ownTeam[existingKey] = cp

        val slot = ownTeamSlots.firstOrNull {
            it.name.equals(name, ignoreCase = true) && (cp == null || it.cp == null || it.cp == cp)
        }
        if (slot != null && cp != null) slot.cp = cp
    }

    private fun ownIdentityKey(name: String, cp: Int?): String {
        val exact = ownTeamSlots.firstOrNull {
            it.name.equals(name, ignoreCase = true) && (cp == null || it.cp == null || it.cp == cp)
        }
        return if (exact != null) {
            "slot${exact.slot}:${normalize(exact.name)}:${exact.cp ?: cp ?: 0}"
        } else {
            "own:${normalize(name)}:${cp ?: 0}"
        }
    }

    private fun rebindOwnActiveStateToStableSlot() {
        val name = playerName ?: return
        val old = ownActiveState ?: return
        val stableKey = ownIdentityKey(name, playerCp)
        if (old.key == stableKey) return
        val target = ownStates.getOrCreate(stableKey, name)
        target.fastMoveId = old.fastMoveId
        target.chargedMove1Id = old.chargedMove1Id
        target.chargedMove2Id = old.chargedMove2Id
        target.fastMoveCount = old.fastMoveCount
        target.estimatedEnergy = old.estimatedEnergy
        target.minEnergy = old.minEnergy
        target.maxEnergy = old.maxEnergy
        target.energyUncertain = old.energyUncertain
        target.lastChargedMoveId = old.lastChargedMoveId
        target.charge1Confirmed = old.charge1Confirmed
        target.charge2Confirmed = old.charge2Confirmed
        target.fastMoveManual = old.fastMoveManual
        target.charge1Manual = old.charge1Manual
        target.charge2Manual = old.charge2Manual
        target.observedChargedMoveIds.clear()
        target.observedChargedMoveIds.addAll(old.observedChargedMoveIds)
        target.lastSeenAtMs = old.lastSeenAtMs
        ownActiveState = target
    }

    private fun onPlayerChanged(name: String, cp: Int?) {
        saveOwnActiveState()
        ownChargedCycle.reset()
        playerName = name
        playerCp = cp ?: ownTeam.entries.firstOrNull { it.key.equals(name, true) }?.value
        val key = ownIdentityKey(name, playerCp)
        ownActiveState = ownStates.getOrCreate(key, name)
        val saved = ownActiveState!!
        val likely = repo.likelyMoveset(name)
        ownFast = repo.move(saved.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        resolveChargedPair(name, saved.chargedMove1Id, saved.chargedMove2Id, likely.second, likely.third).also { (c1, c2) ->
            ownCharge1 = c1
            ownCharge2 = c2
        }
        val minE = if (saved.maxEnergy > 0 || saved.minEnergy > 0 || saved.energyUncertain) saved.minEnergy else saved.estimatedEnergy
        val maxE = if (saved.maxEnergy > 0 || saved.minEnergy > 0 || saved.energyUncertain) saved.maxEnergy else saved.estimatedEnergy
        ownTracker.restoreRange(minE, maxE, saved.fastMoveCount)
        saveOwnActiveState()
    }

    private fun onEnemyChanged(name: String) {
        saveActiveState()
        enemyName = name
        chargedPendingSinceMs = null
        enemyChargedCycle.reset()
        ignoreFastUntilMs = 0L
        val key = normalize(name)
        activeState = enemyStates.getOrCreate(key, name)
        val saved = activeState!!
        val likely = repo.likelyMoveset(name)
        fast = repo.move(saved.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        resolveChargedPair(name, saved.chargedMove1Id, saved.chargedMove2Id, likely.second, likely.third).also { (c1, c2) ->
            charge1 = c1
            charge2 = c2
        }
        val minE = if (saved.maxEnergy > 0 || saved.minEnergy > 0 || saved.energyUncertain) saved.minEnergy else saved.estimatedEnergy
        val maxE = if (saved.maxEnergy > 0 || saved.minEnergy > 0 || saved.energyUncertain) saved.maxEnergy else saved.estimatedEnergy
        tracker.restoreRange(minE, maxE, saved.fastMoveCount)
        saveActiveState()
    }

    fun selectFast(move: MoveDef) {
        fast = move
        activeState?.apply { fastMoveId = move.moveId; fastMoveManual = true }
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
        activeState?.apply { chargedMove1Id = move.moveId; charge1Manual = true; charge1Confirmed = false }
        saveActiveState()
    }

    fun selectCharged2(move: MoveDef) {
        charge2 = move
        activeState?.apply { chargedMove2Id = move.moveId; charge2Manual = true; charge2Confirmed = false }
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
                    charge1 = recommended; state.chargedMove1Id = recommended.moveId
                    state.charge1Confirmed = recommended.moveId in state.observedChargedMoveIds
                }
            }
            1 -> {
                state.charge2Manual = false
                val recommended = likely.third ?: repo.possibleChargedMoves(state.speciesName).getOrNull(1)
                if (recommended != null && recommended.moveId != charge1?.moveId) {
                    charge2 = recommended; state.chargedMove2Id = recommended.moveId
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
        if (System.currentTimeMillis() < ignoreFastUntilMs) { lastAutoSource = "charged-guard"; return }
        val f = effectiveEnemyFastMove() ?: return
        if (fast?.moveId != f.moveId) {
            fast = f
            activeState?.fastMoveId = f.moveId
        }
        tracker.onFastMove(f, count)
        saveActiveState()
    }

    fun onOwnFastDetected(count: Int, confidence: Float, source: String, opponentHpRatio: Float?) {
        lastDetectorConfidence = maxOf(lastDetectorConfidence, confidence)
        lastOwnAutoSource = source
        lastOpponentHpRatio = opponentHpRatio
        if (count <= 0) return
        val f = ownFast ?: return
        ownTracker.onFastMove(f, count)
        saveOwnActiveState()
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

    private fun observedUnknownChargedUsed(nowMs: Long = System.currentTimeMillis()): Boolean {
        val candidates = possibleChargedMoves().ifEmpty { listOfNotNull(charge1, charge2) }
        if (!tracker.onObservedUnknownCharged(candidates)) return false
        chargedPendingSinceMs = null
        lastChargedConfirmedAtMs = nowMs
        ignoreFastUntilMs = maxOf(ignoreFastUntilMs, nowMs + CHARGED_FAST_GUARD_MS)
        lastAutoSource = "charged:unknown-range"
        saveActiveState()
        return true
    }

    private fun observedOwnChargedUsed(move: MoveDef): Boolean {
        ownTracker.onObservedChargedMove(move)
        val state = ownActiveState
        if (state != null) {
            state.lastChargedMoveId = move.moveId
            state.observedChargedMoveIds += move.moveId
            if (move.moveId == ownCharge1?.moveId) state.charge1Confirmed = true
            if (move.moveId == ownCharge2?.moveId) state.charge2Confirmed = true
        }
        lastOwnAutoSource = "charged:${move.moveId}"
        saveOwnActiveState()
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
        val replaceIndex = ChargedMoveSlotResolver.choose(
            observedMoveId = move.moveId,
            slot1Id = charge1?.moveId,
            slot2Id = charge2?.moveId,
            slot1Manual = state.charge1Manual,
            slot2Manual = state.charge2Manual,
            slot1Confirmed = state.charge1Confirmed,
            slot2Confirmed = state.charge2Confirmed
        )
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
        if (raw.isBlank()) {
            ownChargedCycle.observe(null, nowMs)
            enemyChargedCycle.observe(null, nowMs)
            return BattleTextResult()
        }

        val text = normalizeText(raw)
        var newPrompt = false

        if (CHARGED_PROMPTS.any { text.contains(it) }) {
            if (chargedPendingSinceMs == null && nowMs - lastChargedConfirmedAtMs > UNKNOWN_CHARGED_DEBOUNCE_MS) {
                chargedPendingSinceMs = nowMs
                newPrompt = true
            }
        }

        val actionVerbPresent = ACTION_VERBS.any { text.contains(it) }
        if (!actionVerbPresent) {
            ownChargedCycle.observe(null, nowMs)
            enemyChargedCycle.observe(null, nowMs)
            return BattleTextResult(newPrompt, false, false)
        }

        val normalizedPlayer = playerName?.let(::normalizeText).orEmpty()
        val normalizedEnemy = enemyName?.let(::normalizeText).orEmpty()
        val playerActor = normalizedPlayer.isNotBlank() && text.contains(normalizedPlayer)
        val enemyActor = normalizedEnemy.isNotBlank() && text.contains(normalizedEnemy)

        if (playerActor) {
            enemyChargedCycle.observe(null, nowMs)
            val ownCandidates = playerName
                ?.let(repo::possibleChargedMoves)
                .orEmpty()
                .ifEmpty { listOfNotNull(ownCharge1, ownCharge2) }
            val ownMatched = findMoveMention(text, ownCandidates)
            if (ownMatched != null) {
                val signature = ChargedAnimationCycleGate.Signature(
                    actorKey = normalizeText(playerName ?: "voce"),
                    moveId = ownMatched.moveId
                )
                if (ownChargedCycle.observe(signature, nowMs)) {
                    observedOwnChargedUsed(ownMatched)
                    return BattleTextResult(newPrompt, false, true, ownChargedMove = ownMatched)
                }
                return BattleTextResult(newPrompt, false, false)
            }
            ownChargedCycle.observe(null, nowMs)
        }

        if (enemyActor || !playerActor) {
            ownChargedCycle.observe(null, nowMs)
            val candidates = possibleChargedMoves().ifEmpty { listOfNotNull(charge1, charge2) }
            val matched = findMoveMention(text, candidates)
            if (matched != null) {
                val signature = ChargedAnimationCycleGate.Signature(
                    actorKey = normalizeText(enemyName ?: "inimigo"),
                    moveId = matched.moveId
                )
                if (enemyChargedCycle.observe(signature, nowMs)) {
                    observedChargedUsed(matched, nowMs)
                    return BattleTextResult(newPrompt, true, false, enemyChargedMove = matched)
                }
                return BattleTextResult(newPrompt, false, false)
            }
            enemyChargedCycle.observe(null, nowMs)
            if (chargedPendingSinceMs == null && nowMs - lastChargedConfirmedAtMs > UNKNOWN_CHARGED_DEBOUNCE_MS) {
                chargedPendingSinceMs = nowMs
                newPrompt = true
            }
        }
        return BattleTextResult(newPrompt, false, false)
    }

    private fun findMoveMention(text: String, candidates: List<MoveDef>): MoveDef? = candidates
        .distinctBy { it.moveId }
        .map { it to normalizedMoveNames(it) }
        .firstOrNull { (_, names) -> names.any { n -> n.length >= 4 && text.contains(n) } }
        ?.first

    fun maybeConfirmPendingCharged(nowMs: Long = System.currentTimeMillis()): Boolean {
        val since = chargedPendingSinceMs ?: return false
        if (nowMs - since < PENDING_CONFIRM_DELAY_MS) return false
        if (nowMs - lastChargedConfirmedAtMs <= UNKNOWN_CHARGED_DEBOUNCE_MS) {
            chargedPendingSinceMs = null
            return false
        }
        return observedUnknownChargedUsed(nowMs)
    }

    /**
     * Defensive fallback: energy tracking must use an actual Fast Move with positive
     * energy gain. If ranking/default data selected an invalid/zero-energy move,
     * use the first valid Fast Move available for that species.
     */
    private fun effectiveEnemyFastMove(): MoveDef? {
        fast?.takeIf { it.energyGain > 0 }?.let { return it }
        val fallback = enemyName
            ?.let(repo::possibleFastMoves)
            .orEmpty()
            .firstOrNull { it.energyGain > 0 }
        return fallback
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
        val of = ownFast

        val rawReserves = resolvedOwnReserves()
            .take(2)
            .mapIndexed { index, resolved ->
                val slot = resolved.slot
                val name = slot.name
                val cp = slot.cp
                val types = repo.pokemon(name)?.types.orEmpty()
                val identity = ownIdentityKey(name, cp)
                val saved = ownStates.find(identity)
                val likely = repo.likelyMoveset(name)
                val reserveFast = repo.move(saved?.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
                val (reserveC1, reserveC2) = resolveChargedPair(
                    name,
                    saved?.chargedMove1Id,
                    saved?.chargedMove2Id,
                    likely.second,
                    likely.third
                )
                val score = if (resolved.confirmed) reserveScore(name, cp, types, eTypes) else 0.0
                ReserveState(
                    name = name,
                    cp = cp,
                    types = types,
                    matchup = classify(types, eTypes),
                    visible = reserveSlotVisible.getOrNull(index) != false,
                    score = score,
                    teamSlot = slot.slot,
                    identityKey = identity,
                    cardCp = resolved.cardCp,
                    identityConfirmed = resolved.confirmed,
                    fastMove = reserveFast,
                    chargedMove1 = reserveC1,
                    chargedMove2 = reserveC2,
                    fastMoveKnowledge = moveKnowledge(saved, 0, reserveFast),
                    chargedMove1Knowledge = moveKnowledge(saved, 1, reserveC1),
                    chargedMove2Knowledge = moveKnowledge(saved, 2, reserveC2)
                )
            }
        val bestVisibleScore = rawReserves
            .filter { it.visible && it.identityConfirmed }
            .maxOfOrNull { it.score }
        val reserveStates = rawReserves.map { r ->
            val label = when {
                !r.visible || !r.identityConfirmed -> null
                bestVisibleScore != null && r.score >= bestVisibleScore - .08 && r.score > .05 -> "MELHOR"
                r.score < -.35 -> "EVITAR"
                else -> "NEUTRO"
            }
            r.copy(recommendation = label)
        }

        val enemyConf = enemyEnergyConfidence()
        val ownConf = ownEnergyConfidence()
        val enemyHistory = enemyStates.all()
            .sortedByDescending { it.lastSeenAtMs }
            .take(3)
            .map { s ->
                val e = if (s.energyUncertain || s.minEnergy != s.maxEnergy) "${s.minEnergy}-${s.maxEnergy}E" else "${s.estimatedEnergy}E"
                val seen = s.lastChargedMoveId?.let(repo::move)?.name
                buildString {
                    append(s.speciesName)
                    append(": ${s.fastMoveCount}F • $e")
                    if (!seen.isNullOrBlank()) append(" • $seen")
                }
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
            charged1 = if (f != null && charge1 != null) tracker.predict(charge1!!, f, enemyConf) else null,
            charged2 = if (f != null && charge2 != null) tracker.predict(charge2!!, f, enemyConf) else null,
            fastMoveKnowledge = moveKnowledge(activeState, 0, f),
            charged1Knowledge = moveKnowledge(activeState, 1, charge1),
            charged2Knowledge = moveKnowledge(activeState, 2, charge2),
            ownFastMove = of,
            ownCharged1 = if (of != null && ownCharge1 != null) ownTracker.predict(ownCharge1!!, of, ownConf) else null,
            ownCharged2 = if (of != null && ownCharge2 != null) ownTracker.predict(ownCharge2!!, of, ownConf) else null,
            ownFastMoveKnowledge = moveKnowledge(ownActiveState, 0, of),
            ownCharged1Knowledge = moveKnowledge(ownActiveState, 1, ownCharge1),
            ownCharged2Knowledge = moveKnowledge(ownActiveState, 2, ownCharge2),
            ownSwitchSeconds = switchSeconds(ownSwitchReadyAtMs),
            opponentSwitchSeconds = switchSeconds(opponentSwitchReadyAtMs),
            chargedIncoming = chargedPendingSinceMs != null,
            detectorConfidence = lastDetectorConfidence,
            battleConfidence = lastBattleConfidence,
            enemyHistory = enemyHistory,
            debugText = if (debug) buildString {
                val s = activeState
                val os = ownActiveState
                appendLine("Jogador: ${playerName ?: "?"} PC ${playerCp ?: "?"} | slots=${ownTeamSlots.joinToString { "#${it.slot + 1} ${it.name}/${it.cp ?: "?"}" }}")
                appendLine("Own fast: ${of?.name ?: "?"} count=${ownTracker.fastMoveCount} E=${ownTracker.minEnergy}-${ownTracker.maxEnergy} src=$lastOwnAutoSource")
                appendLine("Inimigo: ${enemyName ?: "?"} PC ${enemyCp ?: "?"}")
                appendLine("Fast: ${f?.name ?: "?"} | contagem=${tracker.fastMoveCount} manual=${s?.fastMoveManual == true}")
                appendLine("Energia=${tracker.minEnergy}-${tracker.maxEnergy}/100 | fonte=$lastAutoSource conf=${"%.2f".format(lastDetectorConfidence)}")
                appendLine("Battle conf=${"%.2f".format(lastBattleConfidence)} HP=${lastHpRatio?.let { "%.3f".format(it) } ?: "?"}/${lastOpponentHpRatio?.let { "%.3f".format(it) } ?: "?"}")
                appendLine("Reservas vis=${reserveSlotVisible.joinToString { it?.toString() ?: "?" }} CPcards=${reserveCardCps.joinToString { it?.toString() ?: "?" }} DEXcards=${reserveCardDexes.joinToString { it?.toString() ?: "?" }} FORMS=${reserveCardSpeciesIds.joinToString { it ?: "?" }}")
                appendLine("C1=${charge1?.name ?: "?"} conf=${s?.charge1Confirmed} manual=${s?.charge1Manual}")
                appendLine("C2=${charge2?.name ?: "?"} conf=${s?.charge2Confirmed} manual=${s?.charge2Manual}")
                append("Own C1=${ownCharge1?.name ?: "?"} C2=${ownCharge2?.name ?: "?"} state=${os?.speciesName ?: "?"}")
            } else null
        )
    }

    private fun enemyEnergyConfidence(): EnergyConfidence = when {
        tracker.isRange -> EnergyConfidence.RANGE
        activeState?.fastMoveManual == true || possibleFastMoves().size <= 1 -> EnergyConfidence.CONFIRMED
        else -> EnergyConfidence.ESTIMATED
    }

    private fun ownEnergyConfidence(): EnergyConfidence = when {
        ownTracker.isRange -> EnergyConfidence.RANGE
        (playerName?.let(repo::possibleFastMoves)?.size ?: 0) <= 1 -> EnergyConfidence.CONFIRMED
        else -> EnergyConfidence.ESTIMATED
    }

    private fun reserveScore(name: String, cp: Int?, myTypes: List<String>, enemyTypes: List<String>): Double {
        if (myTypes.isEmpty() || enemyTypes.isEmpty()) return 0.0
        val saved = ownStates.find(ownIdentityKey(name, cp))
        val likely = repo.likelyMoveset(name)
        val resolvedFast = repo.move(saved?.fastMoveId) ?: likely.first ?: repo.possibleFastMoves(name).firstOrNull()
        val (resolvedC1, resolvedC2) = resolveChargedPair(name, saved?.chargedMove1Id, saved?.chargedMove2Id, likely.second, likely.third)
        val myMoves = listOfNotNull(resolvedFast, resolvedC1, resolvedC2)
        val enemyLikely = enemyName?.let(repo::likelyMoveset)
        val enemyMoves = listOfNotNull(enemyLikely?.first, enemyLikely?.second, enemyLikely?.third)
        val offense = myMoves.maxOfOrNull { TypeChart.multiplier(it.type, enemyTypes) } ?: 1.0
        val danger = enemyMoves.maxOfOrNull { TypeChart.multiplier(it.type, myTypes) } ?: 1.0
        val typeBase = when (classify(myTypes, enemyTypes)) {
            MatchupState.FAVORABLE -> .35
            MatchupState.UNFAVORABLE -> -.35
            else -> 0.0
        }
        return typeBase + (offense - 1.0) * .75 - (danger - 1.0) * .65
    }

    private data class ResolvedOwnReserve(
        val slot: OwnTeamSlot,
        val cardCp: Int?,
        val confirmed: Boolean
    )

    /**
     * Native reserve-card CPs are authoritative. This replaces the old circular-slot
     * assumption that could place Houndoom's Fire/Dark types under a Rillaboom card.
     */
    private fun resolvedOwnReserves(): List<ResolvedOwnReserve> {
        val slots = if (ownTeamSlots.isNotEmpty()) ownTeamSlots.toList() else {
            ownTeam.entries.mapIndexed { index, (name, cp) -> OwnTeamSlot(index, name, cp) }
        }
        if (slots.isEmpty()) return emptyList()

        val members = slots.map { slot ->
            ReserveCardMatcher.Member(
                slot = slot.slot,
                name = slot.name,
                cp = slot.cp,
                dex = repo.pokemon(slot.name)?.dex?.takeIf { it > 0 },
                speciesId = repo.pokemon(slot.name)?.speciesId
            )
        }
        return ReserveCardMatcher.resolve(
            team = members,
            activeName = playerName,
            activeCp = playerCp,
            cardCps = reserveCardCps.toList(),
            cardDexes = reserveCardDexes.toList(),
            cardSpeciesIds = reserveCardSpeciesIds.toList()
        ).mapNotNull { match ->
            slots.firstOrNull { it.slot == match.member.slot }?.let { slot ->
                ResolvedOwnReserve(slot, match.cardCp, match.confirmed)
            }
        }
    }

    /** Picks two distinct charged moves and never reuses slot #1 as slot #2. */
    private fun resolveChargedPair(
        speciesName: String,
        savedMove1Id: String?,
        savedMove2Id: String?,
        preferred1: MoveDef?,
        preferred2: MoveDef?
    ): Pair<MoveDef?, MoveDef?> {
        val candidates = linkedMapOf<String, MoveDef>()
        fun add(move: MoveDef?) {
            if (move != null) candidates.putIfAbsent(move.moveId, move)
        }
        val saved1 = repo.move(savedMove1Id)
        val saved2 = repo.move(savedMove2Id)
        add(saved1); add(saved2); add(preferred1); add(preferred2)
        repo.possibleChargedMoves(speciesName).forEach(::add)

        val first = saved1 ?: preferred1 ?: candidates.values.firstOrNull()
        val second = saved2?.takeIf { it.moveId != first?.moveId }
            ?: preferred2?.takeIf { it.moveId != first?.moveId }
            ?: candidates.values.firstOrNull { it.moveId != first?.moveId }
        return first to second
    }

    private fun moveKnowledge(state: BattlePokemonState?, slot: Int, move: MoveDef?): MoveKnowledgeConfidence {
        if (move == null) return MoveKnowledgeConfidence.UNKNOWN
        if (state == null) return MoveKnowledgeConfidence.RANKED
        return when (slot) {
            0 -> if (state.fastMoveManual) MoveKnowledgeConfidence.MANUAL else MoveKnowledgeConfidence.RANKED
            1 -> when {
                state.charge1Manual -> MoveKnowledgeConfidence.MANUAL
                state.charge1Confirmed || move.moveId in state.observedChargedMoveIds -> MoveKnowledgeConfidence.CONFIRMED
                else -> MoveKnowledgeConfidence.RANKED
            }
            2 -> when {
                state.charge2Manual -> MoveKnowledgeConfidence.MANUAL
                state.charge2Confirmed || move.moveId in state.observedChargedMoveIds -> MoveKnowledgeConfidence.CONFIRMED
                else -> MoveKnowledgeConfidence.RANKED
            }
            else -> MoveKnowledgeConfidence.UNKNOWN
        }
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
        state.minEnergy = tracker.minEnergy
        state.maxEnergy = tracker.maxEnergy
        state.energyUncertain = tracker.isRange
        state.lastSeenAtMs = System.currentTimeMillis()
    }

    private fun saveOwnActiveState() {
        val state = ownActiveState ?: return
        state.fastMoveId = ownFast?.moveId
        state.chargedMove1Id = ownCharge1?.moveId
        state.chargedMove2Id = ownCharge2?.moveId
        state.fastMoveCount = ownTracker.fastMoveCount
        state.estimatedEnergy = ownTracker.energy
        state.minEnergy = ownTracker.minEnergy
        state.maxEnergy = ownTracker.maxEnergy
        state.energyUncertain = ownTracker.isRange
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
        private const val RESERVE_VISUAL_MIN_CONFIDENCE = 0.76f
        private const val RESERVE_VISUAL_IMMEDIATE_CONFIDENCE = 0.88f
        private const val RESERVE_VISUAL_CONFIRM_FRAMES = 2
        private const val UNKNOWN_CHARGED_DEBOUNCE_MS = 4_200L
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
