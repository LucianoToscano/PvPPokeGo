package com.lucianotoscano.pvppokego.detect

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lucianotoscano.pvppokego.data.BattleDetection
import com.lucianotoscano.pvppokego.data.DetectedPokemon
import com.lucianotoscano.pvppokego.data.GameDataRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

class BattleOcrDetector(private val repo: GameDataRepository) : AutoCloseable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun detect(frame: Bitmap): BattleDetection {
        if (frame.isRecycled) return BattleDetection(null, null)
        val playerRoi = crop(frame, 0f, TOP_FRAC, PLAYER_RIGHT_FRAC, BOTTOM_FRAC)
        val opponentRoi = crop(frame, OPPONENT_LEFT_FRAC, TOP_FRAC, 1f, BOTTOM_FRAC)
        return try {
            val player = recognizeCard(playerRoi)
            val opponent = recognizeCard(opponentRoi)
            BattleDetection(player, opponent)
        } finally {
            playerRoi.recycle(); opponentRoi.recycle()
        }
    }

    private suspend fun recognizeCard(roi: Bitmap): DetectedPokemon? {
        val filtered = filterByColor(roi, TARGET_TEXT_COLOR, COLOR_TOLERANCE)
        val first = recognize(filtered)
        filtered.recycle()
        val raw = if (first.length >= 3) first else recognize(roi)
        if (raw.isBlank()) return null
        val cp = CP_REGEX.find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val candidates = raw.lines().map { it.replace(CP_REGEX, "").trim() }
            .filter { it.length in 3..28 && it.any(Char::isLetter) }
        val bestRawName = candidates.firstOrNull() ?: return null
        val canonical = repo.canonicalPokemonName(bestRawName) ?: bestRawName
        val confidence = when {
            canonical.equals(bestRawName, ignoreCase = true) -> 0.98f
            canonical.isNotBlank() -> 0.88f
            else -> 0.55f
        }
        return DetectedPokemon(canonical, cp, confidence, raw)
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
        private const val TOP_FRAC = 0.067f
        private const val BOTTOM_FRAC = 0.15f
        private const val PLAYER_RIGHT_FRAC = 0.25f
        private const val OPPONENT_LEFT_FRAC = 0.75f
        private val TARGET_TEXT_COLOR = Color.parseColor("#2f5971")
        private const val COLOR_TOLERANCE = 30
        private val CP_REGEX = Regex("(?i)(?:PC|CP)\\s*[: ]?\\s*(\\d{2,5})")
    }
}
