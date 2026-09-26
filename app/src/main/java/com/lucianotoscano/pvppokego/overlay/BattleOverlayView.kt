package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.lucianotoscano.pvppokego.data.BattleUiState
import com.lucianotoscano.pvppokego.data.ChargedPrediction
import com.lucianotoscano.pvppokego.data.MatchupState
import com.lucianotoscano.pvppokego.data.SettingsRepository

/** Phase 1 overlay is intentionally passive (touch-through). */
class BattleOverlayView(context: Context, private val settings: SettingsRepository) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.5f; color = Color.WHITE }
    private var state = BattleUiState()
    fun render(newState: BattleUiState) { state = newState; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = settings.scale
        paint.alpha = (255 * settings.opacity).toInt().coerceIn(40, 255); stroke.alpha = paint.alpha
        if (settings.showCurrentIndicator) drawMatchup(canvas, 38f * scale, 96f * scale, state.playerMatchup)
        if (settings.showStrongTypes) drawStrongTypes(canvas, scale)
        if (settings.showEnemyMoves) drawMoves(canvas, scale)
        if (settings.analyzeReserves) drawReserves(canvas, scale)
        if (settings.debugMode) drawDebug(canvas, scale)
    }

    private fun drawMatchup(canvas: Canvas, cx: Float, cy: Float, state: MatchupState) {
        val r = 15f * settings.scale
        paint.style = Paint.Style.FILL
        paint.color = when (state) { MatchupState.FAVORABLE -> Color.rgb(20,190,80); MatchupState.UNFAVORABLE -> Color.rgb(225,45,55); MatchupState.NEUTRAL -> Color.rgb(150,150,150); MatchupState.UNKNOWN -> Color.rgb(105,105,105) }
        canvas.drawCircle(cx,cy,r,paint); canvas.drawCircle(cx,cy,r,stroke)
        paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.textSize=17f*settings.scale; paint.typeface=android.graphics.Typeface.DEFAULT_BOLD
        val glyph=when(state){ MatchupState.FAVORABLE->"✓"; MatchupState.UNFAVORABLE->"×"; else->"•" }
        canvas.drawText(glyph,cx,cy+5.5f*settings.scale,paint)
    }

    private fun drawStrongTypes(canvas: Canvas, scale: Float) {
        val types=state.strongTypesAgainstOpponent.take(6); if(types.isEmpty()) return
        var x=width-(types.size*29f+12f)*scale; val y=40f*scale
        types.forEach { drawTypeChip(canvas,x,y,it,11f*scale); x+=29f*scale }
    }

    private fun drawMoves(canvas: Canvas, scale: Float) {
        val centerX=width*.72f; val fastY=height*.23f; val chargedY=height*.33f; val spread=73f*scale
        state.fastMove?.let { drawTypeChip(canvas,centerX,fastY,it.type,22f*scale); label(canvas,centerX,fastY+40f*scale,short(it.name)) }
        state.charged1?.let { drawCharged(canvas,centerX-spread,chargedY,it,scale) }
        state.charged2?.let { drawCharged(canvas,centerX+spread,chargedY,it,scale) }
    }

    private fun drawCharged(canvas: Canvas,cx:Float,cy:Float,p:ChargedPrediction,scale:Float){
        val r=30f*scale; paint.style=Paint.Style.FILL; paint.color=Color.argb(paint.alpha,90,90,90); canvas.drawCircle(cx,cy,r,paint)
        paint.color=withAlpha(typeColor(p.move.type),paint.alpha)
        if(settings.showEnergyProgress) canvas.drawArc(RectF(cx-r,cy-r,cx+r,cy+r),-90f,360f*p.progress,true,paint) else canvas.drawCircle(cx,cy,r,paint)
        canvas.drawCircle(cx,cy,r,stroke); typeGlyph(canvas,cx,cy,p.move.type,13f*scale)
        if(settings.showChargedCounter){ val br=12f*scale; val bx=cx+r*.78f; val by=cy+r*.72f; paint.color=if(p.ready)Color.rgb(15,185,75) else Color.rgb(30,30,30); canvas.drawCircle(bx,by,br,paint); canvas.drawCircle(bx,by,br,stroke); paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.textSize=13f*scale; canvas.drawText(p.fastMovesRemaining.toString(),bx,by+4.5f*scale,paint) }
        label(canvas,cx,cy+r+19f*scale,short(p.move.name))
    }

    private fun drawReserves(canvas:Canvas,scale:Float){ val x=width-24f*scale; val firstY=height*.57f; state.reserves.take(2).forEachIndexed{i,reserve-> val y=firstY+i*112f*scale; drawMatchup(canvas,x,y,reserve.matchup); if(settings.showReserveTypes){ var tx=x-18f*scale; reserve.types.take(2).forEach{ drawTypeChip(canvas,tx,y+43f*scale,it,10f*scale); tx+=26f*scale } } } }
    private fun drawDebug(canvas:Canvas,scale:Float){ val text=state.debugText?:return; val box=RectF(8f,height-145f*scale,width*.76f,height-8f); paint.color=Color.argb(190,0,0,0); canvas.drawRoundRect(box,8f,8f,paint); paint.color=Color.WHITE; paint.textAlign=Paint.Align.LEFT; paint.textSize=10f*scale; text.lines().take(8).forEachIndexed{i,line->canvas.drawText(line,box.left+8f,box.top+(i+1)*14f*scale,paint)} }
    private fun drawTypeChip(canvas:Canvas,cx:Float,cy:Float,type:String,r:Float){ paint.color=withAlpha(typeColor(type),paint.alpha); canvas.drawCircle(cx,cy,r,paint); canvas.drawCircle(cx,cy,r,stroke); typeGlyph(canvas,cx,cy,type,r*.82f) }
    private fun typeGlyph(canvas:Canvas,cx:Float,cy:Float,type:String,size:Float){ paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.textSize=size; paint.typeface=android.graphics.Typeface.DEFAULT_BOLD; canvas.drawText(type.trim().take(2).uppercase().ifBlank{"?"},cx,cy+size*.34f,paint) }
    private fun label(canvas:Canvas,x:Float,y:Float,text:String){ paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.textSize=12f*settings.scale; paint.typeface=android.graphics.Typeface.DEFAULT_BOLD; paint.setShadowLayer(3f,0f,1f,Color.BLACK); canvas.drawText(text,x,y,paint); paint.clearShadowLayer() }
    private fun short(name:String)=if(name.length<=19)name else name.take(18)+"…"
    private fun withAlpha(c:Int,a:Int)=Color.argb(a,Color.red(c),Color.green(c),Color.blue(c))
    private fun typeColor(type:String):Int=when(type.lowercase()){ "fire"->Color.rgb(238,129,48); "water"->Color.rgb(99,144,240); "electric"->Color.rgb(247,208,44); "grass"->Color.rgb(122,199,76); "ice"->Color.rgb(150,217,214); "fighting"->Color.rgb(194,46,40); "poison"->Color.rgb(163,62,161); "ground"->Color.rgb(226,191,101); "flying"->Color.rgb(169,143,243); "psychic"->Color.rgb(249,85,135); "bug"->Color.rgb(166,185,26); "rock"->Color.rgb(182,161,54); "ghost"->Color.rgb(115,87,151); "dragon"->Color.rgb(111,53,252); "dark"->Color.rgb(112,87,70); "steel"->Color.rgb(183,183,206); "fairy"->Color.rgb(214,133,173); else->Color.rgb(168,167,122) }
}
