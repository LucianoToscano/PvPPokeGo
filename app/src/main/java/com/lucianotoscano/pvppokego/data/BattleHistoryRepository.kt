package com.lucianotoscano.pvppokego.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val reason: String? = null,
    val details: Map<String, String> = emptyMap()
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
    val appVersion: String = "desconhecida",
    val dataVersion: String? = null,
    val startedMidBattle: Boolean = false,
    val endReason: String? = null,
    val result: String? = null,
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

    fun exportJson(entries: List<BattleHistoryEntry>): String = json.encodeToString(entries)

    fun battleById(id: Long): BattleHistoryEntry? = loadHistory().firstOrNull { it.id == id }

    /**
     * Human-readable export designed to be uploaded to ChatGPT or shared with a coach.
     * Technical fields are preserved, but the layout separates player, enemy, assistant
     * and diagnostic events so the file is useful without opening the app.
     */
    fun exportAnalysisText(entries: List<BattleHistoryEntry> = loadHistory()): String = buildString {
        appendLine("PvPPokeGo — histórico para análise")
        appendLine("Formato: 1")
        appendLine("Batalhas: ${entries.size}")
        appendLine()
        entries.forEachIndexed { index, battle ->
            val duration = ((battle.endedAtEpochMs - battle.startedAtEpochMs).coerceAtLeast(0L) / 1000.0)
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(Date(battle.startedAtEpochMs))
            val league = when (battle.leagueCp) {
                2500 -> "Liga Ultra"
                10000 -> "Liga Mestra"
                else -> "Liga Grande"
            }
            val ownFast = battle.events.sumOf { if (it.actor == "VOCÊ" && it.category == "RÁPIDO") it.count else 0 }
            val enemyFast = battle.events.sumOf { if (it.actor == "INIMIGO" && it.category == "RÁPIDO") it.count else 0 }
            val ownCharged = battle.events.count { it.actor == "VOCÊ" && it.category == "CARREGADO" }
            val enemyCharged = battle.events.count { it.actor == "INIMIGO" && it.category == "CARREGADO" }
            val decisions = battle.events.count { it.category == "DECISÃO" }

            appendLine("================================================================")
            appendLine("BATALHA ${index + 1}  |  ID ${battle.id}")
            appendLine("$date  |  $league  |  ${"%.1f".format(Locale.US, duration)}s")
            appendLine("VOCÊ: ${battle.playerName ?: "?"} PC ${battle.playerCp ?: "?"}")
            appendLine("INIMIGO: ${battle.opponentName ?: "?"} PC ${battle.opponentCp ?: "?"}")
            appendLine("APP: ${battle.appVersion} | BASE: ${battle.dataVersion ?: "desconhecida"}")
            appendLine("CAPTURA: ${if (battle.startedMidBattle) "iniciada no meio da luta" else "início normal/indeterminado"}")
            appendLine("ENCERRAMENTO: ${battle.endReason ?: "não informado"}")
            battle.result?.let { appendLine("RESULTADO: $it") }
            appendLine("RESUMO: rápidos você=$ownFast, inimigo=$enemyFast | carregados você=$ownCharged, inimigo=$enemyCharged | decisões=$decisions")
            appendLine()

            fun section(title: String, predicate: (BattleHistoryEvent) -> Boolean) {
                val selected = battle.events.filter(predicate)
                appendLine("## $title (${selected.size})")
                if (selected.isEmpty()) {
                    appendLine("- nenhum")
                } else {
                    selected.forEach { event ->
                        val sec = event.elapsedMs / 1000.0
                        val count = if (event.count > 1) " x${event.count}" else ""
                        append("[+${"%.1f".format(Locale.US, sec)}s] ")
                        append(event.category)
                        append(" • ")
                        append(event.moveName)
                        append(count)
                        event.pokemon?.let { append(" • Pokémon: ").append(it) }
                        event.confidence?.let { append(" • confiança: ").append(it) }
                        event.source?.let { append(" • fonte: ").append(it) }
                        appendLine()
                        if (event.details.isNotEmpty()) {
                            appendLine("  Estado: " + event.details.entries.joinToString(" • ") { "${it.key}=${it.value}" })
                        }
                        event.reason?.takeIf(String::isNotBlank)?.let { appendLine("  Motivo: $it") }
                    }
                }
                appendLine()
            }

            section("VOCÊ") { it.actor == "VOCÊ" }
            section("INIMIGO") { it.actor == "INIMIGO" }
            section("ASSISTENTE") { it.actor == "ASSISTENTE" || it.category == "DECISÃO" }
            section("PARTIDA / TROCAS / ESTADO") {
                it.actor == "PARTIDA" || it.category == "TROCA" || it.category == "ESTADO"
            }
            section("DIAGNÓSTICO TÉCNICO") { it.actor == "APP" || it.category == "DIAGNÓSTICO" }
        }
    }

    fun exportCsv(entries: List<BattleHistoryEntry> = loadHistory()): String {
        fun csv(value: Any?): String {
            val raw = value?.toString().orEmpty()
            return "\"" + raw.replace("\"", "\"\"") + "\""
        }
        return buildString {
            appendLine("battle_id,started_at_ms,league_cp,app_version,data_version,started_mid_battle,end_reason,result,player,player_cp,opponent,opponent_cp,elapsed_ms,actor,category,move,count,pokemon,confidence,source,reason,details")
            entries.forEach { battle ->
                battle.events.forEach { event ->
                    appendLine(
                        listOf(
                            battle.id, battle.startedAtEpochMs, battle.leagueCp,
                            battle.appVersion, battle.dataVersion, battle.startedMidBattle, battle.endReason, battle.result,
                            battle.playerName, battle.playerCp, battle.opponentName, battle.opponentCp,
                            event.elapsedMs, event.actor, event.category, event.moveName, event.count,
                            event.pokemon, event.confidence, event.source, event.reason,
                            event.details.entries.joinToString(";") { "${it.key}=${it.value}" }
                        ).joinToString(",") { csv(it) }
                    )
                }
            }
        }
    }

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
        var appVersion: String = "desconhecida",
        var dataVersion: String? = null,
        var startedMidBattle: Boolean = false,
        var result: String? = null,
        var lastEvidenceAtMs: Long = startedAt,
        val events: MutableList<BattleHistoryEvent> = mutableListOf()
    )

    private var active: Active? = null
    private var appVersion: String = "desconhecida"
    private var dataVersion: String? = null

    fun setMetadata(appVersion: String, dataVersion: String?) {
        this.appVersion = appVersion
        this.dataVersion = dataVersion
        active?.let {
            it.appVersion = appVersion
            it.dataVersion = dataVersion
        }
    }

    fun hasActiveSession(): Boolean = active != null

    fun activeElapsedMs(nowMs: Long = System.currentTimeMillis()): Long? =
        active?.let { (nowMs - it.startedAt).coerceAtLeast(0L) }

    fun markStartedMidBattle() {
        active?.startedMidBattle = true
    }

    fun setResult(result: String?) {
        if (!result.isNullOrBlank()) active?.result = result
    }

    fun setBattleActive(isActive: Boolean, leagueCp: Int, nowMs: Long = System.currentTimeMillis()) {
        if (isActive) {
            if (active == null) {
                active = Active(
                    id = nowMs,
                    startedAt = nowMs,
                    leagueCp = leagueCp,
                    appVersion = appVersion,
                    dataVersion = dataVersion,
                    lastEvidenceAtMs = nowMs
                )
                addEvent(
                    actor = "PARTIDA",
                    category = "INÍCIO",
                    moveName = "Partida detectada",
                    count = 1,
                    nowMs = nowMs,
                    pokemon = null,
                    confidence = null,
                    source = "session",
                    reason = null
                )
            } else {
                active?.leagueCp = leagueCp
                active?.lastEvidenceAtMs = nowMs
            }
        } else {
            val a = active ?: return
            if (nowMs - a.lastEvidenceAtMs >= SESSION_INACTIVITY_GRACE_MS) {
                finish(nowMs, reason = "Sem evidência de batalha por 12s")
            }
        }
    }

    fun observeStableState(
        playerName: String?,
        playerCp: Int?,
        opponentName: String?,
        opponentCp: Int?,
        leagueCp: Int,
        nowMs: Long = System.currentTimeMillis()
    ) {
        val a = active ?: return
        a.leagueCp = leagueCp
        playerName?.let { a.playerName = it }
        playerCp?.let { a.playerCp = it }
        opponentName?.let { a.opponentName = it }
        opponentCp?.let { a.opponentCp = it }
        a.lastEvidenceAtMs = nowMs
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

    fun recordSwitch(
        actor: String,
        fromPokemon: String?,
        toPokemon: String?,
        cp: Int?,
        nowMs: Long = System.currentTimeMillis(),
        confidence: String? = null
    ) {
        val label = when {
            fromPokemon != null && toPokemon != null -> "$fromPokemon → $toPokemon"
            toPokemon != null -> "Entrou $toPokemon"
            else -> "Troca detectada"
        }
        addEvent(
            actor, "TROCA", label, 1, nowMs, toPokemon, confidence, "OCR", null,
            buildMap {
                fromPokemon?.let { put("de", it) }
                toPokemon?.let { put("para", it) }
                cp?.let { put("pc", it.toString()) }
            }
        )
    }

    fun recordState(
        actor: String,
        pokemon: String?,
        hpRatio: Float?,
        energyMin: Int?,
        energyMax: Int?,
        shields: Int? = null,
        confidence: String? = null,
        nowMs: Long = System.currentTimeMillis(),
        source: String = "engine"
    ) {
        val details = buildMap {
            hpRatio?.let { put("hpPercent", (it.coerceIn(0f, 1f) * 100f).toInt().toString()) }
            energyMin?.let { put("energyMin", it.toString()) }
            energyMax?.let { put("energyMax", it.toString()) }
            shields?.let { put("shields", it.toString()) }
        }
        if (details.isEmpty()) return
        addEvent(actor, "ESTADO", "Estado da batalha", 1, nowMs, pokemon, confidence, source, null, details)
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
        reason: String?,
        details: Map<String, String> = emptyMap()
    ) {
        val a = active ?: return
        val elapsed = (nowMs - a.startedAt).coerceAtLeast(0L)
        a.events += BattleHistoryEvent(
            elapsed, actor, category, moveName, count, pokemon, confidence, source, reason, details
        )
    }

    fun finish(
        nowMs: Long = System.currentTimeMillis(),
        reason: String = "Encerramento solicitado",
        result: String? = null
    ) {
        val a = active ?: return
        result?.takeIf(String::isNotBlank)?.let { a.result = it }
        addEvent(
            actor = "PARTIDA",
            category = "FIM",
            moveName = a.result?.let { "Partida encerrada: $it" } ?: "Partida encerrada",
            count = 1,
            nowMs = nowMs,
            pokemon = null,
            confidence = null,
            source = "session",
            reason = reason
        )
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
                appVersion = a.appVersion,
                dataVersion = a.dataVersion,
                startedMidBattle = a.startedMidBattle,
                endReason = reason,
                result = a.result,
                events = a.events.toList()
            )
        )
    }

    companion object {
        private const val SESSION_INACTIVITY_GRACE_MS = 12_000L
    }
}
