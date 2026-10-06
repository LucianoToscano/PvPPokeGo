package com.lucianotoscano.pvppokego.engine

/**
 * Decides whether the native reserve cards still need OCR/visual scanning.
 *
 * Mid-battle starts are valid: one stable active Pokémon is enough to scan the two
 * reserve cards and reconstruct the rest of the team from card CP + visual evidence.
 */
internal object ReserveScanPolicy {
    fun shouldScan(
        hasActivePlayer: Boolean,
        knownTeamSize: Int,
        cardCps: List<Int?>,
        cardDexes: List<Int?>,
        cardSpeciesIds: List<String?>
    ): Boolean {
        if (!hasActivePlayer || knownTeamSize <= 0) return false
        val count = maxOf(cardCps.size, cardDexes.size, cardSpeciesIds.size, 2)
        return (0 until count.coerceAtMost(2)).any { index ->
            cardCps.getOrNull(index) == null ||
                (
                    cardSpeciesIds.getOrNull(index).isNullOrBlank() &&
                        cardDexes.getOrNull(index) == null
                )
        }
    }
}
