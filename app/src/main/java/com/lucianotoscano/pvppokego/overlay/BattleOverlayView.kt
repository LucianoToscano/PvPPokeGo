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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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
    private var collapsed = false

    private val hitFast = RectF()
    private val hitCharge1 = RectF()
    private val hitCharge2 = RectF()
    private val hitReset = RectF()
    private val hitMinus = RectF()
    private val hitPlus = RectF()
    private var popup: PopupState? = null

    data class PopupState(val kind: MoveKind, val anchor: RectF, val options: List<MoveDef>)

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

    fun dismissMenu() { popup = null; invalidate() }

    /**
     * The approved reference mockups use an 864x1536 canvas. Keep all anchors tied to those
     * coordinates so 9:16 phones preserve the same composition instead of drifting by dp.
     */
    private fun sx(): Float = width / 864f
    private fun sy(): Float = height / 1536f
    private fun u(): Float = min(sx(), sy()) * (scaleFactor / .8f).coerceIn(.62f, 1.5f)
    private fun rx(v: Float): Float = v * sx()
    private fun ry(v: Float): Float = v * sy()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.alpha = (255 * opacityFactor).toInt().coerceIn(40, 255)
        stroke.alpha = paint.alpha

        if (collapsed) {
            drawRestoreControl(canvas, rx(42f), ry(38f))
            return
        }

        // Canonical top-left pair from the approved mockup: matchup at x=42 and hide at x=96.
        if (settings.showCurrentIndicator) drawMatchupIndicator(canvas, rx(42f), ry(38f), state.playerMatchup, 19f * u())
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

    private fun drawMatchupIndicator(canvas: Canvas, cx: Float, cy: Float, m: MatchupState, radius: Float = 17f * u()) {
        paint.style = Paint.Style.FILL
        paint.color = when (m) {
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
        val glyph = when (m) {
            MatchupState.FAVORABLE -> "✓"
            MatchupState.UNFAVORABLE -> "×"
            else -> "•"
        }
        canvas.drawText(glyph, cx, cy + 6.5f * u(), paint)
    }

    /**
     * Exact anchor positions measured from the approved 864x1536 mockup:
     * strong types centres ~= 574,616,658,701; arrow ~= 739; opponent types ~= 777,819.
     * Strong types are right-aligned to the arrow when fewer than four are present.
     */
    private fun drawStrongTypes(canvas: Canvas) {
        val strong = state.strongTypesAgainstOpponent.take(4)
        val opponentTypes = state.opponentTypes.take(2)
        if (strong.isEmpty() && opponentTypes.isEmpty()) return

        val r = 18f * u()
        val y = ry(39f)
        val spacing = rx(42f)
        val strongRight = rx(701f)
        val arrowX = rx(739f)
        val opponentStart = rx(777f)

        val firstStrong = strongRight - spacing * (strong.size - 1).coerceAtLeast(0)
        strong.forEachIndexed { i, type ->
            drawTypeChip(canvas, firstStrong + spacing * i, y, type, r)
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

        opponentTypes.forEachIndexed { i, type ->
            drawTypeChip(canvas, opponentStart + spacing * i, y, type, r)
        }
    }

    private fun drawMoveTriangle(canvas: Canvas) {
        val centerX = rx(620f)
        val fastY = ry(355f)
        val chargeY = ry(478f)
        val spread = rx(72f)
        val fastR = 31f * u()
        val chargedR = 38f * u()

        state.fastMove?.let { m ->
            drawMoveCircle(canvas, centerX, fastY, fastR, m)
            drawMoveLabel(canvas, centerX, fastY + fastR + 20f * u(), shortMoveName(m.name) + " ▼")
            hitFast.set(centerX-fastR-22f*u(), fastY-fastR-12f*u(), centerX+fastR+22f*u(), fastY+fastR+42f*u())
        } ?: hitFast.setEmpty()

        state.charged1?.let { p ->
            val cx = centerX - spread
            drawCharged(canvas, cx, chargeY, chargedR, p)
            drawMoveLabel(canvas, cx, chargeY + chargedR + 22f * u(), shortMoveName(p.move.name) + " ▼")
            hitCharge1.set(cx-chargedR-22f*u(), chargeY-chargedR-10f*u(), cx+chargedR+22f*u(), chargeY+chargedR+44f*u())
        } ?: hitCharge1.setEmpty()

        state.charged2?.let { p ->
            val cx = centerX + spread
            drawCharged(canvas, cx, chargeY, chargedR, p)
            drawMoveLabel(canvas, cx, chargeY + chargedR + 22f * u(), shortMoveName(p.move.name) + " ▼")
            hitCharge2.set(cx-chargedR-22f*u(), chargeY-chargedR-10f*u(), cx+chargedR+22f*u(), chargeY+chargedR+44f*u())
        } ?: hitCharge2.setEmpty()
    }

    /**
     * Enemy charged icon mirrors Pokémon GO's own charged buttons: energy rises from the bottom
     * of the circle. The remaining upper portion stays dark until enough energy is available.
     */
    private fun drawCharged(canvas: Canvas, cx: Float, cy: Float, r: Float, p: ChargedPrediction) {
        val circle = Path().apply { addCircle(cx, cy, r, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(circle)

        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(49, 55, 69), paint.alpha)
        canvas.drawRect(cx-r, cy-r, cx+r, cy+r, paint)

        if (settings.showEnergyProgress) {
            val progress = p.progress.coerceIn(0f, 1f)
            val fillTop = cy + r - (2f * r * progress)
            paint.color = withAlpha(typeColor(p.move.type), paint.alpha)
            canvas.drawRect(cx-r, fillTop, cx+r, cy+r, paint)
            if (progress > .02f && progress < .98f) {
                paint.color = Color.argb((paint.alpha * .85f).toInt(), 255, 255, 255)
                canvas.drawRect(cx-r, fillTop-1f*u(), cx+r, fillTop+1f*u(), paint)
            }
        } else {
            paint.color = withAlpha(typeColor(p.move.type), paint.alpha)
            canvas.drawRect(cx-r, cy-r, cx+r, cy+r, paint)
        }
        canvas.restore()

        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.2f * u()
        canvas.drawCircle(cx, cy, r, stroke)
        drawTypeGlyph(canvas, cx, cy, p.move.type, 23f * u())

        if (settings.showChargedCounter) {
            val br = 14.5f * u()
            val bx = cx + r * .74f
            val by = cy + r * .72f
            paint.color = if (p.ready) withAlpha(Color.rgb(15, 185, 75), paint.alpha) else withAlpha(Color.rgb(28, 30, 34), paint.alpha)
            canvas.drawCircle(bx, by, br, paint)
            stroke.strokeWidth = 1.7f * u()
            canvas.drawCircle(bx, by, br, stroke)
            paint.color = Color.WHITE
            paint.textSize = 15.5f * u()
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(p.fastMovesRemaining.toString(), bx, by + 5.5f*u(), paint)
        }
    }

    private fun drawMoveCircle(canvas: Canvas, cx: Float, cy: Float, r: Float, move: MoveDef) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(35, 40, 52), (paint.alpha * .65f).toInt())
        canvas.drawCircle(cx, cy + 2f*u(), r + 3f*u(), paint)
        paint.color = withAlpha(typeColor(move.type), paint.alpha)
        canvas.drawCircle(cx, cy, r, paint)
        stroke.color = Color.WHITE
        stroke.strokeWidth = 2.2f*u()
        canvas.drawCircle(cx, cy, r, stroke)
        drawTypeGlyph(canvas, cx, cy, move.type, 20f*u())
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
        // Positions measured from the approved reference cards on the right side.
        val indicatorX = rx(827f)
        val firstY = ry(930f)
        val secondY = ry(1094f)
        val typeYDelta = ry(100f)
        state.reserves.take(2).forEachIndexed { i, reserve ->
            val y = if (i == 0) firstY else secondY
            drawMatchupIndicator(canvas, indicatorX, y, reserve.matchup, 16f*u())
            if (settings.showReserveTypes && reserve.types.isNotEmpty()) {
                val typeR = 13f*u()
                val spacing = rx(38f)
                val center = rx(783f)
                var x = center - if (reserve.types.size > 1) spacing/2f else 0f
                reserve.types.take(2).forEach { t ->
                    drawTypeChip(canvas, x, y + typeYDelta, t, typeR)
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
        val r = 15f * u()
        val resetX = rx(22f)
        hitReset.set(resetX-r, y-r, resetX+r, y+r)
        paint.color = Color.argb(paint.alpha, 45, 45, 45)
        canvas.drawCircle(resetX, y, r, paint); canvas.drawCircle(resetX, y, r, stroke)
        paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER; paint.textSize = 16f*u()
        canvas.drawText("↻", resetX, y+5f*u(), paint)

        val minusX = resetX
        val plusX = resetX + 38f*u()
        val cy = y + 40f*u()
        hitMinus.set(minusX-r, cy-r, minusX+r, cy+r)
        hitPlus.set(plusX-r, cy-r, plusX+r, cy+r)
        for ((x,g) in listOf(minusX to "−", plusX to "+")) {
            paint.color = Color.argb(paint.alpha, 45,45,45); canvas.drawCircle(x, cy, r, paint); canvas.drawCircle(x,cy,r,stroke)
            paint.color = Color.WHITE; canvas.drawText(g, x, cy+5f*u(), paint)
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
        text.lines().take(9).forEachIndexed { i, line ->
            canvas.drawText(line, box.left+8f, box.top+(i+1)*14f*u(), paint)
        }
    }

    private fun drawPopup(canvas: Canvas, p: PopupState) {
        val itemH = 34f * u()
        val w = min(240f * u(), width * .46f)
        val left = (p.anchor.centerX() - w/2).coerceIn(4f, width-w-4f)
        val top = (p.anchor.bottom + 4f).coerceAtMost(height - p.options.size*itemH - 8f)
        val box = RectF(left, top, left+w, top+p.options.size*itemH)
        paint.color = Color.argb(235, 20, 25, 32)
        canvas.drawRoundRect(box, 9f, 9f, paint)
        p.options.forEachIndexed { i, move ->
            val y = top + i*itemH
            if (i>0) { paint.color = Color.argb(90,255,255,255); canvas.drawRect(left, y, left+w, y+1f, paint) }
            drawTypeChip(canvas, left+19f*u(), y+itemH/2, move.type, 9f*u())
            paint.color = Color.WHITE; paint.textAlign = Paint.Align.LEFT; paint.textSize = 12f*u()
            canvas.drawText(shortMoveName(move.name), left+37f*u(), y+21f*u(), paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val x = event.x; val y = event.y
        popup?.let { p ->
            val itemH = 34f * u()
            val w = min(240f * u(), width * .46f)
            val left = (p.anchor.centerX() - w/2).coerceIn(4f, width-w-4f)
            val top = (p.anchor.bottom + 4f).coerceAtMost(height - p.options.size*itemH - 8f)
            if (x in left..(left+w) && y in top..(top+p.options.size*itemH)) {
                val idx = ((y-top)/itemH).toInt().coerceIn(0, p.options.lastIndex)
                callbacks.onMoveSelected(p.kind, p.options[idx])
                popup = null; invalidate(); return true
            }
            popup = null; invalidate(); return true
        }
        when {
            hitFast.contains(x,y) -> callbacks.onFastMoveTapped()
            hitCharge1.contains(x,y) -> callbacks.onCharged1Tapped()
            hitCharge2.contains(x,y) -> callbacks.onCharged2Tapped()
            hitReset.contains(x,y) -> callbacks.onResetTapped()
            hitMinus.contains(x,y) -> callbacks.onManualFastDelta(-1)
            hitPlus.contains(x,y) -> callbacks.onManualFastDelta(1)
            else -> return false
        }
        return true
    }

    private fun drawTypeChip(canvas: Canvas, cx: Float, cy: Float, type: String, r: Float) {
        paint.style = Paint.Style.FILL
        paint.color = withAlpha(Color.rgb(29, 34, 45), (paint.alpha * .55f).toInt())
        canvas.drawCircle(cx, cy + 1.5f*u(), r + 2.2f*u(), paint)
        paint.color = withAlpha(typeColor(type), paint.alpha)
        canvas.drawCircle(cx, cy, r, paint)
        stroke.color = Color.argb((paint.alpha * .86f).toInt(), 255, 255, 255)
        stroke.strokeWidth = 1.35f * u()
        canvas.drawCircle(cx, cy, r, stroke)
        drawTypeGlyph(canvas, cx, cy, type, r*.95f)
        stroke.color = Color.WHITE
    }

    /** White silhouettes use the same visual vocabulary as the supplied Pokémon GO-style set. */
    private fun drawTypeGlyph(canvas: Canvas, cx: Float, cy: Float, type: String, size: Float) {
        val t = type.lowercase()
        val white = Color.argb(paint.alpha, 255,255,255)
        paint.style = Paint.Style.FILL
        paint.color = white
        stroke.color = white
        stroke.strokeWidth = (size*.15f).coerceAtLeast(1.4f)
        stroke.style = Paint.Style.STROKE
        stroke.strokeCap = Paint.Cap.ROUND
        stroke.strokeJoin = Paint.Join.ROUND
        val s = size
        when (t) {
            "water" -> {
                val p = Path(); p.moveTo(cx, cy-s*.72f); p.cubicTo(cx-s*.42f, cy-s*.10f, cx-s*.50f, cy+s*.18f, cx, cy+s*.58f); p.cubicTo(cx+s*.50f, cy+s*.18f, cx+s*.42f, cy-s*.10f, cx, cy-s*.72f); p.close(); canvas.drawPath(p, paint)
            }
            "fire" -> {
                val p = Path(); p.moveTo(cx+s*.08f, cy-s*.72f); p.cubicTo(cx+s*.42f,cy-s*.25f,cx+s*.48f,cy+s*.08f,cx+s*.18f,cy+s*.57f); p.cubicTo(cx+s*.12f,cy+s*.20f,cx-s*.05f,cy+s*.04f,cx-s*.23f,cy-s*.10f); p.cubicTo(cx-s*.48f,cy+s*.20f,cx-s*.32f,cy+s*.55f,cx,cy+s*.66f); p.cubicTo(cx-s*.54f,cy+s*.48f,cx-s*.59f,cy-.02f,cx-s*.18f,cy-s*.42f); p.cubicTo(cx-s*.04f,cy-s*.56f,cx+s*.02f,cy-s*.65f,cx+s*.08f,cy-s*.72f); p.close(); canvas.drawPath(p,paint)
            }
            "electric" -> {
                val p=Path(); p.moveTo(cx+s*.10f,cy-s*.78f); p.lineTo(cx-s*.45f,cy+s*.03f); p.lineTo(cx-s*.06f,cy+s*.03f); p.lineTo(cx-s*.18f,cy+s*.72f); p.lineTo(cx+s*.48f,cy-s*.12f); p.lineTo(cx+s*.07f,cy-s*.12f); p.close(); canvas.drawPath(p,paint)
            }
            "ground" -> {
                val p=Path(); p.moveTo(cx-s*.68f,cy+s*.40f); p.lineTo(cx-s*.28f,cy-s*.46f); p.lineTo(cx+s*.38f,cy-s*.46f); p.lineTo(cx+s*.68f,cy+s*.40f); p.close(); canvas.drawPath(p,paint)
            }
            "rock" -> {
                val p=Path(); p.moveTo(cx-s*.62f,cy+s*.34f); p.lineTo(cx-s*.48f,cy-s*.32f); p.lineTo(cx+s*.04f,cy-s*.58f); p.lineTo(cx+s*.60f,cy-s*.16f); p.lineTo(cx+s*.42f,cy+s*.47f); p.lineTo(cx-s*.12f,cy+s*.56f); p.close(); canvas.drawPath(p,paint)
            }
            "flying" -> {
                val p=Path(); p.moveTo(cx-s*.68f,cy+s*.30f); p.cubicTo(cx-s*.10f,cy-s*.68f,cx+s*.60f,cy-s*.62f,cx+s*.72f,cy-s*.50f); p.cubicTo(cx+s*.26f,cy-s*.08f,cx-s*.05f,cy+s*.04f,cx-s*.30f,cy+s*.10f); p.cubicTo(cx+s*.05f,cy+s*.10f,cx+s*.28f,cy+s*.03f,cx+s*.43f,cy-s*.04f); p.cubicTo(cx+s*.10f,cy+s*.48f,cx-s*.35f,cy+s*.58f,cx-s*.68f,cy+s*.30f); p.close(); canvas.drawPath(p,paint)
            }
            "ice" -> drawSnowflake(canvas,cx,cy,s)
            "psychic" -> {
                canvas.drawCircle(cx,cy,s*.48f,stroke); canvas.drawCircle(cx,cy,s*.18f,stroke); canvas.drawArc(RectF(cx-s*.65f,cy-s*.65f,cx+s*.65f,cy+s*.65f),205f,205f,false,stroke)
            }
            "fighting" -> drawFist(canvas,cx,cy,s)
            "dark" -> {
                canvas.drawCircle(cx-s*.08f,cy,s*.56f,paint)
                val bg = Paint(paint).apply { color = withAlpha(typeColor(type), paint.alpha) }
                canvas.drawCircle(cx+s*.18f,cy-s*.08f,s*.48f,bg)
            }
            "steel" -> drawPolygon(canvas,cx,cy,s*.62f,6,0f,paint, true)
            "fairy" -> drawStar(canvas,cx,cy,s*.66f,s*.24f,4,paint)
            "grass" -> {
                val p=Path(); p.moveTo(cx+s*.62f,cy-s*.62f); p.cubicTo(cx-s*.52f,cy-s*.47f,cx-s*.62f,cy+s*.42f,cx-s*.08f,cy+s*.55f); p.cubicTo(cx+s*.46f,cy+s*.36f,cx+s*.56f,cy-.15f,cx+s*.62f,cy-s*.62f); p.close(); canvas.drawPath(p,paint); canvas.drawLine(cx-s*.38f,cy+s*.38f,cx+s*.35f,cy-s*.32f,stroke)
            }
            "poison" -> {
                canvas.drawCircle(cx,cy-s*.12f,s*.43f,paint); val bg=Paint(paint).apply{color=withAlpha(typeColor(type),paint.alpha)}; canvas.drawCircle(cx,cy-s*.08f,s*.22f,bg); canvas.drawCircle(cx-s*.35f,cy+s*.45f,s*.12f,paint); canvas.drawCircle(cx,cy+s*.50f,s*.12f,paint); canvas.drawCircle(cx+s*.35f,cy+s*.45f,s*.12f,paint)
            }
            "bug" -> {
                canvas.drawOval(RectF(cx-s*.38f,cy-s*.35f,cx+s*.38f,cy+s*.58f),paint); canvas.drawCircle(cx,cy-s*.40f,s*.28f,paint); canvas.drawLine(cx-s*.14f,cy-s*.62f,cx-s*.38f,cy-s*.78f,stroke); canvas.drawLine(cx+s*.14f,cy-s*.62f,cx+s*.38f,cy-s*.78f,stroke)
            }
            "ghost" -> {
                val p=Path(); p.moveTo(cx-s*.50f,cy+s*.55f); p.lineTo(cx-s*.50f,cy-s*.05f); p.cubicTo(cx-s*.48f,cy-s*.62f,cx+s*.48f,cy-s*.62f,cx+s*.50f,cy-s*.05f); p.lineTo(cx+s*.50f,cy+s*.55f); p.lineTo(cx+s*.20f,cy+s*.35f); p.lineTo(cx,cy+s*.55f); p.lineTo(cx-s*.20f,cy+s*.35f); p.close(); canvas.drawPath(p,paint)
            }
            "dragon" -> {
                val p=Path(); p.moveTo(cx-s*.58f,cy-s*.54f); p.lineTo(cx-s*.12f,cy-s*.26f); p.lineTo(cx+s*.38f,cy-s*.58f); p.lineTo(cx+s*.18f,cy-s*.08f); p.lineTo(cx+s*.58f,cy+s*.10f); p.lineTo(cx+s*.12f,cy+s*.62f); p.lineTo(cx-s*.46f,cy+s*.22f); p.lineTo(cx-s*.08f,cy+s*.08f); p.close(); canvas.drawPath(p,paint)
            }
            "normal" -> { canvas.drawCircle(cx,cy,s*.48f,stroke); canvas.drawCircle(cx,cy,s*.20f,stroke) }
            else -> {
                paint.textAlign = Paint.Align.CENTER; paint.textSize = s*1.15f; paint.typeface=Typeface.DEFAULT_BOLD
                canvas.drawText(t.take(1).uppercase().ifBlank { "?" },cx,cy+s*.38f,paint)
            }
        }
        stroke.strokeCap = Paint.Cap.BUTT
        stroke.color = Color.WHITE
    }

    private fun drawSnowflake(canvas: Canvas,cx:Float,cy:Float,s:Float){
        repeat(3){i-> val a=Math.PI*i/3.0; val dx=(cos(a)*s*.62).toFloat(); val dy=(sin(a)*s*.62).toFloat(); canvas.drawLine(cx-dx,cy-dy,cx+dx,cy+dy,stroke) }
    }

    private fun drawFist(canvas: Canvas,cx:Float,cy:Float,s:Float){
        val fingerW=s*.21f; val top=cy-s*.48f
        for(i in 0..3){ val l=cx-s*.44f+i*fingerW; canvas.drawRoundRect(RectF(l,top,l+fingerW*.88f,cy-s*.03f),s*.08f,s*.08f,paint) }
        canvas.drawRoundRect(RectF(cx-s*.43f,cy-s*.08f,cx+s*.45f,cy+s*.48f),s*.12f,s*.12f,paint)
        canvas.drawRoundRect(RectF(cx-s*.60f,cy+s*.02f,cx-s*.22f,cy+s*.34f),s*.10f,s*.10f,paint)
    }

    private fun drawPolygon(canvas: Canvas,cx:Float,cy:Float,r:Float,n:Int,rotation:Float,p:Paint,hollow:Boolean=false){
        val path=Path(); repeat(n){i-> val a=rotation+(Math.PI*2*i/n).toFloat(); val x=cx+cos(a.toDouble()).toFloat()*r; val y=cy+sin(a.toDouble()).toFloat()*r; if(i==0)path.moveTo(x,y) else path.lineTo(x,y) }; path.close(); if(hollow){ canvas.drawPath(path,stroke); canvas.drawCircle(cx,cy,r*.24f,paint) } else canvas.drawPath(path,p)
    }

    private fun drawStar(canvas: Canvas,cx:Float,cy:Float,outer:Float,inner:Float,points:Int,p:Paint){
        val path=Path(); repeat(points*2){i-> val r=if(i%2==0)outer else inner; val a=(-Math.PI/2 + Math.PI*i/points).toFloat(); val x=cx+cos(a.toDouble()).toFloat()*r; val y=cy+sin(a.toDouble()).toFloat()*r; if(i==0)path.moveTo(x,y) else path.lineTo(x,y) }; path.close(); canvas.drawPath(path,p)
    }

    private fun shortMoveName(name: String): String = if (name.length <= 19) name else name.take(18) + "…"
    private fun withAlpha(c: Int, a: Int): Int = Color.argb(a.coerceIn(0,255), Color.red(c), Color.green(c), Color.blue(c))

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
