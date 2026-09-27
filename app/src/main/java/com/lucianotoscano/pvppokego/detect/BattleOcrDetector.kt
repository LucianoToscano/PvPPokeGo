package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.BattleLeagueTextDetector
import com.lucianotoscano.pvppokego.data.DetectedPokemon
import com.lucianotoscano.pvppokego.data.GameDataRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

/**
 * OCR uses small ROIs for the two top cards and a separate central/lower text ROI for
 * battle prompts. A larger party ROI is scanned less often to remember the user's team.
 */
class BattleOcrDetector(private val repo: GameDataRepository) : AutoCloseable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detect(frame: Bitmap): BattleDetection {
        if (frame.isRecycled) return BattleDetection(null, null)
        val playerRoi = crop(frame, 0f, TOP_FRAC, PLAYER_RIGHT_FRAC, BOTTOM_FRAC)
        val opponentRoi = crop(frame, OPPONENT_LEFT_FRAC, TOP_FRAC, 1f, BOTTOM_FRAC)
        val battleRoi = crop(frame, BATTLE_LEFT_FRAC, BATTLE_TOP_FRAC, BATTLE_RIGHT_FRAC, BATTLE_BOTTOM_FRAC)
        return try {
            val player = recognizeCard(playerRoi)
            val opponent = recognizeCard(opponentRoi)
            val battleText = recognize(battleRoi)
            BattleDetection(player, opponent, battleText)
        } finally {
            playerRoi.recycle(); opponentRoi.recycle(); battleRoi.recycle()
        }
    }

    /**
     * Reads the lobby/rules area to determine Great/Ultra/Master. It is called slowly
     * and only before an opponent is active, so it does not add continuous battle OCR cost.
     */
    suspend fun detectLeagueCp(frame: Bitmap): Int? {
        if (frame.isRecycled) return null
        val roi = crop(frame, LEAGUE_LEFT_FRAC, LEAGUE_TOP_FRAC, LEAGUE_RIGHT_FRAC, LEAGUE_BOTTOM_FRAC)
        return try {
            BattleLeagueTextDetector.detectCp(recognize(roi))
        } finally {
            roi.recycle()
        }
    }

    /**
     * Scans the team/party area. This is intentionally throttled by the service because
     * it is a larger OCR region than the two battle cards.
     */
    suspend fun detectOwnTeam(frame: Bitmap): List<DetectedPokemon> {
        if (frame.isRecycled) return emptyList()
        val roi = crop(frame, TEAM_LEFT_FRAC, TEAM_TOP_FRAC, TEAM_RIGHT_FRAC, TEAM_BOTTOM_FRAC)
        return try {
            val raw = recognize(roi)
            val names = repo.findPokemonMentions(raw, 3)
            val cpValues = CP_REGEX.findAll(raw).mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }.toList()
            names.mapIndexed { index, name ->
                DetectedPokemon(
                    name = name,
                    cp = cpValues.getOrNull(index),
                    confidence = 0.78f,
                    rawText = raw
                )
            }
        } finally {
            roi.recycle()
        }
    }

    private suspend fun recognizeCard(roi: Bitmap): DetectedPokemon? {
        // Try teal-text isolation first, but only accept it if the resulting text resolves
        // to a real known Pokemon. A few garbage OCR characters must not suppress the raw fallback.
        val filtered = filterByColor(roi, TARGET_TEXT_COLOR, COLOR_TOLERANCE)
        val filteredRaw = try {
            recognize(filtered)
        } finally {
            filtered.recycle()
        }
        parseCard(filteredRaw)?.let { return it }

        // Raw ROI is the authoritative fallback when color isolation removed/distorted glyphs.
        return parseCard(recognize(roi))
    }

    private fun parseCard(raw: String): DetectedPokemon? {
        if (raw.isBlank()) return null
        val cp = CP_REGEX.find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val candidates = raw.lines()
            .map { it.replace(CP_REGEX, "").trim() }
            .filter { it.length in 3..30 && it.any(Char::isLetter) }
        val canonical = candidates.asSequence().mapNotNull(repo::canonicalPokemonName).firstOrNull()
            ?: return null
        return DetectedPokemon(canonical, cp, 0.94f, raw)
    }

    private suspend fun recognize(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()
        return result.text.orEmpty()
    }

    private fun crop(src: Bitmap, l: Float, t: Float, r: Float, b: Float): Bitmap {
        val x = (src.width * l).toInt().coerceIn(0, src.width - 1)
        val y = (src.height * t).toInt().coerceIn(0, src.height - 1)
        val right = (src.width * r).toInt().coerceIn(x + 1, src.width)
        val bottom = (src.height * b).toInt().coerceIn(y + 1, src.height)
        return Bitmap.createBitmap(src, x, y, right - x, bottom - y)
    }

    private fun filterByColor(src: Bitmap, target: Int, tolerance: Int): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val px = IntArray(src.width * src.height)
        src.getPixels(px, 0, src.width, 0, 0, src.width, src.height)
        val tr = Color.red(target); val tg = Color.green(target); val tb = Color.blue(target)
        for (i in px.indices) {
            val p = px[i]
            val match = abs(Color.red(p) - tr) <= tolerance &&
                abs(Color.green(p) - tg) <= tolerance &&
                abs(Color.blue(p) - tb) <= tolerance
            px[i] = if (match) Color.BLACK else Color.WHITE
        }
        out.setPixels(px, 0, src.width, 0, 0, src.width, src.height)
        return out
    }

    override fun close() = recognizer.close()

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { if (cont.isActive) cont.resume(it) }
        addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }

    companion object {
        // Card values recovered from the working V03 base APK.
        private const val TOP_FRAC = 0.067f
        private const val BOTTOM_FRAC = 0.15f
        private const val PLAYER_RIGHT_FRAC = 0.43f
        private const val OPPONENT_LEFT_FRAC = 0.57f

        // Includes both the upper "Pokemon usou Golpe!" banner and the lower shield prompt.
        // The previous 0.50 bottom cutoff missed "Quer usar um escudo protetor?" on tall screens,
        // so the engine sometimes never registered the Charged event and energy stayed full.
        private const val BATTLE_LEFT_FRAC = 0.06f
        private const val BATTLE_TOP_FRAC = 0.12f
        private const val BATTLE_RIGHT_FRAC = 0.94f
        private const val BATTLE_BOTTOM_FRAC = 0.84f

        // Team-selection/party area. Scanned slowly and stopped once 3 Pokémon are known.
        private const val TEAM_LEFT_FRAC = 0.04f
        private const val TEAM_TOP_FRAC = 0.10f
        private const val TEAM_RIGHT_FRAC = 0.96f
        private const val TEAM_BOTTOM_FRAC = 0.93f

        // Lobby/rules text can appear across a large central portion of the screen.
        private const val LEAGUE_LEFT_FRAC = 0.04f
        private const val LEAGUE_TOP_FRAC = 0.04f
        private const val LEAGUE_RIGHT_FRAC = 0.96f
        private const val LEAGUE_BOTTOM_FRAC = 0.82f

        private val TARGET_TEXT_COLOR = Color.parseColor("#2f5971")
        private const val COLOR_TOLERANCE = 34
        private val CP_REGEX = Regex("(?i)(?:PC|CP)\\s*[: ]?\\s*(\\d{2,5})")
    }
}
