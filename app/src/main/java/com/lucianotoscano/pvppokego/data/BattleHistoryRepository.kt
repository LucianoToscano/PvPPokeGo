package com.lucianotoscano.pvppokego.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BattleHistoryEvent(
    val elapsedMs: Long,
    val actor: String,
    val category: String,
    val moveName: String,
    val count: Int = 1,
    val pokemon: String? = null,
    val confidence: String? = null,
    val source: String? = null,
    val reason: String? = null
)

@Serializable
data class BattleHistoryEntry(
    val id: Long,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val leagueCp: Int,
    val playerName: String? = null,
    val playerCp: Int? = null,
    val opponentName: String? = null,
    val opponentCp: Int? = null,
    val events: List<BattleHistoryEvent> = emptyList()
)

class BattleHistoryRepository(context: Context) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun loadHistory(): List<BattleHistoryEntry> = runCatching {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        json.decodeFromString<List<BattleHistoryEntry>>(raw)
    }.getOrDefault(emptyList())

    fun exportJson(): String = json.encodeToString(loadHistory())

    fun exportJson(): String = json.encodeToString(loadHistory())

    fun append(entry: BattleHistoryEntry) {
        val next = (listOf(entry) + loadHistory()).take(MAX_BATTLES)
        prefs.edit().putString(KEY_HISTORY, json.encodeToString(next)).apply()
    }

    fun clear() = prefs.edit().remove(KEY_HISTORY).apply()

    companion object {
        private const val FILE = "pvppokego_battle_history"
        private const val KEY_HISTORY = "history_json"
        private const val MAX_BATTLES = 50
    }
}

class BattleHistoryRecorder(private val repository: BattleHistoryRepository) {
    private data class Active(
        val id: Long,
        val startedAt: Long,
        var leagueCp: Int,
        var playerName: String? = null,
        var playerCp: Int? = null,
        var opponentName: String? = null,
        var opponentCp: Int? = null,
        val events: MutableList<BattleHistoryEvent> = mutableListOf()
    )

    private var active: Active? = null

    fun setBattleActive(isActive: Boolean, leagueCp: Int, nowMs: Long = System.currentTimeMillis()) {
        if (isActive) {
            if (active == null) active = Active(nowMs, nowMs, leagueCp)
            else active?.leagueCp = leagueCp
        } else {
            finish(nowMs)
        }
    }

    fun observeDetection(detection: BattleDetection, leagueCp: Int, nowMs: Long = System.currentTimeMillis()) {
        val a = active ?: return
        a.leagueCp = leagueCp
        detection.player?.let {
            a.playerName = it.name
            if (it.cp != null) a.playerCp = it.cp
        }
        detection.opponent?.let {
            a.opponentName = it.name
            if (it.cp != null) a.opponentCp = it.cp
        }
    }

    fun recordFast(
        actor: String,
        moveName: String,
        count: Int,
        nowMs: Long = System.currentTimeMillis(),
        pokemon: String? = null,
        confidence: String? = null,
        source: String? = null
    ) {
        if (count <= 0) return
        addEvent(actor, "RÁPIDO", moveName, count, nowMs, pokemon, confidence, source, null)
    }

    fun recordCharged(
        actor: String,
        moveName: String,
        nowMs: Long = System.currentTimeMillis(),
        pokemon: String? = null,
        confidence: String? = null,
        source: String? = null
    ) {
        addEvent(actor, "CARREGADO", moveName, 1, nowMs, pokemon, confidence, source, null)
    }

    fun recordDecision(title: String, detail: String, nowMs: Long = System.currentTimeMillis()) {
        addEvent("ASSISTENTE", "DECISÃO", title, 1, nowMs, null, null, "BattleAssist", detail)
    }

    fun recordDiagnostic(label: String, detail: String, nowMs: Long = System.currentTimeMillis()) {
        addEvent("APP", "DIAGNÓSTICO", label, 1, nowMs, null, null, "engine", detail)
    }

    private fun addEvent(
        actor: String,
        category: String,
        moveName: String,
        count: Int,
        nowMs: Long,
        pokemon: String?,
        confidence: String?,
        source: String?,
        reason: String?
    ) {
        val a = active ?: return
        val elapsed = (nowMs - a.startedAt).coerceAtLeast(0L)
        a.events += BattleHistoryEvent(elapsed, actor, category, moveName, count, pokemon, confidence, source, reason)
    }

    fun finish(nowMs: Long = System.currentTimeMillis()) {
        val a = active ?: return
        active = null
        val useful = a.playerName != null || a.opponentName != null || a.events.isNotEmpty()
        if (!useful) return
        repository.append(
            BattleHistoryEntry(
                id = a.id,
                startedAtEpochMs = a.startedAt,
                endedAtEpochMs = nowMs.coerceAtLeast(a.startedAt),
                leagueCp = a.leagueCp,
                playerName = a.playerName,
                playerCp = a.playerCp,
                opponentName = a.opponentName,
                opponentCp = a.opponentCp,
                events = a.events.toList()
            )
        )
    }
}
