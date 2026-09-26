package pvppokego.core.battle

import kotlin.math.max
import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.EnemyPokemonState
import pvppokego.core.model.EnergyRangeEstimate
import pvppokego.core.model.EvidenceLevel
import pvppokego.core.model.MoveData
import pvppokego.core.model.PokemonBattleStatus

/** Tracks the opponent team independently from the overlay UI. */
class BattleEnergyTracker(
    private val repository: PvpokeRepository,
    private val leagueCp: Int = 1500,
    private val maxEnergy: Int = 100
) {
    private val states = mutableMapOf<Int, EnemyPokemonState>()
    private val energyRanges = mutableMapOf<Int, EnergyRangeEstimate>()
    private var activeSlot: Int? = null

    init {
        resetBattle()
    }

    fun resetBattle() {
        states.clear()
        energyRanges.clear()
        repeat(TEAM_SIZE) { slot ->
            states[slot] = EnemyPokemonState(slot = slot)
            energyRanges[slot] = EnergyRangeEstimate(0, 0, EvidenceLevel.CONFIRMED)
        }
        activeSlot = null
    }

    fun identifyPokemon(
        slot: Int,
        speciesId: String,
        fastMoveId: String? = null,
        evidence: EvidenceLevel = EvidenceLevel.PROBABLE
    ): EnemyPokemonState {
        requireSlot(slot)
        val normalizedSpecies = speciesId.lowercase()
        val existing = state(slot)
        val samePokemon = existing.speciesId == normalizedSpecies
        val recommendation = repository.recommendedMovesets(speciesId, leagueCp).firstOrNull()

        val next = if (samePokemon) {
            existing.copy(
                speciesEvidence = stronger(existing.speciesEvidence, evidence),
                fastMoveId = fastMoveId?.normalizeMoveId()
                    ?: existing.fastMoveId
                    ?: recommendation?.fastMoveId,
                fastMoveEvidence = when {
                    fastMoveId != null -> stronger(existing.fastMoveEvidence, evidence)
                    existing.fastMoveId != null -> existing.fastMoveEvidence
                    recommendation?.fastMoveId != null -> EvidenceLevel.PREDICTED
                    else -> existing.fastMoveEvidence
                },
                selectedChargedMoveIds = if (existing.selectedChargedMoveIds.isNotEmpty()) {
                    existing.selectedChargedMoveIds
                } else {
                    recommendation?.chargedMoveIds.orEmpty()
                        .map(String::normalizeMoveId)
                        .distinct()
                        .take(MAX_CHARGED_BUTTONS)
                }
            )
        } else {
            energyRanges[slot] = EnergyRangeEstimate(0, 0, EvidenceLevel.CONFIRMED)
            EnemyPokemonState(
                slot = slot,
                speciesId = normalizedSpecies,
                speciesEvidence = evidence,
                fastMoveId = fastMoveId?.normalizeMoveId() ?: recommendation?.fastMoveId,
                fastMoveEvidence = if (fastMoveId != null) evidence else EvidenceLevel.PREDICTED,
                selectedChargedMoveIds = recommendation?.chargedMoveIds.orEmpty()
                    .map(String::normalizeMoveId)
                    .distinct()
                    .take(MAX_CHARGED_BUTTONS),
                status = PokemonBattleStatus.BENCHED
            )
        }

        states[slot] = next
        return next
    }

    fun availableFastMoves(slot: Int): List<MoveData> {
        val current = state(slot)
        val pokemon = current.speciesId?.let(repository::pokemon) ?: return emptyList()
        return pokemon.fastMoveIds
            .mapNotNull(repository::move)
            .filter(MoveData::isFastMove)
            .sortedBy(MoveData::name)
    }

    fun availableChargedMoves(slot: Int): List<MoveData> {
        val current = state(slot)
        val pokemon = current.speciesId?.let(repository::pokemon) ?: return emptyList()
        return pokemon.chargedMoveIds
            .mapNotNull(repository::move)
            .filter(MoveData::isChargedMove)
            .sortedBy(MoveData::name)
    }

    fun setFastMove(
        slot: Int,
        fastMoveId: String,
        evidence: EvidenceLevel = EvidenceLevel.CONFIRMED
    ): EnemyPokemonState {
        val normalized = fastMoveId.normalizeMoveId()
        val current = state(slot)
        val legal = availableFastMoves(slot).map(MoveData::moveId).toSet()
        if (legal.isNotEmpty()) {
            require(normalized in legal) {
                "$normalized is not a fast move for ${current.speciesId}"
            }
        }
        val next = current.copy(fastMoveId = normalized, fastMoveEvidence = evidence)
        states[slot] = next
        return next
    }

    fun setChargedMoves(slot: Int, moveIds: Collection<String>): EnemyPokemonState {
        val current = state(slot)
        val normalized = moveIds
            .map(String::normalizeMoveId)
            .distinct()
            .take(MAX_CHARGED_BUTTONS)
        val legal = availableChargedMoves(slot).map(MoveData::moveId).toSet()
        if (legal.isNotEmpty()) {
            require(normalized.all { it in legal }) {
                "One or more charged moves are invalid for ${current.speciesId}: $normalized"
            }
        }
        val next = current.copy(
            selectedChargedMoveIds = normalized,
            confirmedChargedMoveIds = current.confirmedChargedMoveIds.intersect(normalized.toSet())
        )
        states[slot] = next
        return next
    }

    fun setChargedMove(slot: Int, buttonIndex: Int, moveId: String): EnemyPokemonState {
        require(buttonIndex in 0 until MAX_CHARGED_BUTTONS) {
            "buttonIndex must be 0 or 1"
        }
        val current = state(slot)
        val normalized = moveId.normalizeMoveId()
        val legal = availableChargedMoves(slot).map(MoveData::moveId)
        require(normalized in legal) {
            "$normalized is not a charged move for ${current.speciesId}"
        }

        val selected = current.selectedChargedMoveIds.toMutableList()
        while (selected.size <= buttonIndex) {
            val filler = legal.firstOrNull { it !in selected && it != normalized } ?: normalized
            selected.add(filler)
        }

        val previousAtButton = selected.getOrNull(buttonIndex)
        val otherIndex = 1 - buttonIndex
        val isSwap = otherIndex < selected.size && selected[otherIndex] == normalized

        if (isSwap) {
            val previous = selected[buttonIndex]
            selected[buttonIndex] = normalized
            selected[otherIndex] = previous
        } else {
            selected[buttonIndex] = normalized
        }

        val nextConfirmed = if (!isSwap && previousAtButton != null && previousAtButton != normalized) {
            current.confirmedChargedMoveIds - previousAtButton
        } else {
            current.confirmedChargedMoveIds
        }

        val next = current.copy(
            selectedChargedMoveIds = selected.take(MAX_CHARGED_BUTTONS).distinct(),
            confirmedChargedMoveIds = nextConfirmed
        )
        states[slot] = next
        return next
    }

    fun switchTo(slot: Int): EnemyPokemonState {
        requireSlot(slot)
        activeSlot?.takeIf { it != slot }?.let { previous ->
            val old = state(previous)
            if (old.status != PokemonBattleStatus.FAINTED) {
                states[previous] = old.copy(status = PokemonBattleStatus.BENCHED)
            }
        }

        val target = state(slot)
        require(target.status != PokemonBattleStatus.FAINTED) {
            "Cannot switch to a fainted Pokemon in slot $slot"
        }
        val next = target.copy(status = PokemonBattleStatus.ACTIVE)
        states[slot] = next
        activeSlot = slot
        return next
    }

    fun onFastMove(slot: Int, count: Int = 1): EnemyPokemonState {
        require(count > 0) { "count must be > 0" }
        val current = state(slot)
        val fastMoveId = requireNotNull(current.fastMoveId) {
            "Fast move is unknown for enemy slot $slot"
        }
        val move = requireNotNull(repository.move(fastMoveId)) { "Unknown move: $fastMoveId" }
        require(move.energyGain > 0) { "$fastMoveId is not a fast move" }

        val gained = move.energyGain * count
        val next = current.copy(
            energy = (current.energy + gained).coerceAtMost(maxEnergy),
            fastMovesObserved = current.fastMovesObserved + count,
            lastEnergyCorrection = 0
        )
        states[slot] = next

        val range = energyRange(slot)
        val nextMin = (range.min + gained).coerceAtMost(maxEnergy)
        val nextMax = (range.max + gained).coerceAtMost(maxEnergy)
        energyRanges[slot] = EnergyRangeEstimate(
            nextMin,
            nextMax,
            if (nextMin == nextMax) EvidenceLevel.CONFIRMED else EvidenceLevel.PROBABLE
        )
        return next
    }

    fun notePossibleMissedFastMoves(slot: Int, maxAdditionalFastMoves: Int): EnergyRangeEstimate {
        require(maxAdditionalFastMoves >= 0)
        if (maxAdditionalFastMoves == 0) return energyRange(slot)
        val current = state(slot)
        val move = current.fastMoveId?.let(repository::move) ?: return energyRange(slot)
        if (move.energyGain <= 0) return energyRange(slot)

        val range = energyRange(slot)
        val widened = EnergyRangeEstimate(
            min = range.min,
            max = (range.max + move.energyGain * maxAdditionalFastMoves).coerceAtMost(maxEnergy),
            confidence = EvidenceLevel.PROBABLE
        )
        energyRanges[slot] = widened
        return widened
    }

    /**
     * Applies a charged move observed in battle. If the stored energy is too low
     * for that move, the tracker treats the observation as proof that one or more
     * fast attacks were missed and self-corrects to the minimum legal energy.
     */
    fun onChargedMove(slot: Int, chargedMoveId: String): EnemyPokemonState {
        val current = state(slot)
        val normalized = chargedMoveId.normalizeMoveId()
        val move = requireNotNull(repository.move(normalized)) { "Unknown move: $normalized" }
        require(move.energyCost > 0) { "$normalized is not a charged move" }

        val legal = availableChargedMoves(slot).map(MoveData::moveId).toSet()
        if (legal.isNotEmpty()) {
            require(normalized in legal) {
                "$normalized is not a charged move for ${current.speciesId}"
            }
        }

        val selected = current.selectedChargedMoveIds.toMutableList()
        var canConfirm = normalized in selected
        if (normalized !in selected) {
            when {
                selected.size < MAX_CHARGED_BUTTONS -> {
                    selected.add(normalized)
                    canConfirm = true
                }
                else -> {
                    val replaceIndex = selected.indexOfFirst { it !in current.confirmedChargedMoveIds }
                    if (replaceIndex >= 0) {
                        selected[replaceIndex] = normalized
                        canConfirm = true
                    }
                }
            }
        }

        val confirmed = if (canConfirm) {
            current.confirmedChargedMoveIds + normalized
        } else {
            current.confirmedChargedMoveIds
        }

        val correction = (move.energyCost - current.energy).coerceAtLeast(0)
        val legalEnergyBeforeUse = (current.energy + correction).coerceAtMost(maxEnergy)
        val next = current.copy(
            energy = max(0, legalEnergyBeforeUse - move.energyCost),
            energyCorrectionCount = current.energyCorrectionCount + if (correction > 0) 1 else 0,
            lastEnergyCorrection = correction,
            chargedMovesUsed = current.chargedMovesUsed + 1,
            lastChargedMoveId = normalized,
            selectedChargedMoveIds = selected.take(MAX_CHARGED_BUTTONS).distinct(),
            confirmedChargedMoveIds = confirmed
        )
        states[slot] = next

        val range = energyRange(slot)
        val minBefore = max(range.min, move.energyCost)
        val maxBefore = max(range.max, move.energyCost)
        val minAfter = (minBefore - move.energyCost).coerceIn(0, maxEnergy)
        val maxAfter = (maxBefore - move.energyCost).coerceIn(0, maxEnergy)
        energyRanges[slot] = EnergyRangeEstimate(
            min = minAfter,
            max = maxAfter,
            confidence = if (minAfter == maxAfter) EvidenceLevel.CONFIRMED else EvidenceLevel.PROBABLE
        )
        return next
    }

    fun setEstimatedEnergy(slot: Int, energy: Int): EnemyPokemonState {
        val corrected = energy.coerceIn(0, maxEnergy)
        val next = state(slot).copy(energy = corrected, lastEnergyCorrection = 0)
        states[slot] = next
        energyRanges[slot] = EnergyRangeEstimate(corrected, corrected, EvidenceLevel.CONFIRMED)
        return next
    }

    fun energyRange(slot: Int): EnergyRangeEstimate {
        requireSlot(slot)
        return requireNotNull(energyRanges[slot])
    }

    fun markFainted(slot: Int): EnemyPokemonState {
        val current = state(slot)
        val next = current.copy(
            energy = 0,
            lastEnergyCorrection = 0,
            status = PokemonBattleStatus.FAINTED
        )
        states[slot] = next
        energyRanges[slot] = EnergyRangeEstimate(0, 0, EvidenceLevel.CONFIRMED)
        if (activeSlot == slot) activeSlot = null
        return next
    }

    fun chargedMoveProgress(slot: Int): List<ChargedMoveProgress> {
        val current = state(slot)
        val pokemon = current.speciesId?.let(repository::pokemon) ?: return emptyList()
        val fastMove = current.fastMoveId?.let(repository::move) ?: return emptyList()
        if (fastMove.energyGain <= 0) return emptyList()

        val moveIds = if (current.selectedChargedMoveIds.isNotEmpty()) {
            current.selectedChargedMoveIds
        } else {
            pokemon.chargedMoveIds.sorted()
        }

        return moveIds
            .mapNotNull(repository::move)
            .filter { it.energyCost > 0 }
            .map { charged ->
                val missingEnergy = (charged.energyCost - current.energy).coerceAtLeast(0)
                val secondChargeStorable = charged.energyCost * 2 <= maxEnergy
                val secondMissing = if (secondChargeStorable) {
                    (charged.energyCost * 2 - current.energy).coerceAtLeast(0)
                } else {
                    -1
                }
                ChargedMoveProgress(
                    move = charged,
                    currentEnergy = current.energy,
                    fastMovesNeeded = ceilDiv(missingEnergy, fastMove.energyGain),
                    ready = missingEnergy == 0,
                    chargesReady = (current.energy / charged.energyCost)
                        .coerceAtMost(if (secondChargeStorable) 2 else 1),
                    secondChargeStorable = secondChargeStorable,
                    fastMovesNeededForSecond = if (secondChargeStorable) {
                        ceilDiv(secondMissing, fastMove.energyGain)
                    } else {
                        -1
                    },
                    evidence = if (charged.moveId in current.confirmedChargedMoveIds) {
                        EvidenceLevel.CONFIRMED
                    } else {
                        EvidenceLevel.PREDICTED
                    }
                )
            }
    }

    fun state(slot: Int): EnemyPokemonState {
        requireSlot(slot)
        return requireNotNull(states[slot])
    }

    fun allStates(): List<EnemyPokemonState> = (0 until TEAM_SIZE).map(::state)

    fun activeEnemySlot(): Int? = activeSlot

    private fun requireSlot(slot: Int) {
        require(slot in 0 until TEAM_SIZE) { "slot must be 0..${TEAM_SIZE - 1}" }
    }

    private fun ceilDiv(numerator: Int, denominator: Int): Int {
        if (numerator <= 0) return 0
        return (numerator + denominator - 1) / denominator
    }

    private fun stronger(left: EvidenceLevel, right: EvidenceLevel): EvidenceLevel =
        if (left.ordinal >= right.ordinal) left else right

    companion object {
        const val TEAM_SIZE = 3
        const val MAX_CHARGED_BUTTONS = 2
    }
}

private fun String.normalizeMoveId(): String =
    trim().uppercase().replace(' ', '_')
