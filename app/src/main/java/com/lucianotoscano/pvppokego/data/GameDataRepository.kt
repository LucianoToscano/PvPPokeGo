package com.lucianotoscano.pvppokego.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GameDataRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Volatile private var gameMaster: GameMaster? = null
    @Volatile private var movesById: Map<String, MoveDef> = emptyMap()
    @Volatile private var pokemonByName: Map<String, PokemonDef> = emptyMap()
    @Volatile private var ranking: List<RankingEntry> = emptyList()
    @Volatile private var moveNamesPtBr: Map<String, String> = emptyMap()

    suspend fun load(leagueCp: Int = 1500) = withContext(Dispatchers.IO) {
        val gmText = runCatching {
            fetchWithCache(
                url = GAMEMASTER_URL,
                cacheName = "gamemaster.json",
                maxAgeMs = 24L * 60 * 60 * 1000
            )
        }.getOrElse { "{\"pokemon\":[],\"moves\":[]}" }
        gameMaster = runCatching { json.decodeFromString<GameMaster>(gmText) }.getOrNull()
        movesById = gameMaster?.moves?.associateBy { normalizeId(it.moveId) }.orEmpty()
        pokemonByName = gameMaster?.pokemon
            ?.flatMap { p -> listOf(p.speciesName to p, p.speciesId to p) }
            ?.associate { normalizeName(it.first) to it.second }
            .orEmpty()

        val rankingText = runCatching {
            context.assets.open("rankings-$leagueCp.json").bufferedReader().use { it.readText() }
        }.getOrElse {
            fetchWithCache(
                url = "$RANKINGS_PREFIX$leagueCp.json",
                cacheName = "rankings-$leagueCp.json",
                maxAgeMs = 12L * 60 * 60 * 1000
            )
        }
        ranking = runCatching { json.decodeFromString<List<RankingEntry>>(rankingText) }.getOrDefault(emptyList())
        moveNamesPtBr = runCatching {
            context.assets.open("move_names_ptbr.json").bufferedReader().use {
                json.decodeFromString<Map<String, String>>(it.readText())
            }
        }.getOrDefault(emptyMap())
    }

    fun move(id: String?): MoveDef? = id?.let { raw ->
        val key = normalizeId(raw)
        movesById[key]?.let { m -> m.copy(name = moveNamesPtBr[key] ?: m.name) }
    }

    fun pokemon(nameOrId: String?): PokemonDef? = nameOrId?.let { pokemonByName[normalizeName(it)] }

    fun possibleFastMoves(speciesName: String): List<MoveDef> {
        val entry = ranking.firstOrNull { normalizeName(it.speciesName) == normalizeName(speciesName) }
        return entry?.moves?.fastMoves.orEmpty()
            .sortedByDescending { it.uses }
            .mapNotNull { move(it.moveId) }
    }

    fun possibleChargedMoves(speciesName: String): List<MoveDef> {
        val entry = ranking.firstOrNull { normalizeName(it.speciesName) == normalizeName(speciesName) }
        return entry?.moves?.chargedMoves.orEmpty()
            .sortedByDescending { it.uses }
            .mapNotNull { move(it.moveId) }
    }

    fun likelyMoveset(speciesName: String): Triple<MoveDef?, MoveDef?, MoveDef?> {
        val entry = ranking.firstOrNull { normalizeName(it.speciesName) == normalizeName(speciesName) }
        val ids = entry?.moveset.orEmpty()
        return Triple(move(ids.getOrNull(0)), move(ids.getOrNull(1)), move(ids.getOrNull(2)))
    }

    fun canonicalPokemonName(ocrName: String): String? {
        val key = normalizeName(ocrName)
        pokemonByName[key]?.let { return it.speciesName }
        return pokemonByName.entries.minByOrNull { levenshtein(key, it.key) }
            ?.takeIf { levenshtein(key, it.key) <= 2 }
            ?.value?.speciesName
    }

    private fun fetchWithCache(url: String, cacheName: String, maxAgeMs: Long): String {
        val file = File(context.cacheDir, cacheName)
        if (file.exists() && System.currentTimeMillis() - file.lastModified() < maxAgeMs) {
            return file.readText()
        }
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("User-Agent", "PVPPokeGo/0.1")
        return try {
            conn.inputStream.bufferedReader().use { it.readText() }.also { text ->
                runCatching { file.writeText(text) }
            }
        } catch (e: Exception) {
            if (file.exists()) file.readText() else throw e
        } finally {
            conn.disconnect()
        }
    }

    private fun normalizeName(v: String): String = v.lowercase()
        .replace("♀", "f").replace("♂", "m")
        .replace(Regex("[^a-z0-9]+"), "")

    private fun normalizeId(v: String): String = v.uppercase().trim()

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in a.indices) {
            cur[0] = i + 1
            for (j in b.indices) {
                val c = if (a[i] == b[j]) 0 else 1
                cur[j + 1] = minOf(cur[j] + 1, prev[j + 1] + 1, prev[j] + c)
            }
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
    }

    companion object {
        const val GAMEMASTER_URL = "https://pvpoke.com/data/gamemaster.json"
        const val RANKINGS_PREFIX = "https://pvpoke.com/data/rankings/all/overall/rankings-"
    }
}
