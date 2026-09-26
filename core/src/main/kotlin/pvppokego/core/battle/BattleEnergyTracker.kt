package pvppokego.core.battle

import kotlin.math.max
import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.EnemyPokemonState
import pvppokego.core.model.MoveData
import pvppokego.core.model.PokemonBattleStatus

/**
 * Tracks the opponent team independently from the overlay UI.
 *
 * Important rule:
 * switching a Pokemon out never resets its energy or fast-move count.
 * State is reset only when the Pokemon faints or the battle session is reset.
 */
class BattleEnergyTracker(
    private val repository: PvpokeRepository,
    private val leagueCp: Int = 1500,
    private val maxEnergy: Int = 100
) {
    private val states = mutableMapOf<Int, EnemyPokemonState>()
    private var activeSlot: Int? = null

    init {
        resetBattle()
    }

    fun resetBattle() {
        states.clear()
        repeat(TEAM_SIZE) { slot ->
            states[slot] = EnemyPokemonState(slot = slot)
        }
        activeSlot = null
    }

    fun identifyPokemon(
        slot: Int,
        speciesId: String,
        fastMoveId: String? = null
    ): EnemyPokemonState {
        requireSlot(slot)
        val existing = state(slot)
        val samePokemon = existing.speciesId == speciesId.lowercase()

        val recommendation = repository
            .recommendedMovesets(speciesId, leagueCp)
            .firstOrNull()

        val next = if (samePokemon) {
            existing.copy(
                fastMoveId = fastMoveId?.normalizeMoveId()
                    ?: existing.fastMoveId
                    ?: recommendation?.fastMoveId,
                selectedChargedMoveIds =
                    if (existing.selectedChargedMoveIds.isNotEmpty()) {
                        existing.selectedChargedMoveIds
                    } else {
                        recommendation?.chargedMoveIds.orEmpty()
                            .map(String::normalizeMoveId)
                            .distinct()
                            .take(MAX_CHARGED_BUTTONS)
                    }
            )
        } else {
            EnemyPokemonState(
                slot = slot,
                speciesId = speciesId.lowercase(),
                fastMoveId = fastMoveId?.normalizeMoveId() ?: recommendation?.fastMoveId,
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

    fun setFastMove(slot: Int, fastMoveId: String): EnemyPokemonState {
        val normalized = fastMoveId.normalizeMoveId()
        val current = state(slot)
        val legal = availableFastMoves(slot).map(MoveData::moveId).toSet()
        if (legal.isNotEmpty()) {
            require(normalized in legal) {
                "$normalized is not a fast move for ${current.speciesId}"
            }
        }
        val next = current.copy(fastMoveId = normalized)
        states[slot] = next
        return next
    }

    /**
     * Replaces both charged-move buttons while preserving the supplied order.
     * Any previously confirmed move that is intentionally removed here loses
     * its confirmed marker, allowing manual correction of an OCR mistake.
     */
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

    /**
     * Updates exactly one overlay charged-move button. Index 0 is left and
     * index 1 is right. Selecting a move already present on the other button
     * swaps the two buttons instead of creating a duplicate.
     *
     * Manual replacement of a previously confirmed button is treated as a
     * correction and clears the confirmation for the replaced move.
     */
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
            val filler = legal.firstOrNull { it !in selected && it != normalized }
                ?: normalized
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
            selectedChargedMoveIds = selected
                .take(MAX_CHARGED_BUTTONS)
                .distinct(),
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

    /**
     * Called once for each visually detected fast attack.
     * The energy is capped at 100, matching Pokemon GO PvP.
     */
    fun onFastMove(slot: Int, count: Int = 1): EnemyPokemonState {
        require(count > 0) { "count must be > 0" }
        val current = state(slot)
        val fastMoveId = requireNotNull(current.fastMoveId) {
            "Fast move is unknown for enemy slot $slot"
        }
        val move = requireNotNull(repository.move(fastMoveId)) {
            "Unknown move: $fastMoveId"
        }
        require(move.energyGain > 0) { "$fastMoveId is not a fast move" }

        val next = current.copy(
            energy = (current.energy + (move.energyGain * count)).coerceAtMost(maxEnergy),
            fastMovesObserved = current.fastMovesObserved + count
        )
        states[slot] = next
        return next
    }

    /**
     * Applies a charged move actually observed in battle.
     *
     * The first observed move replaces one unconfirmed prediction and becomes
     * confirmed. A later, different observed move replaces the other
     * unconfirmed prediction. Repeating an already confirmed move never
     * overwrites the other button.
     */
    fun onChargedMove(slot: Int, chargedMoveId: String): EnemyPokemonState {
        val current = state(slot)
        val normalized = chargedMoveId.normalizeMoveId()
        val move = requireNotNull(repository.move(normalized)) {
            "Unknown move: $normalized"
        }
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
                    val replaceIndex = selected.indexOfFirst {
                        it !in current.confirmedChargedMoveIds
                    }
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

        val next = current.copy(
            energy = max(0, current.energy - move.energyCost),
            chargedMovesUsed = current.chargedMovesUsed + 1,
            lastChargedMoveId = normalized,
            selectedChargedMoveIds = selected
                .take(MAX_CHARGED_BUTTONS)
                .distinct(),
            confirmedChargedMoveIds = confirmed
        )
        states[slot] = next
        return next
    }

    /**
     * Manual correction hook for missed visual detections.
     */
    fun setEstimatedEnergy(slot: Int, energy: Int): EnemyPokemonState {
        val next = state(slot).copy(energy = energy.coerceIn(0, maxEnergy))
        states[slot] = next
        return next
    }

    fun markFainted(slot: Int): EnemyPokemonState {
        val current = state(slot)
        val next = current.copy(
            energy = 0,
            status = PokemonBattleStatus.FAINTED
        )
        states[slot] = next
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
                val fastNeeded = ceilDiv(missingEnergy, fastMove.energyGain)
                ChargedMoveProgress(
                    move = charged,
                    currentEnergy = current.energy,
                    fastMovesNeeded = fastNeeded,
                    ready = missingEnergy == 0
                )
            }
    }

    fun state(slot: Int): EnemyPokemonState {
        requireSlot(slot)
        return requireNotNull(states[slot])
    }

    fun allStates(): List<EnemyPokemonState> =
        (0 until TEAM_SIZE).map(::state)

    fun activeEnemySlot(): Int? = activeSlot

    private fun requireSlot(slot: Int) {
        require(slot in 0 until TEAM_SIZE) { "slot must be 0..${TEAM_SIZE - 1}" }
    }

    private fun ceilDiv(numerator: Int, denominator: Int): Int {
        if (numerator <= 0) return 0
        return (numerator + denominator - 1) / denominator
    }

    companion object {
        const val TEAM_SIZE = 3
        const val MAX_CHARGED_BUTTONS = 2
    }
}

private fun String.normalizeMoveId(): String =
    trim().uppercase().replace(' ', '_')
