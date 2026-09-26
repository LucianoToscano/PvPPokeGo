package pvppokego.core.battle

import pvppokego.core.data.PvpokeRepository
import pvppokego.core.model.BattleSnapshot
import pvppokego.core.model.MyPokemonState
import pvppokego.core.model.PokemonBattleStatus
import pvppokego.core.model.PokemonData

/**
 * One PvP match session.
 *
 * The vision layer should feed detections into this class. This class does not
 * click, inject, automate, or interact with Pokemon GO.
 */
class BattleSession(
    private val repository: PvpokeRepository,
    leagueCp: Int = 1500
) {
    val enemyTracker = BattleEnergyTracker(repository, leagueCp)
    private val myTeam = mutableMapOf<Int, MyPokemonState>()

    init {
        reset()
    }

    fun reset() {
        myTeam.clear()
        repeat(TEAM_SIZE) { slot ->
            myTeam[slot] = MyPokemonState(slot = slot)
        }
        enemyTracker.resetBattle()
    }

    /**
     * Called by pre-battle visual recognition for the player's selected team.
     */
    fun registerMyPokemon(slot: Int, speciesId: String): MyPokemonState {
        requireSlot(slot)
        val normalized = speciesId.lowercase()
        requireNotNull(repository.pokemon(normalized)) {
            "Pokemon not found in local PvPoke data: $speciesId"
        }
        val next = MyPokemonState(
            slot = slot,
            speciesId = normalized,
            status = PokemonBattleStatus.BENCHED
        )
        myTeam[slot] = next
        return next
    }

    fun setMyActivePokemon(slot: Int): MyPokemonState {
        requireSlot(slot)
        myTeam.forEach { (key, value) ->
            if (value.status != PokemonBattleStatus.FAINTED) {
                myTeam[key] = value.copy(
                    status = if (key == slot) {
                        PokemonBattleStatus.ACTIVE
                    } else {
                        PokemonBattleStatus.BENCHED
                    }
                )
            }
        }
        return requireNotNull(myTeam[slot])
    }

    fun markMyPokemonFainted(slot: Int): MyPokemonState {
        requireSlot(slot)
        val next = requireNotNull(myTeam[slot]).copy(status = PokemonBattleStatus.FAINTED)
        myTeam[slot] = next
        return next
    }

    fun myPokemonData(slot: Int): PokemonData? {
        requireSlot(slot)
        return myTeam[slot]?.speciesId?.let(repository::pokemon)
    }

    fun myTeamData(): List<PokemonData?> =
        (0 until TEAM_SIZE).map(::myPokemonData)

    fun snapshot(): BattleSnapshot =
        BattleSnapshot(
            myTeam = (0 until TEAM_SIZE).map { requireNotNull(myTeam[it]) },
            enemyTeam = enemyTracker.allStates(),
            activeEnemySlot = enemyTracker.activeEnemySlot()
        )

    private fun requireSlot(slot: Int) {
        require(slot in 0 until TEAM_SIZE) { "slot must be 0..${TEAM_SIZE - 1}" }
    }

    companion object {
        const val TEAM_SIZE = 3
    }
}
