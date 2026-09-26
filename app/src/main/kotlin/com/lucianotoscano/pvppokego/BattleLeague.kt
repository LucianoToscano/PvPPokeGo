package com.lucianotoscano.pvppokego

import android.content.Context
import java.text.Normalizer

enum class BattleLeague(val cpCap: Int, val labelPtBr: String) {
    GREAT(1500, "Liga Grande"),
    ULTRA(2500, "Liga Ultra"),
    MASTER(10000, "Liga Mestra")
}

enum class LeagueSelectionMode {
    AUTO,
    GREAT,
    ULTRA,
    MASTER
}

class LeaguePreference(context: Context) {
    private val prefs = context.getSharedPreferences("pvppokego", Context.MODE_PRIVATE)

    var mode: LeagueSelectionMode
        get() = runCatching {
            LeagueSelectionMode.valueOf(prefs.getString(KEY_MODE, LeagueSelectionMode.AUTO.name)!!)
        }.getOrDefault(LeagueSelectionMode.AUTO)
        set(value) { prefs.edit().putString(KEY_MODE, value.name).apply() }

    var lastDetectedLeague: BattleLeague?
        get() = prefs.getString(KEY_LAST, null)?.let {
            runCatching { BattleLeague.valueOf(it) }.getOrNull()
        }
        set(value) { prefs.edit().putString(KEY_LAST, value?.name).apply() }

    fun configuredLeague(): BattleLeague? = when (mode) {
        LeagueSelectionMode.AUTO -> null
        LeagueSelectionMode.GREAT -> BattleLeague.GREAT
        LeagueSelectionMode.ULTRA -> BattleLeague.ULTRA
        LeagueSelectionMode.MASTER -> BattleLeague.MASTER
    }

    private companion object {
        const val KEY_MODE = "league_mode"
        const val KEY_LAST = "last_detected_league"
    }
}

object LobbyLeagueDetector {
    fun detect(text: String): BattleLeague? {
        val normalized = normalize(text)
        if (normalized.isBlank()) return null

        return when {
            containsAny(normalized,
                "liga grande", "great league", "max pc 1500", "pc max 1500",
                "limite 1500", "ate 1500", "cp 1500", "1500 cp"
            ) -> BattleLeague.GREAT

            containsAny(normalized,
                "liga ultra", "ultra league", "max pc 2500", "pc max 2500",
                "limite 2500", "ate 2500", "cp 2500", "2500 cp"
            ) -> BattleLeague.ULTRA

            containsAny(normalized,
                "liga mestra", "liga mestre", "master league", "sem limite de pc",
                "sem limite de cp", "no cp limit"
            ) -> BattleLeague.MASTER

            else -> null
        }
    }

    private fun containsAny(text: String, vararg values: String): Boolean =
        values.any(text::contains)

    private fun normalize(value: String): String {
        val noAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return noAccents.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
