package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.view.View
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.GameDataRepository
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.MoveDef
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.engine.TypeChart
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/** Canonical PvPPokeGo HUD anchored around Pokemon GO's native battle layout. */
class BattleOverlayView(
    context: Context,
    private val settings: SettingsRepository,
    private val gameRepo: GameDataRepository,
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
    private var editTarget: String? = null

    fun render(newState: BattleUiState) {
        state = newState
        scaleFactor = settings.scale
        opacityFactor = settings.opacity
        invalidate()
    }

    fun setCollapsed(value: Boolean) { collapsed = value; invalidate() }
    fun isCollapsed(): Boolean = collapsed
    fun showMoveMenu(kind: MoveKind, options: List<MoveDef>) = Unit
    fun dismissMenu() = Unit

    private fun sx(): Float = width / 864f
    private fun sy(): Float = height / 1536f
    private fun u(): Float = min(sx(), sy()) * (scaleFactor / .8f).coerceIn(.62f, 1.5f)
    private fun rx(v: Float): Float = v * sx()
    private fun ry(v: Float): Float = v * sy()
    private fun refX(px: Float): Float = if (sx() > 0f) px / sx() else px
    private fun refY(px: Float): Float = if (sy() > 0f) px / sy() else px

    private fun pos(block: String, defaultX: Float, defaultY: Float): Pair<Float, Float> =
        settings.getPosition(block, defaultX, defaultY)

    /** Returns the draggable HUD block under a screen coordinate while edit mode is active. */
    fun dragTargetAt(screenX: Float, screenY: Float): String? {
        if (!settings.editMode || collapsed || width <= 0 || height <= 0) return null
        val x = refX(screenX)
        val y = refY(screenY)

        fun near(block: String, dx: Float, dy: Float, halfW: Float, halfH: Float): Boolean {
            val (cx, cy) = pos(block, dx, dy)
            return x in (cx-halfW)..(cx+halfW) && y in (cy-halfH)..(cy+halfH)
        }

        if (near(BLOCK_HUD_TOGGLE, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y, 32f, 32f)) return BLOCK_HUD_TOGGLE
        if (near(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y, 34f, 30f)) return BLOCK_PLAYER_MATCHUP

        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isNotEmpty()) {
            val (right, cy) = pos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X, TOP_ICON_Y)
            val left = right - 35f * (strong.size - 1).coerceAtLeast(0) - 28f
            if (x in left..(right + 48f) && y in (cy-30f)..(cy+30f)) return BLOCK_STRONG_TYPES
        }

        val (mx, my) = pos(BLOCK_ENEMY_MOVES, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        if (x in (mx-150f)..(mx+150f) && y in (my-55f)..(my+205f)) return BLOCK_ENEMY_MOVES

        val (rx, ry) = pos(BLOCK_RESERVES, RESERVE_INDICATOR_X, RESERVE_FIRST_Y)
        if (x in (rx-65f)..(rx+175f) && y in (ry-55f)..(ry+255f)) return BLOCK_RESERVES
        return null
    }

    fun setEditTarget(block: String?) {
        editTarget = block
        invalidate()
    }

    /** Persists a dragged position in normalized 864x1536 coordinates. */
    fun moveDragTarget(block: String, screenX: Float, screenY: Float) {
        if (!settings.editMode || width <= 0 || height <= 0) return
        val x = refX(screenX)
        val y = refY(screenY)
        val safe = safeDragPosition(block, x, y)
        settings.setPosition(block, safe.first, safe.second)
        editTarget = block
        invalidate()
    }

    private fun safeDragPosition(block: String, x: Float, y: Float): Pair<Float, Float> = when (block) {
        // Top blocks remain in the free strip between status bar and native white cards.
        BLOCK_PLAYER_MATCHUP -> x.coerceIn(124f, 352f) to y.coerceIn(60f, 88f)
        BLOCK_STRONG_TYPES -> x.coerceIn(520f, 688f) to y.coerceIn(60f, 88f)
        BLOCK_HUD_TOGGLE -> x.coerceIn(382f, 482f) to y.coerceIn(60f, 88f)
        // Main move cluster can be moved broadly, while native HUD clipping remains authoritative.
        BLOCK_ENEMY_MOVES -> x.coerceIn(330f, 680f) to y.coerceIn(250f, 520f)
        // Reserve annotations move as one group and remain near the right-side reserve lane.
        BLOCK_RESERVES -> x.coerceIn(620f, 710f) to y.coerceIn(820f, 1010f)
        else -> x.coerceIn(24f, 840f) to y.coerceIn(60f, 1470f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hudAlpha = (255f * opacityFactor).toInt().coerceIn(76, 255)
        paint.alpha = 255
        stroke.alpha = 255
        stroke.color = hudColor(Color.WHITE)

        // Mandatory rule: PvPPokeGo may only use free regions around Pokemon GO's native HUD.
        // The clean battle screenshots supplied by the project are the canonical reference.
        val nativeSafeSave = canvas.save()
        clipOutNativePokemonGoUi(canvas)

        if (collapsed) {
            drawVisibilityToggle(canvas, true)
            canvas.restoreToCount(nativeSafeSave)
            return
        }

        if (settings.showCurrentIndicator) {
            val (x, y) = pos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y)
            drawMatchupIndicator(canvas, rx(x), ry(y), state.playerMatchup, 14f*u())
        }
        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (state.chargedIncoming && settings.showEnemyMoves) drawShieldAdvice(canvas)
        if (settings.analyzeReserves) drawReserves(canvas)
        if (settings.showSwitchTimer) drawTimers(canvas)
        if (settings.editMode) {
            drawResetAndCorrection(canvas)
            drawEditModeGuide(canvas)
        }
        if (settings.debugMode) drawDebug(canvas)
        drawVisibilityToggle(canvas, false)

        canvas.restoreToCount(nativeSafeSave)
    }

    @Suppress("DEPRECATION")
    private fun clipOutNativePokemonGoUi(canvas: Canvas) {
        val protected = listOf(
            RectF(0f, 0f, width.toFloat(), ry(52f)),
            RectF(rx(0f), ry(52f), rx(112f), ry(92f)),
            RectF(rx(744f), ry(52f), width.toFloat(), ry(92f)),
            RectF(rx(14f), ry(93f), rx(354f), ry(176f)),
            RectF(rx(510f), ry(93f), rx(852f), ry(176f)),
            RectF(rx(12f), ry(180f), rx(116f), ry(286f)),
            RectF(rx(35f), ry(620f), rx(304f), ry(704f)),
            RectF(rx(425f), ry(620f), rx(724f), ry(704f)),
            RectF(rx(716f), ry(846f), width.toFloat(), ry(988f)),
            RectF(rx(716f), ry(996f), width.toFloat(), ry(1122f)),
            RectF(rx(78f), ry(1260f), rx(786f), height.toFloat())
        )
        protected.forEach { canvas.clipRect(it, Region.Op.DIFFERENCE) }
    }

    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
        val (refCx, refCy) = pos(BLOCK_HUD_TOGGLE, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y)
        val cx = rx(refCx); val cy = ry(refCy); val radius = 16f*u()
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(72,76,82), .88f)
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 1.5f*u()
        stroke.color = hudColor(Color.WHITE, .68f)
        canvas.drawCircle(cx, cy, radius, stroke)
        val eye = RectF(cx-8.2f*u(), cy-4.7f*u(), cx+8.2f*u(), cy+4.7f*u())
        stroke.color = hudColor(Color.WHITE); stroke.strokeWidth = 1.8f*u()
        canvas.drawOval(eye, stroke)
        paint.color = hudColor(Color.WHITE); canvas.drawCircle(cx,cy,2.6f*u(),paint)
        if (!hidden) {
            stroke.strokeWidth = 2.1f*u()
            canvas.drawLine(cx-9f*u(),cy+7.7f*u(),cx+9f*u(),cy-7.7f*u(),stroke)
        }
        drawEditSelection(canvas, BLOCK_HUD_TOGGLE, cx, cy, 25f*u(), 25f*u())
    }

    private fun drawMatchupIndicator(canvas: Canvas, cx: Float, cy: Float, matchup: MatchupState, radius: Float = 17f*u()) {
        paint.style = Paint.Style.FILL
        paint.color = when (matchup) {
            MatchupState.FAVORABLE -> hudColor(Color.rgb(20,190,80))
            MatchupState.UNFAVORABLE -> hudColor(Color.rgb(225,45,55))
            MatchupState.NEUTRAL -> hudColor(Color.rgb(145,145,145))
            MatchupState.UNKNOWN -> hudColor(Color.rgb(105,105,105))
        }
        canvas.drawCircle(cx,cy,radius,paint)
        stroke.color = hudColor(Color.WHITE); stroke.strokeWidth = 2f*u(); canvas.drawCircle(cx,cy,radius,stroke)
        paint.color = hudColor(Color.WHITE); paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.DEFAULT_BOLD
        when (matchup) {
            MatchupState.FAVORABLE -> { paint.textSize=19f*u(); canvas.drawText("✓",cx,cy+6f*u(),paint) }
            MatchupState.UNFAVORABLE -> { paint.textSize=20f*u(); canvas.drawText("×",cx,cy+6.3f*u(),paint) }
            MatchupState.NEUTRAL -> canvas.drawCircle(cx,cy,5.6f*u(),paint)
            MatchupState.UNKNOWN -> { paint.textSize=13f*u(); canvas.drawText("?",cx,cy+4.5f*u(),paint) }
        }
        if (settings.editMode) {
            val (mx, my) = pos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y)
            if (kotlin.math.abs(cx-rx(mx)) < 2f && kotlin.math.abs(cy-ry(my)) < 2f) {
                drawEditSelection(canvas, BLOCK_PLAYER_MATCHUP, cx, cy, 24f*u(), 24f*u())
            }
        }
    }

    private fun drawStrongTypes(canvas: Canvas) {
        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isEmpty()) return
        val radius = 14f*u()
        val (rightRef, yRef) = pos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X, TOP_ICON_Y)
        val y = ry(yRef)
        val spacing = rx(35f)
        val strongRight = rx(rightRef)
        val arrowX = strongRight + rx(36f)
        val firstStrong = strongRight - spacing*(strong.size-1).coerceAtLeast(0)
        strong.forEachIndexed { i,type -> drawTypeChip(canvas,firstStrong+spacing*i,y,type,radius) }
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=18f*u(); paint.typeface=Typeface.DEFAULT_BOLD
        paint.setShadowLayer(2.5f,0f,1f,Color.BLACK); canvas.drawText("›",arrowX,y+5.8f*u(),paint); paint.clearShadowLayer()
        val centerX = (firstStrong + strongRight) / 2f
        val halfW = (strongRight-firstStrong)/2f + 27f*u()
        drawEditSelection(canvas, BLOCK_STRONG_TYPES, centerX, y, halfW, 24f*u())
    }

    private fun drawMoveTriangle(canvas: Canvas) {
        val (centerRefX, fastRefY) = pos(BLOCK_ENEMY_MOVES, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        val centerX=rx(centerRefX); val fastY=ry(fastRefY); val chargedY=ry(fastRefY+123f)
        val spread=rx(72f); val fastRadius=31f*u(); val chargedRadius=38f*u()
        state.fastMove?.let { m ->
            drawMoveCircle(canvas,centerX,fastY,fastRadius,m)
            drawMoveLabel(canvas,centerX,fastY+fastRadius+20f*u(),shortMoveName(m.name)+" ▼")
        }
        state.charged1?.let { p ->
            val cx=centerX-spread
            drawCharged(canvas,cx,chargedY,chargedRadius,p)
            val labelY=chargedY+chargedRadius+22f*u()
            drawMoveLabel(canvas,cx,labelY,shortMoveName(p.move.name)+" ▼")
            drawEnemySuperEffectiveText(canvas,cx,labelY+17f*u(),p.move)
        }
        state.charged2?.let { p ->
            val cx=centerX+spread
            drawCharged(canvas,cx,chargedY,chargedRadius,p)
            val labelY=chargedY+chargedRadius+22f*u()
            drawMoveLabel(canvas,cx,labelY,shortMoveName(p.move.name)+" ▼")
            drawEnemySuperEffectiveText(canvas,cx,labelY+17f*u(),p.move)
        }
        drawEditSelection(canvas, BLOCK_ENEMY_MOVES, centerX, (fastY+chargedY)/2f, 132f*u(), 116f*u())
    }

    private fun drawEnemySuperEffectiveText(canvas: Canvas, x: Float, y: Float, move: MoveDef) {
        val playerTypes = currentPlayerTypes()
        if (playerTypes.isEmpty()) return
        if (TypeChart.multiplier(move.type, playerTypes) <= 1.01) return
        paint.color = hudColor(Color.rgb(255, 153, 45))
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10.8f*u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3.4f,0f,1.3f,Color.BLACK)
        canvas.drawText("SUPEREFICAZ!",x,y,paint)
        paint.clearShadowLayer()
    }

    private fun drawShieldAdvice(canvas: Canvas) {
        val playerTypes = currentPlayerTypes()
        if (playerTypes.isEmpty()) return
        val all = listOfNotNull(state.charged1, state.charged2)
        if (all.isEmpty()) return
        val plausible = all.filter { it.ready || it.progress >= .72f }.ifEmpty { all }
        val threat = plausible.maxByOrNull { p -> p.move.power.toDouble() * TypeChart.multiplier(p.move.type, playerTypes) } ?: return
        val eff = TypeChart.multiplier(threat.move.type, playerTypes)
        val threatScore = threat.move.power.toDouble() * eff
        val advice = when {
            eff >= 1.5 || threatScore >= 100.0 -> ShieldAdvice("USAR ESCUDO", "${shortMoveName(threat.move.name)} • ${if (eff > 1.01) "SUPEREFICAZ • " else ""}DANO ALTO", Color.rgb(232,61,72))
            eff <= .65 && threatScore < 70.0 -> ShieldAdvice("PODE NÃO USAR", "${shortMoveName(threat.move.name)} • POUCO EFICAZ • DANO BAIXO", Color.rgb(46,188,105))
            threatScore >= 70.0 -> ShieldAdvice("ESCUDO OPCIONAL", "${shortMoveName(threat.move.name)} • DANO MÉDIO", Color.rgb(245,175,52))
            else -> ShieldAdvice("PODE NÃO USAR", "${shortMoveName(threat.move.name)} • DANO BAIXO", Color.rgb(46,188,105))
        }
        val (centerRefX, fastRefY) = pos(BLOCK_ENEMY_MOVES, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        val centerX=rx(centerRefX); val centerY=ry(fastRefY+237f); val halfW=142f*u(); val halfH=31f*u()
        val box=RectF(centerX-halfW,centerY-halfH,centerX+halfW,centerY+halfH)
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.rgb(22,27,34),.90f); canvas.drawRoundRect(box,16f*u(),16f*u(),paint)
        stroke.style=Paint.Style.STROKE; stroke.strokeWidth=2.4f*u(); stroke.color=hudColor(advice.color); canvas.drawRoundRect(box,16f*u(),16f*u(),stroke)
        paint.textAlign=Paint.Align.CENTER; paint.typeface=Typeface.DEFAULT_BOLD; paint.color=hudColor(advice.color); paint.textSize=16f*u(); paint.setShadowLayer(2f,0f,1f,Color.BLACK)
        canvas.drawText(advice.title,centerX,centerY-4f*u(),paint)
        paint.color=hudColor(Color.WHITE); paint.textSize=10.5f*u(); canvas.drawText(advice.detail,centerX,centerY+16f*u(),paint); paint.clearShadowLayer()
    }

    private fun drawCharged(canvas: Canvas,cx: Float,cy: Float,radius: Float,prediction: ChargedPrediction) {
        val circle=Path().apply { addCircle(cx,cy,radius,Path.Direction.CW) }
        canvas.save(); canvas.clipPath(circle)
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.rgb(49,55,69)); canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        if (settings.showEnergyProgress) {
            val progress=prediction.progress.coerceIn(0f,1f); val fillTop=cy+radius-2f*radius*progress
            paint.color=hudColor(typeColor(prediction.move.type)); canvas.drawRect(cx-radius,fillTop,cx+radius,cy+radius,paint)
            if (progress>.02f && progress<.98f) { paint.color=hudColor(Color.WHITE,.84f); canvas.drawRect(cx-radius,fillTop-1f*u(),cx+radius,fillTop+1f*u(),paint) }
        } else {
            paint.color=hudColor(typeColor(prediction.move.type)); canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        }
        canvas.restore()
        stroke.color=hudColor(Color.WHITE); stroke.strokeWidth=2.2f*u(); canvas.drawCircle(cx,cy,radius,stroke)
        drawTypeGlyph(canvas,cx,cy,prediction.move.type,23f*u())
        if (settings.showChargedCounter) drawChargeProgressDots(canvas,cx,cy,radius,prediction)
    }

    private fun drawChargeProgressDots(canvas: Canvas,cx: Float,cy: Float,radius: Float,prediction: ChargedPrediction) {
        val total=5
        val progress=if(prediction.ready) prediction.overflowProgress.coerceIn(0f,1f) else prediction.progress.coerceIn(0f,1f)
        val filled=if(prediction.ready){ if(progress>=.999f) total else floor(progress*total).toInt().coerceIn(0,total) } else { if(progress<=0f) 0 else ceil(progress*total).toInt().coerceIn(0,total) }
        val dotRadius=2.5f*u(); val gap=7.5f*u(); val startX=cx-gap*(total-1)/2f; val y=cy+radius*.72f
        repeat(total){i-> paint.style=Paint.Style.FILL; paint.color=if(i<filled) hudColor(Color.WHITE) else hudColor(Color.rgb(34,37,44),.92f); canvas.drawCircle(startX+i*gap,y,dotRadius,paint)}
    }

    private fun drawMoveCircle(canvas: Canvas,cx: Float,cy: Float,radius: Float,move: MoveDef) {
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.rgb(35,40,52),.72f); canvas.drawCircle(cx,cy+2f*u(),radius+3f*u(),paint)
        paint.color=hudColor(typeColor(move.type)); canvas.drawCircle(cx,cy,radius,paint)
        stroke.color=hudColor(Color.WHITE); stroke.strokeWidth=2.2f*u(); canvas.drawCircle(cx,cy,radius,stroke); drawTypeGlyph(canvas,cx,cy,move.type,20f*u())
    }

    private fun drawMoveLabel(canvas: Canvas,x: Float,y: Float,text: String) {
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=13.5f*u(); paint.typeface=Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3f,0f,1f,Color.BLACK); canvas.drawText(text,x,y,paint); paint.clearShadowLayer()
    }

    private fun drawReserves(canvas: Canvas) {
        val (indicatorRefX, firstRefY) = pos(BLOCK_RESERVES, RESERVE_INDICATOR_X, RESERVE_FIRST_Y)
        val indicatorX=rx(indicatorRefX); val firstY=ry(firstRefY); val secondY=ry(firstRefY+140f); val typeYDelta=ry(82f)
        state.reserves.take(2).forEachIndexed { index,reserve ->
            if (!reserve.visible) return@forEachIndexed
            val cardCenterY=if(index==0) firstY else secondY
            drawMatchupIndicator(canvas,indicatorX,cardCenterY,reserve.matchup,13.5f*u())
            if(settings.showReserveTypes && reserve.types.isNotEmpty()){
                val typeRadius=12f*u(); val spacing=rx(31f); val centerX=rx(indicatorRefX+98f)
                var x=centerX-if(reserve.types.size>1) spacing/2f else 0f
                reserve.types.take(2).forEach{type->drawTypeChip(canvas,x,cardCenterY+typeYDelta,type,typeRadius);x+=spacing}
            }
        }
        drawEditSelection(canvas, BLOCK_RESERVES, indicatorX+rx(49f), (firstY+secondY)/2f, 112f*u(), 132f*u())
    }

    private fun drawTimers(canvas: Canvas) {
        paint.textAlign=Paint.Align.LEFT; paint.textSize=12f*u(); paint.typeface=Typeface.DEFAULT_BOLD; paint.color=hudColor(Color.WHITE)
        state.ownSwitchSeconds?.let{canvas.drawText("↻ $it",10f*u(),height*.47f,paint)}
        state.opponentSwitchSeconds?.let{canvas.drawText("↻ $it",width-58f*u(),height*.47f,paint)}
    }

    private fun drawResetAndCorrection(canvas: Canvas) {
        val y=ry(274f); val radius=15f*u(); val resetX=rx(22f)
        paint.color=hudColor(Color.rgb(45,45,45)); canvas.drawCircle(resetX,y,radius,paint); stroke.color=hudColor(Color.WHITE); canvas.drawCircle(resetX,y,radius,stroke)
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=16f*u(); canvas.drawText("↻",resetX,y+5f*u(),paint)
        val correctionY=y+40f*u(); listOf(resetX to "−",(resetX+38f*u()) to "+").forEach{(x,g)->paint.color=hudColor(Color.rgb(45,45,45));canvas.drawCircle(x,correctionY,radius,paint);canvas.drawCircle(x,correctionY,radius,stroke);paint.color=hudColor(Color.WHITE);canvas.drawText(g,x,correctionY+5f*u(),paint)}
    }

    private fun drawEditModeGuide(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(18,22,28), .72f)
        val cx = rx(432f); val cy = ry(302f); val box = RectF(cx-rx(145f),cy-ry(18f),cx+rx(145f),cy+ry(18f))
        canvas.drawRoundRect(box,12f*u(),12f*u(),paint)
        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11.5f*u()
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("MODO EDIÇÃO • SEGURE E ARRASTE",cx,cy+4f*u(),paint)
    }

    private fun drawEditSelection(canvas: Canvas, block: String, cx: Float, cy: Float, halfW: Float, halfH: Float) {
        if (!settings.editMode || editTarget != block) return
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 1.7f*u()
        stroke.color = hudColor(Color.rgb(255,205,60), .95f)
        canvas.drawRoundRect(RectF(cx-halfW,cy-halfH,cx+halfW,cy+halfH),8f*u(),8f*u(),stroke)
    }

    private fun drawDebug(canvas: Canvas) {
        val text=state.debugText?:return; paint.color=hudColor(Color.BLACK,.78f); val box=RectF(8f,height-166f*u(),width*.82f,height-8f); canvas.drawRoundRect(box,8f,8f,paint)
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.LEFT; paint.textSize=10f*u(); text.lines().take(9).forEachIndexed{i,line->canvas.drawText(line,box.left+8f,box.top+(i+1)*14f*u(),paint)}
    }

    private fun drawTypeChip(canvas: Canvas,cx: Float,cy: Float,type: String,radius: Float) {
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.rgb(29,34,45),.62f); canvas.drawCircle(cx,cy+1.5f*u(),radius+2.2f*u(),paint)
        paint.color=hudColor(typeColor(type)); canvas.drawCircle(cx,cy,radius,paint); stroke.color=hudColor(Color.WHITE,.92f); stroke.strokeWidth=1.35f*u(); canvas.drawCircle(cx,cy,radius,stroke); drawTypeGlyph(canvas,cx,cy,type,radius*.95f); stroke.color=hudColor(Color.WHITE)
    }

    private fun drawTypeGlyph(canvas: Canvas,cx: Float,cy: Float,type: String,size: Float) {
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.WHITE); val path=TypeGlyphPaths.build(type,cx,cy,size*1.18f)
        if(path!=null){canvas.drawPath(path,paint);return}; paint.textAlign=Paint.Align.CENTER; paint.textSize=size*1.05f; paint.typeface=Typeface.DEFAULT_BOLD; canvas.drawText(type.take(1).uppercase().ifBlank{"?"},cx,cy+size*.35f,paint)
    }

    private fun currentPlayerTypes(): List<String> = gameRepo.pokemon(state.playerName)?.types.orEmpty()
    private data class ShieldAdvice(val title:String,val detail:String,val color:Int)
    private fun shortMoveName(name:String)=if(name.length<=19) name else name.take(18)+"…"
    private fun hudColor(color:Int,multiplier:Float=1f):Int{val a=(hudAlpha*multiplier).toInt().coerceIn(0,255);return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color))}
    private fun typeColor(type:String):Int=when(type.lowercase()){
        "fairy"->Color.rgb(239,153,230);"psychic"->Color.rgb(250,137,131);"fighting"->Color.rgb(219,66,86);"rock"->Color.rgb(206,193,141);"fire"->Color.rgb(251,165,75);"steel"->Color.rgb(85,151,164);"water"->Color.rgb(91,166,224);"flying"->Color.rgb(155,180,229);"ghost"->Color.rgb(99,111,191);"grass"->Color.rgb(93,190,101);"bug"->Color.rgb(161,194,49);"ground"->Color.rgb(214,133,85);"ice"->Color.rgb(125,212,200);"dark"->Color.rgb(113,121,140);"dragon"->Color.rgb(7,115,200);"normal"->Color.rgb(153,156,161);"electric"->Color.rgb(243,219,85);"poison"->Color.rgb(178,98,204);else->Color.rgb(153,156,161)
    }

    companion object {
        const val BLOCK_PLAYER_MATCHUP = "hud_player_matchup"
        const val BLOCK_STRONG_TYPES = "hud_strong_types"
        const val BLOCK_ENEMY_MOVES = "hud_enemy_moves"
        const val BLOCK_RESERVES = "hud_reserves"
        const val BLOCK_HUD_TOGGLE = "hud_toggle"

        const val TOP_ICON_Y=72f
        const val PLAYER_MATCHUP_X=136f
        const val STRONG_TYPES_RIGHT_X=688f
        const val HUD_TOGGLE_CENTER_X=432f
        const val HUD_TOGGLE_CENTER_Y=72f
        const val ENEMY_MOVE_CENTER_X=558f
        const val ENEMY_MOVE_FAST_Y=355f
        const val RESERVE_INDICATOR_X=692f
        const val RESERVE_FIRST_Y=918f
    }
}
