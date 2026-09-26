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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hudAlpha = (255f * opacityFactor).toInt().coerceIn(76, 255)
        paint.alpha = 255
        stroke.alpha = 255
        stroke.color = hudColor(Color.WHITE)

        if (collapsed) {
            drawVisibilityToggle(canvas, true)
            return
        }

        if (settings.showCurrentIndicator) {
            // Keep our current-Pokemon indicator beside, never over, GO's native portrait.
            drawMatchupIndicator(canvas, rx(PLAYER_MATCHUP_X), ry(TOP_ICON_Y), state.playerMatchup, 17.5f*u())
        }
        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (state.chargedIncoming && settings.showEnemyMoves) drawShieldAdvice(canvas)
        if (settings.analyzeReserves) drawReserves(canvas)
        if (settings.showSwitchTimer) drawTimers(canvas)
        if (settings.editMode) drawResetAndCorrection(canvas)
        if (settings.debugMode) drawDebug(canvas)
        drawVisibilityToggle(canvas, false)
    }

    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
        val cx = rx(HUD_TOGGLE_CENTER_X); val cy = ry(HUD_TOGGLE_CENTER_Y); val radius = 19f*u()
        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(72,76,82), .88f)
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 1.5f*u()
        stroke.color = hudColor(Color.WHITE, .68f)
        canvas.drawCircle(cx, cy, radius, stroke)
        val eye = RectF(cx-9.8f*u(), cy-5.6f*u(), cx+9.8f*u(), cy+5.6f*u())
        stroke.color = hudColor(Color.WHITE); stroke.strokeWidth = 2.0f*u()
        canvas.drawOval(eye, stroke)
        paint.color = hudColor(Color.WHITE); canvas.drawCircle(cx,cy,3f*u(),paint)
        if (!hidden) {
            stroke.strokeWidth = 2.4f*u()
            canvas.drawLine(cx-10.5f*u(),cy+9f*u(),cx+10.5f*u(),cy-9f*u(),stroke)
        }
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
    }

    private fun drawStrongTypes(canvas: Canvas) {
        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isEmpty()) return
        val radius = 16.5f*u(); val y = ry(TOP_ICON_Y); val spacing = rx(39f)
        val strongRight = rx(712f); val arrowX = rx(750f)
        val firstStrong = strongRight - spacing*(strong.size-1).coerceAtLeast(0)
        strong.forEachIndexed { i,type -> drawTypeChip(canvas,firstStrong+spacing*i,y,type,radius) }
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=21f*u(); paint.typeface=Typeface.DEFAULT_BOLD
        paint.setShadowLayer(2.5f,0f,1f,Color.BLACK); canvas.drawText("›",arrowX,y+6.5f*u(),paint); paint.clearShadowLayer()
    }

    private fun drawMoveTriangle(canvas: Canvas) {
        val centerX=rx(ENEMY_MOVE_CENTER_X); val fastY=ry(355f); val chargedY=ry(478f)
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
    }

    /** Mirrors GO's compact SUPEREFICAZ! feedback under the dangerous enemy Charged Move. */
    private fun drawEnemySuperEffectiveText(canvas: Canvas, x: Float, y: Float, move: MoveDef) {
        val playerTypes = currentPlayerTypes()
        if (playerTypes.isEmpty()) return
        if (TypeChart.multiplier(move.type, playerTypes) <= 1.01) return
        paint.color = hudColor(Color.WHITE)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10.8f*u()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3.4f,0f,1.3f,Color.BLACK)
        canvas.drawText("SUPEREFICAZ!",x,y,paint)
        paint.clearShadowLayer()
    }

    /**
     * Qualitative shield alert. Exact HP percentage is deliberately not invented because the
     * overlay does not know exact live IV/level/defense; it uses real type effectiveness plus
     * PvP move power and the enemy's current energy prediction.
     */
    private fun drawShieldAdvice(canvas: Canvas) {
        val playerTypes = currentPlayerTypes()
        if (playerTypes.isEmpty()) return

        val all = listOfNotNull(state.charged1, state.charged2)
        if (all.isEmpty()) return
        val plausible = all.filter { it.ready || it.progress >= .72f }.ifEmpty { all }
        val threat = plausible.maxByOrNull { p ->
            p.move.power.toDouble() * TypeChart.multiplier(p.move.type, playerTypes)
        } ?: return

        val eff = TypeChart.multiplier(threat.move.type, playerTypes)
        val threatScore = threat.move.power.toDouble() * eff
        val advice = when {
            eff >= 1.5 || threatScore >= 100.0 -> ShieldAdvice(
                title = "USAR ESCUDO",
                detail = "${shortMoveName(threat.move.name)} • ${if (eff > 1.01) "SUPEREFICAZ • " else ""}DANO ALTO",
                color = Color.rgb(232, 61, 72)
            )
            eff <= .65 && threatScore < 70.0 -> ShieldAdvice(
                title = "PODE NÃO USAR",
                detail = "${shortMoveName(threat.move.name)} • POUCO EFICAZ • DANO BAIXO",
                color = Color.rgb(46, 188, 105)
            )
            threatScore >= 70.0 -> ShieldAdvice(
                title = "ESCUDO OPCIONAL",
                detail = "${shortMoveName(threat.move.name)} • DANO MÉDIO",
                color = Color.rgb(245, 175, 52)
            )
            else -> ShieldAdvice(
                title = "PODE NÃO USAR",
                detail = "${shortMoveName(threat.move.name)} • DANO BAIXO",
                color = Color.rgb(46, 188, 105)
            )
        }

        val centerX = rx(ENEMY_MOVE_CENTER_X)
        val centerY = ry(592f)
        val halfW = 142f*u()
        val halfH = 31f*u()
        val box = RectF(centerX-halfW, centerY-halfH, centerX+halfW, centerY+halfH)

        paint.style = Paint.Style.FILL
        paint.color = hudColor(Color.rgb(22, 27, 34), .90f)
        canvas.drawRoundRect(box, 16f*u(), 16f*u(), paint)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 2.4f*u()
        stroke.color = hudColor(advice.color)
        canvas.drawRoundRect(box, 16f*u(), 16f*u(), stroke)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = hudColor(advice.color)
        paint.textSize = 16f*u()
        paint.setShadowLayer(2f,0f,1f,Color.BLACK)
        canvas.drawText(advice.title, centerX, centerY-4f*u(), paint)

        paint.color = hudColor(Color.WHITE)
        paint.textSize = 10.5f*u()
        canvas.drawText(advice.detail, centerX, centerY+16f*u(), paint)
        paint.clearShadowLayer()
    }

    private fun drawCharged(canvas: Canvas,cx: Float,cy: Float,radius: Float,prediction: ChargedPrediction) {
        val circle=Path().apply { addCircle(cx,cy,radius,Path.Direction.CW) }
        canvas.save(); canvas.clipPath(circle)
        paint.style=Paint.Style.FILL; paint.color=hudColor(Color.rgb(49,55,69)); canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        if (settings.showEnergyProgress) {
            val progress=prediction.progress.coerceIn(0f,1f); val fillTop=cy+radius-2f*radius*progress
            paint.color=hudColor(typeColor(prediction.move.type)); canvas.drawRect(cx-radius,fillTop,cx+radius,cy+radius,paint)
            if (progress>.02f && progress<.98f) {
                paint.color=hudColor(Color.WHITE,.84f); canvas.drawRect(cx-radius,fillTop-1f*u(),cx+radius,fillTop+1f*u(),paint)
            }
        } else {
            paint.color=hudColor(typeColor(prediction.move.type)); canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        }
        canvas.restore()
        stroke.color=hudColor(Color.WHITE); stroke.strokeWidth=2.2f*u(); canvas.drawCircle(cx,cy,radius,stroke)
        drawTypeGlyph(canvas,cx,cy,prediction.move.type,23f*u())
        if (settings.showChargedCounter) drawChargeProgressDots(canvas,cx,cy,radius,prediction)
    }

    /**
     * Dots show useful information even when the main circle is already full. Before the first
     * charge is ready they mirror progress to that charge. Once ready, they show banked overflow
     * toward a second copy. That makes a spend visible even when residual energy still leaves the
     * move immediately ready again, exactly matching Pokemon GO's shared-energy behavior.
     */
    private fun drawChargeProgressDots(canvas: Canvas,cx: Float,cy: Float,radius: Float,prediction: ChargedPrediction) {
        val total=5
        val progress = if (prediction.ready) prediction.overflowProgress.coerceIn(0f,1f) else prediction.progress.coerceIn(0f,1f)
        val filled = if (prediction.ready) {
            if (progress >= .999f) total else floor(progress * total).toInt().coerceIn(0,total)
        } else {
            if (progress <= 0f) 0 else ceil(progress * total).toInt().coerceIn(0,total)
        }
        val dotRadius=2.5f*u(); val gap=7.5f*u(); val startX=cx-gap*(total-1)/2f; val y=cy+radius*.72f
        repeat(total) { i ->
            paint.style=Paint.Style.FILL
            paint.color=if(i<filled) hudColor(Color.WHITE) else hudColor(Color.rgb(34,37,44),.92f)
            canvas.drawCircle(startX+i*gap,y,dotRadius,paint)
        }
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
        val indicatorX=rx(827f); val firstY=ry(905f); val secondY=ry(1052f); val typeYDelta=ry(65f)
        state.reserves.take(2).forEachIndexed { index,reserve ->
            if (!reserve.visible) return@forEachIndexed
            val cardCenterY=if(index==0) firstY else secondY
            drawMatchupIndicator(canvas,indicatorX,cardCenterY,reserve.matchup,14.5f*u())
            if (settings.showReserveTypes && reserve.types.isNotEmpty()) {
                val typeRadius=12.5f*u(); val spacing=rx(33f); val centerX=rx(782f)
                var x=centerX-if(reserve.types.size>1) spacing/2f else 0f
                reserve.types.take(2).forEach { type -> drawTypeChip(canvas,x,cardCenterY+typeYDelta,type,typeRadius); x+=spacing }
            }
        }
    }

    private fun drawTimers(canvas: Canvas) {
        paint.textAlign=Paint.Align.LEFT; paint.textSize=12f*u(); paint.typeface=Typeface.DEFAULT_BOLD; paint.color=hudColor(Color.WHITE)
        state.ownSwitchSeconds?.let { canvas.drawText("↻ $it",10f*u(),height*.47f,paint) }
        state.opponentSwitchSeconds?.let { canvas.drawText("↻ $it",width-58f*u(),height*.47f,paint) }
    }

    private fun drawResetAndCorrection(canvas: Canvas) {
        val y=ry(274f); val radius=15f*u(); val resetX=rx(22f)
        paint.color=hudColor(Color.rgb(45,45,45)); canvas.drawCircle(resetX,y,radius,paint); stroke.color=hudColor(Color.WHITE); canvas.drawCircle(resetX,y,radius,stroke)
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=16f*u(); canvas.drawText("↻",resetX,y+5f*u(),paint)
        val correctionY=y+40f*u(); listOf(resetX to "−",(resetX+38f*u()) to "+").forEach { (x,g) ->
            paint.color=hudColor(Color.rgb(45,45,45)); canvas.drawCircle(x,correctionY,radius,paint); canvas.drawCircle(x,correctionY,radius,stroke); paint.color=hudColor(Color.WHITE); canvas.drawText(g,x,correctionY+5f*u(),paint)
        }
    }

    private fun drawDebug(canvas: Canvas) {
        val text=state.debugText?:return; paint.color=hudColor(Color.BLACK,.78f); val box=RectF(8f,height-166f*u(),width*.82f,height-8f); canvas.drawRoundRect(box,8f,8f,paint)
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.LEFT; paint.textSize=10f*u(); text.lines().take(9).forEachIndexed { i,line -> canvas.drawText(line,box.left+8f,box.top+(i+1)*14f*u(),paint) }
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
    private data class ShieldAdvice(val title: String, val detail: String, val color: Int)

    private fun shortMoveName(name:String)=if(name.length<=19) name else name.take(18)+"…"
    private fun hudColor(color:Int,multiplier:Float=1f):Int { val a=(hudAlpha*multiplier).toInt().coerceIn(0,255); return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color)) }
    private fun typeColor(type:String):Int=when(type.lowercase()){
        "fairy"->Color.rgb(239,153,230);"psychic"->Color.rgb(250,137,131);"fighting"->Color.rgb(219,66,86);"rock"->Color.rgb(206,193,141);"fire"->Color.rgb(251,165,75);"steel"->Color.rgb(85,151,164);"water"->Color.rgb(91,166,224);"flying"->Color.rgb(155,180,229);"ghost"->Color.rgb(99,111,191);"grass"->Color.rgb(93,190,101);"bug"->Color.rgb(161,194,49);"ground"->Color.rgb(214,133,85);"ice"->Color.rgb(125,212,200);"dark"->Color.rgb(113,121,140);"dragon"->Color.rgb(7,115,200);"normal"->Color.rgb(153,156,161);"electric"->Color.rgb(243,219,85);"poison"->Color.rgb(178,98,204);else->Color.rgb(153,156,161)
    }

    companion object {
        // Kept comfortably inside the real full-screen overlay so circles are never clipped.
        private const val TOP_ICON_Y=44f
        private const val PLAYER_MATCHUP_X=30f
        const val HUD_TOGGLE_CENTER_X=432f
        const val HUD_TOGGLE_CENTER_Y=44f
        const val ENEMY_MOVE_CENTER_X=558f
    }
}
