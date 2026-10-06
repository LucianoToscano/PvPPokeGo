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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun detect(frame: Bitmap): BattleDetection = coroutineScope {
        if (frame.isRecycled) return@coroutineScope BattleDetection(null, null)
        val playerRoi = crop(frame, 0f, TOP_FRAC, PLAYER_RIGHT_FRAC, BOTTOM_FRAC)
        val opponentRoi = crop(frame, OPPONENT_LEFT_FRAC, TOP_FRAC, 1f, BOTTOM_FRAC)
        val battleRoi = crop(frame, BATTLE_LEFT_FRAC, BATTLE_TOP_FRAC, BATTLE_RIGHT_FRAC, BATTLE_BOTTOM_FRAC)
        try {
            val playerJob = async { recognizeCard(playerRoi) }
            val opponentJob = async { recognizeCard(opponentRoi) }
            val battleJob = async { recognize(battleRoi) }
            BattleDetection(playerJob.await(), opponentJob.await(), battleJob.await())
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
     * Reads only the two native reserve cards on the right side and extracts their CPs
     * in top-to-bottom order. CP is used to bind the HUD types/matchup to the correct
     * reserve after switches, avoiding slot-order guesses.
     */
    suspend fun detectReserveCps(frame: Bitmap): Pair<Int?, Int?> = coroutineScope {
        if (frame.isRecycled) return@coroutineScope null to null
        val top = crop(frame, RESERVE_LEFT_FRAC, RESERVE_TOP_1, RESERVE_RIGHT_FRAC, RESERVE_BOTTOM_1)
        val bottom = crop(frame, RESERVE_LEFT_FRAC, RESERVE_TOP_2, RESERVE_RIGHT_FRAC, RESERVE_BOTTOM_2)
        try {
            fun parseCp(raw: String): Int? = CP_REGEX.find(raw)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
                ?.takeIf { it in 10..10000 }

            val topJob = async { parseCp(recognize(top)) }
            val bottomJob = async { parseCp(recognize(bottom)) }
            topJob.await() to bottomJob.await()
        } finally {
            top.recycle(); bottom.recycle()
        }
    }

    /**
     * Scans the team/party area. This is intentionally throttled by the service because
     * it is a larger OCR region than the two battle cards.
     */
    suspend fun detectOwnTeam(frame: Bitmap): List<DetectedPokemon> = coroutineScope {
        if (frame.isRecycled) return@coroutineScope emptyList()

        // Read each team card independently. This preserves the real pairing between
        // name and CP and prevents a shifted list such as Houndoom=1485, Cramorant=1500.
        val slot1 = crop(frame, TEAM_SLOT_1_LEFT, TEAM_SLOT_TOP, TEAM_SLOT_1_RIGHT, TEAM_SLOT_BOTTOM)
        val slot2 = crop(frame, TEAM_SLOT_2_LEFT, TEAM_SLOT_TOP, TEAM_SLOT_2_RIGHT, TEAM_SLOT_BOTTOM)
        val slot3 = crop(frame, TEAM_SLOT_3_LEFT, TEAM_SLOT_TOP, TEAM_SLOT_3_RIGHT, TEAM_SLOT_BOTTOM)
        try {
            val jobs = listOf(
                async { recognizeTeamSlot(slot1) },
                async { recognizeTeamSlot(slot2) },
                async { recognizeTeamSlot(slot3) }
            )
            jobs.mapNotNull { it.await() }
        } finally {
            slot1.recycle(); slot2.recycle(); slot3.recycle()
        }
    }

    private suspend fun recognizeTeamSlot(roi: Bitmap): DetectedPokemon? {
        suspend fun parse(raw: String, confidence: Float): DetectedPokemon? {
            if (raw.isBlank()) return null
            val cp = CP_REGEX.find(raw)
                ?.groupValues
                ?.getOrNull(1)
                ?.toIntOrNull()
                ?.takeIf { it in 10..10000 }
            val name = repo.findPokemonMentions(raw, 1).firstOrNull()
                ?: raw.lines()
                    .asSequence()
                    .map { line ->
                        line.replace(CP_REGEX, " ")
                            .replace(Regex("[^\\p{L}♀♂' .-]+"), " ")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                    }
                    .mapNotNull(repo::canonicalPokemonName)
                    .firstOrNull()
            if (name == null) return null
            return DetectedPokemon(name, cp, confidence, raw)
        }

        val raw = recognize(roi)
        parse(raw, if (CP_REGEX.containsMatchIn(raw)) 0.98f else 0.90f)?.let { return it }

        // Color isolation is now a fallback only, so the common path costs one OCR pass/card.
        val filtered = filterByColor(roi, TARGET_TEXT_COLOR, COLOR_TOLERANCE)
        return try {
            parse(recognize(filtered), 0.90f)
        } finally {
            filtered.recycle()
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

        // Native reserve cards on the right side during battle.
        // Keep the OCR boxes tight around each card so confirmation is faster and
        // CP values cannot swap order.
        private const val RESERVE_LEFT_FRAC = 0.80f
        private const val RESERVE_RIGHT_FRAC = 0.995f
        private const val RESERVE_TOP_1 = 0.545f
        private const val RESERVE_BOTTOM_1 = 0.645f
        private const val RESERVE_TOP_2 = 0.640f
        private const val RESERVE_BOTTOM_2 = 0.750f

        // Team selection card, read as three independent columns.
        private const val TEAM_SLOT_TOP = 0.615f
        private const val TEAM_SLOT_BOTTOM = 0.835f
        private const val TEAM_SLOT_1_LEFT = 0.065f
        private const val TEAM_SLOT_1_RIGHT = 0.355f
        private const val TEAM_SLOT_2_LEFT = 0.345f
        private const val TEAM_SLOT_2_RIGHT = 0.655f
        private const val TEAM_SLOT_3_LEFT = 0.645f
        private const val TEAM_SLOT_3_RIGHT = 0.945f

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
