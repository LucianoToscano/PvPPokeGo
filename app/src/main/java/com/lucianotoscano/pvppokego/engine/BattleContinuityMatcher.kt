package com.lucianotoscano.pvppokego.engine

/**
 * Decides whether fresh visual/OCR evidence belongs to the current PvP match.
 *
 * The detector intentionally favors continuity: Charged Move animations, switch screens,
 * short OCR gaps and one-sided Pokémon changes stay in the same session. A new session is
 * opened only from strong boundaries (stable team-selection screen) or a long gap followed
 * by both active identities changing.
 */
internal object BattleContinuityMatcher {
    enum class Decision { KEEP, NEW_MATCH }

    data class ActiveIdentity(
        val playerName: String?,
        val playerCp: Int?,
        val opponentName: String?,
        val opponentCp: Int?
    )

    data class DetectionIdentity(
        val playerName: String?,
        val playerCp: Int?,
        val opponentName: String?,
        val opponentCp: Int?
    )

    fun fromTeamScreen(
        hasSession: Boolean,
        battleActive: Boolean,
        gapSinceBattleEvidenceMs: Long,
        stableThreePokemonTeam: Boolean
    ): Decision {
        if (!hasSession || battleActive || !stableThreePokemonTeam) return Decision.KEEP
        return if (gapSinceBattleEvidenceMs >= TEAM_SCREEN_BOUNDARY_MS) {
            Decision.NEW_MATCH
        } else {
            Decision.KEEP
        }
    }

    fun fromPairedCards(
        hasSession: Boolean,
        battleActive: Boolean,
        gapSinceBattleEvidenceMs: Long,
        current: ActiveIdentity,
        detected: DetectionIdentity,
        switchPrompt: Boolean
    ): Decision {
        if (!hasSession || battleActive || switchPrompt) return Decision.KEEP
        if (gapSinceBattleEvidenceMs < PAIRED_CARD_BOUNDARY_MS) return Decision.KEEP

        val playerChanged = definitelyDifferent(
            current.playerName, current.playerCp,
            detected.playerName, detected.playerCp
        )
        val opponentChanged = definitelyDifferent(
            current.opponentName, current.opponentCp,
            detected.opponentName, detected.opponentCp
        )

        // One side changing is a normal switch. Both sides changing after a meaningful
        // no-battle gap is strong evidence that a fresh match has started.
        return if (playerChanged && opponentChanged) Decision.NEW_MATCH else Decision.KEEP
    }

    private fun definitelyDifferent(
        oldName: String?,
        oldCp: Int?,
        newName: String?,
        newCp: Int?
    ): Boolean {
        if (oldName.isNullOrBlank() || newName.isNullOrBlank()) return false
        if (!oldName.equals(newName, ignoreCase = true)) return true

        // Same species can exist at different CPs. Only call it different when both CPs
        // are present and disagree; a truncated/missing CP is never a boundary by itself.
        return oldCp != null && newCp != null && oldCp != newCp
    }

    const val TEAM_SCREEN_BOUNDARY_MS = 2_500L
    const val PAIRED_CARD_BOUNDARY_MS = 6_000L
}
