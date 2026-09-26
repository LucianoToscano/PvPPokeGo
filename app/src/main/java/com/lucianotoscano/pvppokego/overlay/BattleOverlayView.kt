package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.SettingsRepository
import kotlin.math.ceil
import kotlin.math.min

/**
 * Canonical PvPPokeGo battle HUD.
 *
 * The native Pokemon GO HUD is never replaced. Extra information is anchored around it and
 * tuned against the approved 864x1536 reference layout.
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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.WHITE
    }

    private var state = BattleUiState()
    private var scaleFactor = settings.scale
    private var opacityFactor = settings.opacity
    private var hudAlpha = 255
    private var collapsed = false

    fun render(newState: BattleUiState) {
        state = newState
        scaleFactor = settings.scale
        opacityFactor = settings.opacity
        invalidate()
    }

    fun setCollapsed(value: Boolean) {
        collapsed = value
        invalidate()
    }

    fun isCollapsed(): Boolean = collapsed

    // Native PopupMenu windows are used by BattleOverlayService, so these are intentionally
    // lightweight compatibility hooks for the old callback surface.
    fun showMoveMenu(kind: MoveKind, options: List<MoveDef>) = Unit
    fun dismissMenu() = Unit

    private fun sx(): Float = width / 864f
    private fun sy(): Float = height / 1536f
    private fun u(): Float = min(sx(), sy()) * (scaleFactor / .8f).coerceIn(.62f, 1.5f)
    private fun rx(v: Float): Float = v * sx()
    private fun ry(v: Float): Float = v * sy()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Opacity is applied exactly once. 100% means fully solid foreground icons.
        hudAlpha = (255f * opacityFactor).toInt().coerceIn(76, 255)
        paint.alpha = 255
        stroke.alpha = 255
        stroke.color = hudColor(Color.WHITE)

        if (collapsed) {
            drawVisibilityToggle(canvas, hidden = true)
            return
        }

        if (settings.showCurrentIndicator) {
            drawMatchupIndicator(
                canvas = canvas,
                cx = rx(78f),
                cy = ry(TOP_ICON_Y),
                matchup = state.playerMatchup,
                radius = 17.5f * u()
            )
        }

        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (settings.analyzeReserves) drawReserves(canvas)
        if (settings.showSwitchTimer) drawTimers(canvas)
        if (settings.editMode) drawResetAndCorrection(canvas)
        if (settings.debugMode) drawDebug(canvas)

        drawVisibilityToggle(canvas, hidden = false)
    }

    /** Bottom-center eye/eye-slash control from the approved reference. */
    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
        val cx = rx(432f)
        val cy = ry(1480f)
        val radius = 21f * u()

        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(72, 76, 82), .90f)
        canvas.drawCircle(cx, cy, radius, paint)

        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 1.5f * u()
        stroke.color = hudColor(Color.WHITE, .65f)
        canvas.drawCircle(cx, cy, radius, stroke)

        val eye = RectF(
            cx - 10.5f * u(), cy - 6.2f * u(),
            cx + 10.5f * u(), cy + 6.2f * u()
        )
        stroke.color = hudColor(Color.WHITE)
        stroke.strokeWidth = 2.2f * u()
        canvas.drawOval(eye, stroke)

        paint.color = hudColor(Color.WHITE)
        canvas.drawCircle(cx, cy, 3.2f * u(), paint)

        if (!hidden) {
            stroke.strokeWidth = 2.6f * u()
            canvas.drawLine(
                cx - 11.5f * u(), cy + 10f * u(),
                cx + 11.5f * u(), cy - 10f * u(),
                stroke
            )
        }
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
            MatchupState.FAVORABLE -> hudColor(Color.rgb(20, 190, 80))
            MatchupState.UNFAVORABLE -> hudColor(Color.rgb(225, 45, 55))
            MatchupState.NEUTRAL -> hudColor(Color.rgb(145, 145, 145))
            MatchupState.UNKNOWN -> hudColor(Color.rgb(105, 105, 105))
        }
        canvas.drawCircle(cx, cy, radius, paint)

        stroke.color = hudColor(Color.WHITE)
        stroke.strokeWidth = 2.0f * u()
        canvas.drawCircle(cx, cy, radius, stroke)

        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        when (matchup) {
            MatchupState.FAVORABLE -> {
                paint.textSize = 19f * u()
                canvas.drawText("✓", cx, cy + 6.0f * u(), paint)
            }
            MatchupState.UNFAVORABLE -> {
                paint.textSize = 20f * u()
                canvas.drawText("×", cx, cy + 6.3f * u(), paint)
            }
            MatchupState.NEUTRAL -> canvas.drawCircle(cx, cy, 5.6f * u(), paint)
            MatchupState.UNKNOWN -> {
                paint.textSize = 13f * u()
                canvas.drawText("?", cx, cy + 4.5f * u(), paint)
            }
        }
    }

    /** Only weaknesses are added; Pokemon GO's native opponent-type icons remain untouched. */
    private fun drawStrongTypes(canvas: Canvas) {
        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isEmpty()) return

        val radius = 16.5f * u()
        val y = ry(TOP_ICON_Y)
        val spacing = rx(39f)
        // Slightly closer to the game's native right-side type row than the previous build.
        val strongRight = rx(712f)
        val arrowX = rx(750f)

        val firstStrong = strongRight - spacing * (strong.size - 1).coerceAtLeast(0)
        strong.forEachIndexed { index, type ->
            drawTypeChip(canvas, firstStrong + spacing * index, y, type, radius)
        }

        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 21f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(2.5f, 0f, 1f, Color.BLACK)
        canvas.drawText("›", arrowX, y + 6.5f * u(), paint)
        paint.clearShadowLayer()
    }

    /** Fast above, two charged below. Height is preserved; only horizontal center was corrected. */
    private fun drawMoveTriangle(canvas: Canvas) {
        val centerX = rx(ENEMY_MOVE_CENTER_X)
        val fastY = ry(355f)
        val chargedY = ry(478f)
        val spread = rx(72f)
        val fastRadius = 31f * u()
        val chargedRadius = 38f * u()

        state.fastMove?.let { move ->
            drawMoveCircle(canvas, centerX, fastY, fastRadius, move)
            drawMoveLabel(canvas, centerX, fastY + fastRadius + 20f*u(), shortMoveName(move.name) + " ▼")
        }

        state.charged1?.let { prediction ->
            val cx = centerX - spread
            drawCharged(canvas, cx, chargedY, chargedRadius, prediction)
            drawMoveLabel(canvas, cx, chargedY + chargedRadius + 22f*u(), shortMoveName(prediction.move.name) + " ▼")
        }

        state.charged2?.let { prediction ->
            val cx = centerX + spread
            drawCharged(canvas, cx, chargedY, chargedRadius, prediction)
            drawMoveLabel(canvas, cx, chargedY + chargedRadius + 22f*u(), shortMoveName(prediction.move.name) + " ▼")
        }
    }

    /** Enemy Charged Move fills from bottom to top from the shared tracked energy value. */
    private fun drawCharged(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        prediction: ChargedPrediction
    ) {
        val circle = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(circle)

        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(49, 55, 69))
        canvas.drawRect(cx-radius, cy-radius, cx+radius, cy+radius, paint)

        if (settings.showEnergyProgress) {
            val progress = prediction.progress.coerceIn(0f, 1f)
            val fillTop = cy + radius - 2f * radius * progress
            paint.color = hudColor(typeColor(prediction.move.type))
            canvas.drawRect(cx-radius, fillTop, cx+radius, cy+radius, paint)
            if (progress > .02f && progress < .98f) {
                paint.color = hudColor(Color.WHITE, .84f)
                canvas.drawRect(cx-radius, fillTop-1f*u(), cx+radius, fillTop+1f*u(), paint)
            }
        } else {
            paint.color = hudColor(typeColor(prediction.move.type))
            canvas.drawRect(cx-radius, cy-radius, cx+radius, cy+radius, paint)
        }
        canvas.restore()

        stroke.color = hudColor(Color.WHITE)
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, prediction.move.type, 23f*u())

        if (settings.showChargedCounter) {
            drawChargeProgressDots(canvas, cx, cy, radius, prediction.progress, prediction.ready)
        }
    }

    private fun drawChargeProgressDots(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        progress: Float,
        ready: Boolean
    ) {
        val totalDots = 5
        val normalized = progress.coerceIn(0f, 1f)
        val filled = if (ready) totalDots else ceil(normalized * totalDots).toInt().coerceIn(0, totalDots)
        val dotRadius = 2.5f * u()
        val gap = 7.5f * u()
        val startX = cx - gap * (totalDots - 1) / 2f
        val y = cy + radius * .72f

        repeat(totalDots) { index ->
            paint.style = Paint.Style.FILL
            paint.color = if (index < filled) hudColor(Color.WHITE)
            else hudColor(Color.rgb(34, 37, 44), .92f)
            canvas.drawCircle(startX + index*gap, y, dotRadius, paint)
        }
    }

    private fun drawMoveCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, move: MoveDef) {
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(35, 40, 52), .72f)
        canvas.drawCircle(cx, cy + 2f*u(), radius + 3f*u(), paint)
        paint.color = hudColor(typeColor(move.type))
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.color = hudColor(Color.WHITE)
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, move.type, 20f*u())
    }

    private fun drawMoveLabel(canvas: Canvas, x: Float, y: Float, text: String) {
        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 13.5f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3f, 0f, 1f, Color.BLACK)
        canvas.drawText(text, x, y, paint)
        paint.clearShadowLayer()
    }

    /**
     * Native reserve cards stay untouched. Extra icons are anchored tightly to each card.
     */
    private fun drawReserves(canvas: Canvas) {
        val indicatorX = rx(827f)
        val firstY = ry(905f)
        val secondY = ry(1052f)
        val typeYDelta = ry(61f)

        state.reserves.take(2).forEachIndexed { index, reserve ->
            val cardCenterY = if (index == 0) firstY else secondY

            drawMatchupIndicator(
                canvas = canvas,
                cx = indicatorX,
                cy = cardCenterY,
                matchup = reserve.matchup,
                radius = 14.5f * u()
            )

            if (settings.showReserveTypes && reserve.types.isNotEmpty()) {
                val typeRadius = 12.5f * u()
                val spacing = rx(33f)
                val typesCenterX = rx(782f)
                var x = typesCenterX - if (reserve.types.size > 1) spacing/2f else 0f
                reserve.types.take(2).forEach { type ->
                    drawTypeChip(canvas, x, cardCenterY + typeYDelta, type, typeRadius)
                    x += spacing
                }
            }
        }
    }

    private fun drawTimers(canvas: Canvas) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f * u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = hudColor(Color.WHITE)
        state.ownSwitchSeconds?.let { canvas.drawText("↻ $it", 10f*u(), height*.47f, paint) }
        state.opponentSwitchSeconds?.let { canvas.drawText("↻ $it", width-58f*u(), height*.47f, paint) }
    }

    private fun drawResetAndCorrection(canvas: Canvas) {
        val y = ry(274f)
        val radius = 15f*u()
        val resetX = rx(22f)
        paint.color = hudColor(Color.rgb(45,45,45))
        canvas.drawCircle(resetX, y, radius, paint)
        stroke.color = hudColor(Color.WHITE)
        canvas.drawCircle(resetX, y, radius, stroke)
        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 16f*u()
        canvas.drawText("↻", resetX, y+5f*u(), paint)

        val minusX = resetX
        val plusX = resetX + 38f*u()
        val correctionY = y + 40f*u()
        listOf(minusX to "−", plusX to "+").forEach { (x, glyph) ->
            paint.color = hudColor(Color.rgb(45,45,45))
            canvas.drawCircle(x, correctionY, radius, paint)
            canvas.drawCircle(x, correctionY, radius, stroke)
            paint.color = hudColor(Color.WHITE)
            canvas.drawText(glyph, x, correctionY+5f*u(), paint)
        }
    }

    private fun drawDebug(canvas: Canvas) {
        val text = state.debugText ?: return
        paint.color = hudColor(Color.BLACK, .78f)
        val box = RectF(8f, height-166f*u(), width*.82f, height-8f)
        canvas.drawRoundRect(box, 8f, 8f, paint)
        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f*u()
        text.lines().take(9).forEachIndexed { index, line ->
            canvas.drawText(line, box.left+8f, box.top+(index+1)*14f*u(), paint)
        }
    }

    private fun drawTypeChip(canvas: Canvas, cx: Float, cy: Float, type: String, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(29,34,45), .62f)
        canvas.drawCircle(cx, cy+1.5f*u(), radius+2.2f*u(), paint)
        paint.color = hudColor(typeColor(type))
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.color = hudColor(Color.WHITE, .92f)
        stroke.strokeWidth = 1.35f*u()
        canvas.drawCircle(cx, cy, radius, stroke)
        drawTypeGlyph(canvas, cx, cy, type, radius*.95f)
        stroke.color = hudColor(Color.WHITE)
    }

    private fun drawTypeGlyph(canvas: Canvas, cx: Float, cy: Float, type: String, size: Float) {
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.WHITE)
        val path = TypeGlyphPaths.build(type, cx, cy, size*1.18f)
        if (path != null) {
            canvas.drawPath(path, paint)
            return
        }
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = size*1.05f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(type.take(1).uppercase().ifBlank { "?" }, cx, cy+size*.35f, paint)
    }

    private fun shortMoveName(name: String): String =
        if (name.length <= 19) name else name.take(18) + "…"

    private fun hudColor(color: Int, multiplier: Float = 1f): Int {
        val alpha = (hudAlpha*multiplier).toInt().coerceIn(0,255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun typeColor(type: String): Int = when (type.lowercase()) {
        "fairy" -> Color.rgb(239,153,230)
        "psychic" -> Color.rgb(250,137,131)
        "fighting" -> Color.rgb(219,66,86)
        "rock" -> Color.rgb(206,193,141)
        "fire" -> Color.rgb(251,165,75)
        "steel" -> Color.rgb(85,151,164)
        "water" -> Color.rgb(91,166,224)
        "flying" -> Color.rgb(155,180,229)
        "ghost" -> Color.rgb(99,111,191)
        "grass" -> Color.rgb(93,190,101)
        "bug" -> Color.rgb(161,194,49)
        "ground" -> Color.rgb(214,133,85)
        "ice" -> Color.rgb(125,212,200)
        "dark" -> Color.rgb(113,121,140)
        "dragon" -> Color.rgb(7,115,200)
        "normal" -> Color.rgb(153,156,161)
        "electric" -> Color.rgb(243,219,85)
        "poison" -> Color.rgb(178,98,204)
        else -> Color.rgb(153,156,161)
    }

    companion object {
        // Intentionally clips a few pixels like the native Pokemon GO top-row chips.
        private const val TOP_ICON_Y = 11f
        const val ENEMY_MOVE_CENTER_X = 558f
    }
}
