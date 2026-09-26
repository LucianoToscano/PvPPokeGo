package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.*
import android.view.View
import com.lucianotoscano.pvppokego.data.*

/** Minimal, passive overlay. It never consumes Pokemon GO touches. */
class BattleOverlayView(context:Context, private val settings:SettingsRepository):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val s=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeWidth=2.5f;color=Color.WHITE}; private var st=BattleUiState()
 fun render(v:BattleUiState){st=v;invalidate()}
 override fun onDraw(c:Canvas){super.onDraw(c); val z=settings.scale;p.alpha=(255*settings.opacity).toInt().coerceIn(40,255);s.alpha=p.alpha;if(settings.showCurrentIndicator)match(c,38f*z,96f*z,st.playerMatchup);if(settings.showStrongTypes)strong(c,z);if(settings.showEnemyMoves)moves(c,z);if(settings.analyzeReserves)reserves(c,z);if(settings.debugMode)debug(c,z)}
 private fun match(c:Canvas,x:Float,y:Float,m:MatchupState){val r=15f*settings.scale;p.style=Paint.Style.FILL;p.color=when(m){MatchupState.FAVORABLE->Color.rgb(20,190,80);MatchupState.UNFAVORABLE->Color.rgb(225,45,55);MatchupState.NEUTRAL->Color.rgb(150,150,150);else->Color.rgb(105,105,105)};c.drawCircle(x,y,r,p);c.drawCircle(x,y,r,s);p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=17f*settings.scale;p.typeface=Typeface.DEFAULT_BOLD;c.drawText(when(m){MatchupState.FAVORABLE->"✓";MatchupState.UNFAVORABLE->"×";else->"•"},x,y+5.5f*settings.scale,p)}
 private fun strong(c:Canvas,z:Float){val a=st.strongTypesAgainstOpponent.take(6);var x=width-(a.size*31f+12f)*z;a.forEach{chip(c,x,40f*z,it,12f*z);x+=31f*z}}
 private fun moves(c:Canvas,z:Float){val x=width*.72f;val fy=height*.23f;val cy=height*.33f;val d=73f*z;st.fastMove?.let{chip(c,x,fy,it.type,22f*z);label(c,x,fy+40f*z,short(it.name))};st.charged1?.let{charged(c,x-d,cy,it,z)};st.charged2?.let{charged(c,x+d,cy,it,z)}}
 private fun charged(c:Canvas,x:Float,y:Float,q:ChargedPrediction,z:Float){val r=30f*z;p.style=Paint.Style.FILL;p.color=Color.argb(p.alpha,90,90,90);c.drawCircle(x,y,r,p);p.color=alpha(typeColor(q.move.type));if(settings.showEnergyProgress)c.drawArc(RectF(x-r,y-r,x+r,y+r),-90f,360f*q.progress,true,p)else c.drawCircle(x,y,r,p);c.drawCircle(x,y,r,s);symbol(c,x,y,q.move.type,17f*z);if(settings.showChargedCounter){val br=12f*z,bx=x+r*.78f,by=y+r*.72f;p.color=if(q.ready)Color.rgb(15,185,75)else Color.rgb(30,30,30);c.drawCircle(bx,by,br,p);c.drawCircle(bx,by,br,s);p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=13f*z;p.typeface=Typeface.DEFAULT_BOLD;c.drawText(q.fastMovesRemaining.toString(),bx,by+4.5f*z,p)};label(c,x,y+r+19f*z,short(q.move.name))}
 private fun reserves(c:Canvas,z:Float){val x=width-24f*z;st.reserves.take(2).forEachIndexed{i,r->val y=height*.57f+i*112f*z;match(c,x,y,r.matchup);if(settings.showReserveTypes){var tx=x-18f*z;r.types.take(2).forEach{chip(c,tx,y+43f*z,it,11f*z);tx+=27f*z}}}}
 private fun debug(c:Canvas,z:Float){val t=st.debugText?:return;val b=RectF(8f,height-132f*z,width*.64f,height-8f);p.color=Color.argb(175,0,0,0);c.drawRoundRect(b,8f,8f,p);p.color=Color.WHITE;p.textAlign=Paint.Align.LEFT;p.textSize=9.5f*z;p.typeface=Typeface.DEFAULT;t.lines().take(7).forEachIndexed{i,l->c.drawText(l,b.left+8f,b.top+(i+1)*13f*z,p)}}
 private fun chip(c:Canvas,x:Float,y:Float,t:String,r:Float){p.style=Paint.Style.FILL;p.color=alpha(typeColor(t));c.drawCircle(x,y,r,p);c.drawCircle(x,y,r,s);symbol(c,x,y,t,r*1.12f)}
 private fun symbol(c:Canvas,x:Float,y:Float,t:String,n:Float){val u=n/18f;p.color=Color.WHITE;p.alpha=(255*settings.opacity).toInt().coerceIn(80,255);p.style=Paint.Style.FILL;p.strokeWidth=2f*u;p.strokeCap=Paint.Cap.ROUND;p.strokeJoin=Paint.Join.ROUND
  when(t.lowercase()){
   "water"->{val q=Path();q.moveTo(x,y-9*u);q.cubicTo(x-2*u,y-5*u,x-7*u,y,x-7*u,y+4*u);q.cubicTo(x-7*u,y+9*u,x+7*u,y+9*u,x+7*u,y+4*u);q.cubicTo(x+7*u,y,x+2*u,y-5*u,x,y-9*u);q.close();c.drawPath(q,p)}
   "fire"->{val q=Path();q.moveTo(x+u,y-10*u);q.cubicTo(x+6*u,y-3*u,x,y,x+6*u,y+3*u);q.cubicTo(x+10*u,y+7*u,x+4*u,y+11*u,x,y+9*u);q.cubicTo(x-8*u,y+8*u,x-8*u,y+u,x-4*u,y-3*u);q.cubicTo(x-3*u,y+2*u,x+u,y+u,x+u,y-10*u);q.close();c.drawPath(q,p)}
   "electric"->{val q=Path();q.moveTo(x+u,y-10*u);q.lineTo(x-7*u,y+u);q.lineTo(x-u,y+u);q.lineTo(x-4*u,y+10*u);q.lineTo(x+8*u,y-3*u);q.lineTo(x+2*u,y-3*u);q.close();c.drawPath(q,p)}
   "grass"->{val q=Path();q.moveTo(x-9*u,y+7*u);q.cubicTo(x-7*u,y-5*u,x+2*u,y-10*u,x+10*u,y-9*u);q.cubicTo(x+9*u,y+u,x+3*u,y+8*u,x-9*u,y+7*u);q.close();c.drawPath(q,p);p.style=Paint.Style.STROKE;c.drawLine(x-6*u,y+5*u,x+6*u,y-6*u,p)}
   "ice"->{p.style=Paint.Style.STROKE;for(a in listOf(0f,60f,120f)){c.save();c.rotate(a,x,y);c.drawLine(x-9*u,y,x+9*u,y,p);c.drawLine(x+5*u,y,x+2*u,y-3*u,p);c.drawLine(x+5*u,y,x+2*u,y+3*u,p);c.restore()}}
   "ground"->{val q=Path();q.moveTo(x-10*u,y+8*u);q.lineTo(x-2*u,y-8*u);q.lineTo(x+3*u,y);q.lineTo(x+7*u,y-5*u);q.lineTo(x+11*u,y+8*u);q.close();c.drawPath(q,p)}
   "flying"->{val q=Path();q.moveTo(x-10*u,y+5*u);q.cubicTo(x-2*u,y+2*u,x+2*u,y-8*u,x+10*u,y-8*u);q.cubicTo(x+7*u,y-u,x+2*u,y+7*u,x-10*u,y+5*u);q.close();c.drawPath(q,p)}
   "psychic"->{p.style=Paint.Style.STROKE;for(i in 0..2){val r=(3.5f+i*2.5f)*u;c.drawArc(RectF(x-r,y-r,x+r,y+r),-45f+i*20f,250f,false,p)};c.drawCircle(x,y,1.5f*u,p)}
   "normal"->{p.style=Paint.Style.STROKE;p.strokeWidth=3f*u;c.drawCircle(x,y,7f*u,p)}
   else->{p.textAlign=Paint.Align.CENTER;p.textSize=15f*u;p.typeface=Typeface.DEFAULT_BOLD;val g=when(t.lowercase()){ "fighting"->"✊";"poison"->"☠";"bug"->"●";"rock"->"◆";"ghost"->"◉";"dragon"->"◇";"dark"->"☾";"steel"->"⚙";"fairy"->"✦";else->"?"};c.drawText(g,x,y+5f*u,p)}
  };p.style=Paint.Style.FILL
 }
 private fun label(c:Canvas,x:Float,y:Float,t:String){p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=12f*settings.scale;p.typeface=Typeface.DEFAULT_BOLD;p.setShadowLayer(3f,0f,1f,Color.BLACK);c.drawText(t,x,y,p);p.clearShadowLayer()}
 private fun short(n:String)=if(n.length<=19)n else n.take(18)+"…"
 private fun alpha(c:Int)=Color.argb(p.alpha,Color.red(c),Color.green(c),Color.blue(c))
 private fun typeColor(t:String)=when(t.lowercase()){ "fire"->Color.rgb(238,129,48);"water"->Color.rgb(99,144,240);"electric"->Color.rgb(247,208,44);"grass"->Color.rgb(122,199,76);"ice"->Color.rgb(150,217,214);"fighting"->Color.rgb(194,46,40);"poison"->Color.rgb(163,62,161);"ground"->Color.rgb(226,191,101);"flying"->Color.rgb(169,143,243);"psychic"->Color.rgb(249,85,135);"bug"->Color.rgb(166,185,26);"rock"->Color.rgb(182,161,54);"ghost"->Color.rgb(115,87,151);"dragon"->Color.rgb(111,53,252);"dark"->Color.rgb(112,87,70);"steel"->Color.rgb(183,183,206);"fairy"->Color.rgb(214,133,173);else->Color.rgb(168,167,122)}
}
