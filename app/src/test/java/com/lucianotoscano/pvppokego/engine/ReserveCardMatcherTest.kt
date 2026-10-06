package com.lucianotoscano.pvppokego.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReserveCardMatcherTest {
    private val team = listOf(
        ReserveCardMatcher.Member(0, "Houndoom", 1499, 229),
        ReserveCardMatcher.Member(1, "Cramorant", 1485, 845),
        ReserveCardMatcher.Member(2, "Rillaboom", 1500, 812)
    )

    @Test
    fun cpPinsCramorantAndRillaboomToCorrectCards() {
        val r = ReserveCardMatcher.resolve(team, "Houndoom", 1499, listOf(1485, 1500))
        assertEquals(listOf("Cramorant", "Rillaboom"), r.map { it.member.name })
        assertTrue(r.all { it.confirmed })
    }

    @Test
    fun afterSwitchCpStillPinsCardsInsteadOfCircularGuess() {
        val r = ReserveCardMatcher.resolve(team, "Cramorant", 1485, listOf(1499, 1500))
        assertEquals(listOf("Houndoom", "Rillaboom"), r.map { it.member.name })
        assertTrue(r.all { it.confirmed })
    }

    @Test
    fun oneCpConfirmsSecondCardByElimination() {
        val r = ReserveCardMatcher.resolve(team, "Houndoom", 1499, listOf(1485, null))
        assertEquals(listOf("Cramorant", "Rillaboom"), r.map { it.member.name })
        assertTrue(r.all { it.confirmed })
    }

    @Test
    fun imageCanRecoverReserveIdentityWhenCpIsUnreadable() {
        val r = ReserveCardMatcher.resolve(
            team = team,
            activeName = "Houndoom",
            activeCp = 1499,
            cardCps = listOf(null, null),
            cardDexes = listOf(845, 812)
        )
        assertEquals(listOf("Cramorant", "Rillaboom"), r.map { it.member.name })
        assertEquals(listOf("image", "image"), r.map { it.source })
        assertTrue(r.all { it.confirmed })
    }

    @Test
    fun cpWinsEvenWhenVisualEvidenceConflicts() {
        val r = ReserveCardMatcher.resolve(
            team = team,
            activeName = "Houndoom",
            activeCp = 1499,
            cardCps = listOf(1485, 1500),
            cardDexes = listOf(812, 845)
        )
        assertEquals(listOf("Cramorant", "Rillaboom"), r.map { it.member.name })
        assertEquals(listOf("cp", "cp"), r.map { it.source })
        assertTrue(r.all { it.confirmed })
    }

    @Test
    fun missingCpDoesNotPretendIdentityIsConfirmed() {
        val r = ReserveCardMatcher.resolve(team, "Houndoom", 1499, listOf(null, null))
        assertEquals(listOf("Cramorant", "Rillaboom"), r.map { it.member.name })
        assertFalse(r[0].confirmed)
        assertFalse(r[1].confirmed)
    }
}
