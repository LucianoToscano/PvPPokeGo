package com.lucianotoscano.pvppokego.engine

/**
 * Maps Pokemon GO's two native reserve cards to the user's stable team identities.
 * CP from the card is authoritative when it uniquely identifies a reserve.
 * When CP is ambiguous/missing we keep stable team order but mark the identity unconfirmed,
 * so the HUD can avoid drawing a wrong type icon.
 */
internal object ReserveCardMatcher {
    data class Member(val slot: Int, val name: String, val cp: Int?)
    data class Match(val member: Member, val cardCp: Int?, val confirmed: Boolean)

    fun resolve(
        team: List<Member>,
        activeName: String?,
        activeCp: Int?,
        cardCps: List<Int?>
    ): List<Match> {
        if (team.isEmpty()) return emptyList()

        val active = team.firstOrNull {
            it.name.equals(activeName, ignoreCase = true) &&
                (activeCp == null || it.cp == null || it.cp == activeCp)
        } ?: team.firstOrNull { it.name.equals(activeName, ignoreCase = true) }

        val reserves = team.filter { it.slot != active?.slot }
        if (reserves.isEmpty()) return emptyList()

        val result = arrayOfNulls<Match>(minOf(2, reserves.size))
        val used = mutableSetOf<Int>()

        result.indices.forEach { cardIndex ->
            val cp = cardCps.getOrNull(cardIndex) ?: return@forEach
            val matches = reserves.filter { it.cp == cp && it.slot !in used }
            if (matches.size == 1) {
                val member = matches.first()
                result[cardIndex] = Match(member, cp, true)
                used += member.slot
            }
        }

        // If one reserve card was identified and only one team member remains,
        // the other card is confirmed by elimination even if OCR missed its CP.
        if (reserves.size == 2 && used.size == 1) {
            val remainingMember = reserves.first { it.slot !in used }
            val emptyIndex = result.indexOfFirst { it == null }
            if (emptyIndex >= 0) {
                result[emptyIndex] = Match(
                    member = remainingMember,
                    cardCp = cardCps.getOrNull(emptyIndex),
                    confirmed = true
                )
                used += remainingMember.slot
            }
        }

        // Final fallback: stable original team order, never circular rotation.
        result.indices.forEach { index ->
            if (result[index] == null) {
                val remaining = reserves.firstOrNull { it.slot !in used } ?: return@forEach
                result[index] = Match(
                    member = remaining,
                    cardCp = cardCps.getOrNull(index),
                    confirmed = false
                )
                used += remaining.slot
            }
        }

        return result.filterNotNull()
    }
}
