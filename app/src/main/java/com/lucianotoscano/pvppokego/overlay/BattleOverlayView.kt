package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.SettingsRepository
import kotlin.math.min

/**
 * Canonical PvPPokeGo battle HUD.
 *
 * Coordinates are tied to the approved 864x1536 reference mockup. The full view remains
 * click-through in BattleOverlayService; only tiny separate windows receive touches.
 */
class BattleOverlayView(
    context: Context,
    private val settings: SettingsRepository,
    private val callbacks: Callbacks
) : View(context) {

    interface Callbacks {
        fun onFastMoveTapped()
        fun onCharged1Tapped()
        fun onCharged2Tapped()
        fun onResetTapped()
        fun onManualFastDelta(delta: Int)
        fun onMoveSelected(kind: MoveKind, move: MoveDef)
    }

    enum class MoveKind { FAST, CHARGED_1, CHARGED_2 }

    data class PopupState(
        val kind: MoveKind,
        val anchor: RectF,
        val options: List<MoveDef>
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }

    private var state = BattleUiState()
    private var scaleFactor = settings.scale
    private var opacityFactor = settings.opacity
    private var collapsed = false

    private val hitFast = RectF()
    private val hitCharge1 = RectF()
    private val hitCharge2 = RectF()
    private val hitReset = RectF()
    private val hitMinus = RectF()
    private val hitPlus = RectF()
    private var popup: PopupState? = null

    fun render(newState: BattleUiState) {
        state = newState
        scaleFactor = settings.scale
        opacityFactor = settings.opacity
        invalidate()
    }

    fun setCollapsed(value: Boolean) {
        collapsed = value
        if (value) popup = null
        invalidate()
    }

    fun isCollapsed(): Boolean = collapsed

    fun showMoveMenu(kind: MoveKind, options: List<MoveDef>) {
        if (collapsed) return
        val anchor = when (kind) {
            MoveKind.FAST -> hitFast
            MoveKind.CHARGED_1 -> hitCharge1
            MoveKind.CHARGED_2 -> hitCharge2
        }
        if (anchor.isEmpty || options.isEmpty()) return
        popup = PopupState(kind, RectF(anchor), options.take(7))
        invalidate()
    }

    fun dismissMenu() {
        popup = null
        invalidate()
    }

    private fun sx(): Float = width / 864f
    private fun sy(): Float = height / 1536f
    private fun u(): Float = min(sx(), sy()) * (scaleFactor / .8f).coerceIn(.62f, 1.5f)
    private fun rx(v: Float): Float = v * sx()
    private fun ry(v: Float): Float = v * sy()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.alpha = (255 * opacityFactor).toInt().coerceIn(40, 255)
        stroke.alpha = paint.alpha
        stroke.color = Color.WHITE

        if (collapsed) {
            drawRestoreControl(canvas, rx(42f), ry(38f))
            return
        }

        // Top-left pair from the approved mockup.
        if (settings.showCurrentIndicator) {
            drawMatchupIndicator(canvas, rx(42f), ry(38f), state.playerMatchup, 19f * u())
        }
        drawHideControl(canvas, rx(96f), ry(38f), 19f * u())

        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (settings.analyzeReserves) drawReserves(canvas)
        if (settings.showSwitchTimer) drawTimers(canvas)
        if (settings.editMode) drawResetAndCorrection(canvas)
        if (settings.debugMode) drawDebug(canvas)
        popup?.let { drawPopup(canvas, it) }
    }

    private fun drawHideControl(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(225, 38, 54), paint.alpha)
        canvas.drawCircle(cx, cy, r, paint)
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, r, stroke)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 23f * u()
        canvas.drawText("×", cx, cy + 7.5f * u(), paint)
    }

    private fun drawRestoreControl(canvas: Canvas, cx: Float, cy: Float) {
        val outer = 21f * u()
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(43, 53, 69), paint.alpha)
        canvas.drawCircle(cx, cy, outer, paint)
        stroke.color = withAlpha(Color.rgb(77, 168, 232), paint.alpha)
        stroke.strokeWidth = 3f * u()
        canvas.drawCircle(cx, cy, outer - 1.5f * u(), stroke)
        paint.color = withAlpha(Color.rgb(251, 165, 75), paint.alpha)
        canvas.drawCircle(cx, cy, 12f * u(), paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 12f * u()
        canvas.drawText("P", cx, cy + 4f * u(), paint)
        stroke.color = Color.WHITE
    }

    private fun drawMatchupIndicator(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        matchup: MatchupState,
        radius: Float = 17f * u()
    ) {
        paint.style = Paint.Style.FILL
        paint.color = when (matchup) {
            MatchupState.FAVORABLE -> withAlpha(Color.rgb(20, 190, 80), paint.alpha)
            MatchupState.UNFAVORABLE -> withAlpha(Color.rgb(225, 45, 55), paint.alpha)
            MatchupState.NEUTRAL -> withAlpha(Color.rgb(150, 150, 150), paint.alpha)
            MatchupState.UNKNOWN -> withAlpha(Color.rgb(105, 105, 105), paint.alpha)
        }
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, radius, stroke)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 20f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        val glyph = when (matchup) {
            MatchupState.FAVORABLE -> "✓"
            MatchupState.UNFAVORABLE -> "×"
            else -> "•"
        }
        canvas.drawText(glyph, cx, cy + 6.5f * u(), paint)
    }

    /**
     * Approved top row at 864x1536:
     * strong types 574/616/658/701, arrow 739, opponent types 777/819, y=39.
     */
    private fun drawStrongTypes(canvas: Canvas) {
        val strong = state.strongTypesAgainstOpponent.take(4)
        val opponentTypes = state.opponentTypes.take(2)
        if (strong.isEmpty() && opponentTypes.isEmpty()) return

        val radius = 18f * u()
        val y = ry(39f)
        val spacing = rx(42f)
        val strongRight = rx(701f)
        val arrowX = rx(739f)
        val opponentStart = rx(777f)

        val firstStrong = strongRight - spacing * (strong.size - 1).coerceAtLeast(0)
        strong.forEachIndexed { index, type ->
            drawTypeChip(canvas, firstStrong + spacing * index, y, type, radius)
        }

        if (strong.isNotEmpty() && opponentTypes.isNotEmpty()) {
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 24f * u()
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.setShadowLayer(3f, 0f, 1f, Color.BLACK)
            canvas.drawText("›", arrowX, y + 7f * u(), paint)
            paint.clearShadowLayer()
        }

        opponentTypes.forEachIndexed { index, type ->
            drawTypeChip(canvas, opponentStart + spacing * index, y, type, radius)
        }
    }

    private fun drawMoveTriangle(canvas: Canvas) {
        // Keeps the enemy move cluster away from the human trainer.
        val centerX = rx(620f)
        val fastY = ry(355f)
        val chargedY = ry(478f)
        val spread = rx(72f)
        val fastRadius = 31f * u()
        val chargedRadius = 38f * u()

        state.fastMove?.let { move ->
            drawMoveCircle(canvas, centerX, fastY, fastRadius, move)
            drawMoveLabel(canvas, centerX, fastY + fastRadius + 20f * u(), shortMoveName(move.name) + " ▼")
            hitFast.set(
                centerX-fastRadius-22f*u(), fastY-fastRadius-12f*u(),
                centerX+fastRadius+22f*u(), fastY+fastRadius+42f*u()
            )
        } ?: hitFast.setEmpty()

        state.charged1?.let { prediction ->
            val cx = centerX - spread
            drawCharged(canvas, cx, chargedY, chargedRadius, prediction)
            drawMoveLabel(canvas, cx, chargedY + chargedRadius + 22f * u(), shortMoveName(prediction.move.name) + " ▼")
            hitCharge1.set(
                cx-chargedRadius-22f*u(), chargedY-chargedRadius-10f*u(),
                cx+chargedRadius+22f*u(), chargedY+chargedRadius+44f*u()
            )
        } ?: hitCharge1.setEmpty()

        state.charged2?.let { prediction ->
            val cx = centerX + spread
            drawCharged(canvas, cx, chargedY, chargedRadius, prediction)
            drawMoveLabel(canvas, cx, chargedY + chargedRadius + 22f * u(), shortMoveName(prediction.move.name) + " ▼")
            hitCharge2.set(
                cx-chargedRadius-22f*u(), chargedY-chargedRadius-10f*u(),
                cx+chargedRadius+22f*u(), chargedY+chargedRadius+44f*u()
            )
        } ?: hitCharge2.setEmpty()
    }

    /**
     * Mirrors GO's own charged buttons: the enemy move fills from bottom to top while energy builds.
     */
    private fun drawCharged(canvas: Canvas, cx: Float, cy: Float, radius: Float, prediction: ChargedPrediction) {
        val circle = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(circle)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(49, 55, 69), paint.alpha)
        canvas.drawRect(cx-radius, cy-radius, cx+radius, cy+radius, paint)

        if (settings.showEnergyProgress) {
            val progress = prediction.progress.coerceIn(0f, 1f)
            val fillTop = cy + radius - (2f * radius * progress)
            paint.color = withAlpha(typeColor(prediction.move.type), paint.alpha)
            canvas.drawRect(cx-radius, fillTop, cx+radius, cy+radius, paint)
            if (progress > .02f && progress < .98f) {
                paint.color = Color.argb((paint.alpha * .85f).toInt(), 255, 255, 255)
                canvas.drawRect(cx-radius, fillTop-1f*u(), cx+radius, fillTop+1f*u(), paint)
            }
        } else {
            paint.color = withAlpha(typeColor(prediction.move.type), paint.alpha)
            canvas.drawRect(cx-radius, cy-radius, cx+radius, cy+radius, paint)
        }
        canvas.restore()

        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, prediction.move.type, 23f * u())

        if (settings.showChargedCounter) {
            val badgeRadius = 14.5f * u()
            val badgeX = cx + radius * .74f
            val badgeY = cy + radius * .72f
            paint.color = if (prediction.ready) {
                withAlpha(Color.rgb(15, 185, 75), paint.alpha)
            } else {
                withAlpha(Color.rgb(28, 30, 34), paint.alpha)
            }
            canvas.drawCircle(badgeX, badgeY, badgeRadius, paint)
            stroke.strokeWidth = 1.7f * u()
            canvas.drawCircle(badgeX, badgeY, badgeRadius, stroke)
            paint.color = Color.WHITE
            paint.textSize = 15.5f * u()
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(prediction.fastMovesRemaining.toString(), badgeX, badgeY + 5.5f*u(), paint)
        }
    }

    private fun drawMoveCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, move: MoveDef) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(35, 40, 52), (paint.alpha * .65f).toInt())
        canvas.drawCircle(cx, cy + 2f*u(), radius + 3f*u(), paint)
        paint.color = withAlpha(typeColor(move.type), paint.alpha)
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, move.type, 20f * u())
    }

    private fun drawMoveLabel(canvas: Canvas, x: Float, y: Float, text: String) {
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 13.5f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3f, 0f, 1f, Color.BLACK)
        canvas.drawText(text, x, y, paint)
        paint.clearShadowLayer()
    }

    private fun drawReserves(canvas: Canvas) {
        val indicatorX = rx(827f)
        val firstY = ry(930f)
        val secondY = ry(1094f)
        val typeYDelta = ry(100f)

        state.reserves.take(2).forEachIndexed { index, reserve ->
            val y = if (index == 0) firstY else secondY
            drawMatchupIndicator(canvas, indicatorX, y, reserve.matchup, 16f * u())
            if (settings.showReserveTypes && reserve.types.isNotEmpty()) {
                val typeRadius = 13f * u()
                val spacing = rx(38f)
                val center = rx(783f)
                var x = center - if (reserve.types.size > 1) spacing/2f else 0f
                reserve.types.take(2).forEach { type ->
                    drawTypeChip(canvas, x, y + typeYDelta, type, typeRadius)
                    x += spacing
                }
            }
        }
    }

    private fun drawTimers(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = Color.WHITE
        state.ownSwitchSeconds?.let { canvas.drawText("↻ $it", 10f*u(), height*.47f, paint) }
        state.opponentSwitchSeconds?.let { canvas.drawText("↻ $it", width-58f*u(), height*.47f, paint) }
    }

    private fun drawResetAndCorrection(canvas: Canvas) {
        val y = ry(274f)
        val radius = 15f * u()
        val resetX = rx(22f)
        hitReset.set(resetX-radius, y-radius, resetX+radius, y+radius)
        paint.color = Color.argb(paint.alpha, 45, 45, 45)
        canvas.drawCircle(resetX, y, radius, paint)
        canvas.drawCircle(resetX, y, radius, stroke)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 16f * u()
        canvas.drawText("↻", resetX, y+5f*u(), paint)

        val minusX = resetX
        val plusX = resetX + 38f*u()
        val correctionY = y + 40f*u()
        hitMinus.set(minusX-radius, correctionY-radius, minusX+radius, correctionY+radius)
        hitPlus.set(plusX-radius, correctionY-radius, plusX+radius, correctionY+radius)
        listOf(minusX to "−", plusX to "+").forEach { (x, glyph) ->
            paint.color = Color.argb(paint.alpha, 45, 45, 45)
            canvas.drawCircle(x, correctionY, radius, paint)
            canvas.drawCircle(x, correctionY, radius, stroke)
            paint.color = Color.WHITE
            canvas.drawText(glyph, x, correctionY+5f*u(), paint)
        }
    }

    private fun drawDebug(canvas: Canvas) {
        val text = state.debugText ?: return
        paint.color = Color.argb(190, 0, 0, 0)
        val box = RectF(8f, height-166f*u(), width*.82f, height-8f)
        canvas.drawRoundRect(box, 8f, 8f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f * u()
        text.lines().take(9).forEachIndexed { index, line ->
            canvas.drawText(line, box.left+8f, box.top+(index+1)*14f*u(), paint)
        }
    }

    private fun drawPopup(canvas: Canvas, popupState: PopupState) {
        val itemHeight = 34f * u()
        val popupWidth = min(240f * u(), width * .46f)
        val left = (popupState.anchor.centerX() - popupWidth/2).coerceIn(4f, width-popupWidth-4f)
        val top = (popupState.anchor.bottom + 4f)
            .coerceAtMost(height - popupState.options.size*itemHeight - 8f)
        val box = RectF(left, top, left+popupWidth, top+popupState.options.size*itemHeight)
        paint.color = Color.argb(235, 20, 25, 32)
        canvas.drawRoundRect(box, 9f, 9f, paint)

        popupState.options.forEachIndexed { index, move ->
            val y = top + index*itemHeight
            if (index > 0) {
                paint.color = Color.argb(90, 255, 255, 255)
                canvas.drawRect(left, y, left+popupWidth, y+1f, paint)
            }
            drawTypeChip(canvas, left+19f*u(), y+itemHeight/2, move.type, 9f*u())
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 12f*u()
            canvas.drawText(shortMoveName(move.name), left+37f*u(), y+21f*u(), paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val x = event.x
        val y = event.y

        popup?.let { popupState ->
            val itemHeight = 34f * u()
            val popupWidth = min(240f * u(), width * .46f)
            val left = (popupState.anchor.centerX() - popupWidth/2).coerceIn(4f, width-popupWidth-4f)
            val top = (popupState.anchor.bottom + 4f)
                .coerceAtMost(height - popupState.options.size*itemHeight - 8f)
            if (x in left..(left+popupWidth) && y in top..(top+popupState.options.size*itemHeight)) {
                val index = ((y-top)/itemHeight).toInt().coerceIn(0, popupState.options.lastIndex)
                callbacks.onMoveSelected(popupState.kind, popupState.options[index])
                popup = null
                invalidate()
                return true
            }
            popup = null
            invalidate()
            return true
        }

        when {
            hitFast.contains(x, y) -> callbacks.onFastMoveTapped()
            hitCharge1.contains(x, y) -> callbacks.onCharged1Tapped()
            hitCharge2.contains(x, y) -> callbacks.onCharged2Tapped()
            hitReset.contains(x, y) -> callbacks.onResetTapped()
            hitMinus.contains(x, y) -> callbacks.onManualFastDelta(-1)
            hitPlus.contains(x, y) -> callbacks.onManualFastDelta(1)
            else -> return false
        }
        return true
    }

    private fun drawTypeChip(canvas: Canvas, cx: Float, cy: Float, type: String, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(29, 34, 45), (paint.alpha * .55f).toInt())
        canvas.drawCircle(cx, cy + 1.5f*u(), radius + 2.2f*u(), paint)
        paint.color = withAlpha(typeColor(type), paint.alpha)
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.color = Color.argb((paint.alpha * .86f).toInt(), 255, 255, 255)
        stroke.strokeWidth = 1.35f * u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, type, radius * .95f)
        stroke.color = Color.WHITE
    }

    /**
     * Uses the 18 white silhouettes traced from the icon sheet supplied for this project.
     * EVEN_ODD contours in TypeGlyphPaths preserve holes in Normal/Steel/Psychic/etc.
     */
    private fun drawTypeGlyph(canvas: Canvas, cx: Float, cy: Float, type: String, size: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(paint.alpha, 255, 255, 255)
        val path = TypeGlyphPaths.build(type, cx, cy, size * 1.18f)
        if (path != null) {
            canvas.drawPath(path, paint)
            return
        }
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = size * 1.05f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(type.take(1).uppercase().ifBlank { "?" }, cx, cy + size*.35f, paint)
    }

    private fun shortMoveName(name: String): String =
        if (name.length <= 19) name else name.take(18) + "…"

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    // Palette sampled from the supplied Pokémon GO-style icon sheet.
    private fun typeColor(type: String): Int = when (type.lowercase()) {
        "fairy" -> Color.rgb(239, 153, 230)
        "psychic" -> Color.rgb(250, 137, 131)
        "fighting" -> Color.rgb(219, 66, 86)
        "rock" -> Color.rgb(206, 193, 141)
        "fire" -> Color.rgb(251, 165, 75)
        "steel" -> Color.rgb(85, 151, 164)
        "water" -> Color.rgb(91, 166, 224)
        "flying" -> Color.rgb(155, 180, 229)
        "ghost" -> Color.rgb(99, 111, 191)
        "grass" -> Color.rgb(93, 190, 101)
        "bug" -> Color.rgb(161, 194, 49)
        "ground" -> Color.rgb(214, 133, 85)
        "ice" -> Color.rgb(125, 212, 200)
        "dark" -> Color.rgb(113, 121, 140)
        "dragon" -> Color.rgb(7, 115, 200)
        "normal" -> Color.rgb(153, 156, 161)
        "electric" -> Color.rgb(243, 219, 85)
        "poison" -> Color.rgb(178, 98, 204)
        else -> Color.rgb(153, 156, 161)
    }
}
