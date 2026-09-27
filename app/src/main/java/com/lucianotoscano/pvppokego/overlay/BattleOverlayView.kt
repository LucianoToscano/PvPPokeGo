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
            // Player Pokemon can have 1 or 2 native type icons. Reserve two slots permanently,
            // then draw our matchup chip only after that native lane.
            drawMatchupIndicator(canvas, rx(PLAYER_MATCHUP_X), ry(TOP_ICON_Y), state.playerMatchup, 14f*u())
        }
        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (state.chargedIncoming && settings.showEnemyMoves) drawShieldAdvice(canvas)
        if (settings.analyzeReserves) drawReserves(canvas)
        if (settings.showSwitchTimer) drawTimers(canvas)
        if (settings.editMode) drawResetAndCorrection(canvas)
        if (settings.debugMode) drawDebug(canvas)
        drawVisibilityToggle(canvas, false)

        canvas.restoreToCount(nativeSafeSave)
    }

    @Suppress("DEPRECATION")
    private fun clipOutNativePokemonGoUi(canvas: Canvas) {
        // Normalized 864x1536 coordinates. These are protection zones, not placement guides.
        // They deliberately include safety margins so future overlay changes cannot cover GO UI.
        val protected = listOf(
            RectF(0f, 0f, width.toFloat(), ry(52f)),                       // Android/status bar

            // Native top type/status lanes. ALWAYS reserve room for up to TWO Pokemon types.
            RectF(rx(0f), ry(52f), rx(112f), ry(92f)),                    // player's 1-2 native type icons
            RectF(rx(744f), ry(52f), width.toFloat(), ry(92f)),           // opponent's 1-2 native type icons

            RectF(rx(14f), ry(93f), rx(354f), ry(176f)),                  // player's white name/CP card
            RectF(rx(510f), ry(93f), rx(852f), ry(176f)),                 // opponent white name/CP card
            RectF(rx(12f), ry(180f), rx(116f), ry(286f)),                 // native switch/run button

            RectF(rx(35f), ry(620f), rx(304f), ry(704f)),                 // player's HP/effectiveness
            RectF(rx(425f), ry(620f), rx(724f), ry(704f)),                // opponent HP/effectiveness

            RectF(rx(716f), ry(846f), width.toFloat(), ry(988f)),         // reserve card 1
            RectF(rx(716f), ry(996f), width.toFloat(), ry(1122f)),        // reserve card 2

            RectF(rx(78f), ry(1260f), rx(786f), height.toFloat())         // player's charged move controls
        )
        protected.forEach { canvas.clipRect(it, Region.Op.DIFFERENCE) }
    }

    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
        val cx = rx(HUD_TOGGLE_CENTER_X); val cy = ry(HUD_TOGGLE_CENTER_Y); val radius = 16f*u()
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

        // Enemy Pokemon can also have 1 or 2 native type icons. Our weakness row ends well before
        // that reserved two-icon lane; it can never use the far-right native area.
        val radius = 14f*u()
        val y = ry(TOP_ICON_Y)
        val spacing = rx(35f)
        val strongRight = rx(688f)
        val arrowX = rx(724f)
        val firstStrong = strongRight - spacing*(strong.size-1).coerceAtLeast(0)
        strong.forEachIndexed { i,type -> drawTypeChip(canvas,firstStrong+spacing*i,y,type,radius) }
        paint.color=hudColor(Color.WHITE); paint.textAlign=Paint.Align.CENTER; paint.textSize=18f*u(); paint.typeface=Typeface.DEFAULT_BOLD
        paint.setShadowLayer(2.5f,0f,1f,Color.BLACK); canvas.drawText("›",arrowX,y+5.8f*u(),paint); paint.clearShadowLayer()
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
        val centerX=rx(ENEMY_MOVE_CENTER_X); val centerY=ry(592f); val halfW=142f*u(); val halfH=31f*u()
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
        val indicatorX=rx(692f); val firstY=ry(918f); val secondY=ry(1058f); val typeYDelta=ry(82f)
        state.reserves.take(2).forEachIndexed { index,reserve ->
            if (!reserve.visible) return@forEachIndexed
            val cardCenterY=if(index==0) firstY else secondY
            drawMatchupIndicator(canvas,indicatorX,cardCenterY,reserve.matchup,13.5f*u())
            if(settings.showReserveTypes && reserve.types.isNotEmpty()){
                val typeRadius=12f*u(); val spacing=rx(31f); val centerX=rx(790f)
                var x=centerX-if(reserve.types.size>1) spacing/2f else 0f
                reserve.types.take(2).forEach{type->drawTypeChip(canvas,x,cardCenterY+typeYDelta,type,typeRadius);x+=spacing}
            }
        }
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
        // Canonical free row from the clean 709x1536 battle references.
        private const val TOP_ICON_Y=72f
        private const val PLAYER_MATCHUP_X=136f
        const val HUD_TOGGLE_CENTER_X=432f
        const val HUD_TOGGLE_CENTER_Y=72f
        const val ENEMY_MOVE_CENTER_X=558f
    }
}