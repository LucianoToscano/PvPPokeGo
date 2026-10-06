package com.lucianotoscano.pvppokego.engine

import com.lucianotoscano.pvppokego.data.DetectedPokemon

/** Pure boundary rules used to separate a finished match from temporary battle UI gaps. */
internal object BattleContinuityPolicy {
    fun confirmedTeamSelectionBoundary(
        team: List<DetectedPokemon>,
        battleActive: Boolean,
        msSinceBattleEvidence: Long
    ): Boolean {
        if (battleActive || msSinceBattleEvidence < MIN_OUT_OF_BATTLE_MS) return false
        val three = team.take(3)
        return three.size == 3 &&
            three.all { it.cp != null && it.confidence >= MIN_TEAM_CONFIDENCE } &&
            three.map { normalize(it.name) }.distinct().size == 3
    }

    private fun normalize(v: String): String =
        v.lowercase().replace(Regex("[^a-z0-9]+"), "")

    const val MIN_OUT_OF_BATTLE_MS = 4_500L
    const val MIN_TEAM_CONFIDENCE = 0.90f
}
