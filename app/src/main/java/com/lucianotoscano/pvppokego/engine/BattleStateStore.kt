package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.BattlePokemonState
import java.util.concurrent.ConcurrentHashMap

class BattleStateStore {
    private val states = ConcurrentHashMap<String, BattlePokemonState>()
    fun getOrCreate(key: String, speciesName: String): BattlePokemonState = states.getOrPut(key) { BattlePokemonState(key = key, speciesName = speciesName) }
    fun find(key: String): BattlePokemonState? = states[key]
    fun put(state: BattlePokemonState) { states[state.key] = state }
    fun all(): List<BattlePokemonState> = states.values.toList()
    fun clearBattle() = states.clear()
}
