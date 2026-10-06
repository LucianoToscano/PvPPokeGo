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
            val cpMissing = cardCps.getOrNull(index) == null
            val visualMissing =
                cardSpeciesIds.getOrNull(index).isNullOrBlank() &&
                    cardDexes.getOrNull(index) == null

            // A complete team plus both native CPs is sufficient: CP binding is
            // authoritative and no extra visual scan is needed. With an incomplete
            // team, keep searching for image identity so the missing reserve can be
            // discovered rather than remaining "IDENTIFICANDO".
            cpMissing || (knownTeamSize < 3 && visualMissing)
        }
    }
}
