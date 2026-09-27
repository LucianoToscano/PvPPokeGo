from pathlib import Path
import re

p = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt')
s = p.read_text()

s = s.replace('const val TOP_ICON_Y=28f', 'const val TOP_ICON_Y=66f')
s = s.replace('const val HUD_TOGGLE_CENTER_Y=28f', 'const val HUD_TOGGLE_CENTER_Y=66f')
s = s.replace(
    'val spread=rx(72f); val fastRadius=31f*u(); val chargedRadius=38f*u()',
    'val spread=rx(74f); val fastRadius=31f*u(); val chargedRadius=41f*u()'
)

charged = '''    private fun drawCharged(canvas: Canvas,cx: Float,cy: Float,radius: Float,prediction: ChargedPrediction) {
        val circle=Path().apply { addCircle(cx,cy,radius,Path.Direction.CW) }
        canvas.save(); canvas.clipPath(circle)
        paint.style=Paint.Style.FILL
        paint.color=hudColor(Color.rgb(22,26,34),.84f)
        canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        if (settings.showEnergyProgress) {
            val progress=prediction.progress.coerceIn(0f,1f)
            val fillTop=cy+radius-2f*radius*progress
            paint.color=hudColor(typeColor(prediction.move.type),.92f)
            canvas.drawRect(cx-radius,fillTop,cx+radius,cy+radius,paint)
            if (progress>.02f && progress<.98f) {
                paint.color=hudColor(Color.WHITE,.62f)
                canvas.drawRect(cx-radius,fillTop-1.1f*u(),cx+radius,fillTop+1.1f*u(),paint)
            }
        } else {
            paint.color=hudColor(typeColor(prediction.move.type),.90f)
            canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        }
        canvas.restore()
        stroke.style=Paint.Style.STROKE
        stroke.color=hudColor(Color.rgb(10,13,18),.82f)
        stroke.strokeWidth=5.2f*u()
        canvas.drawCircle(cx,cy,radius,stroke)
        stroke.color=hudColor(Color.WHITE,.68f)
        stroke.strokeWidth=1.5f*u()
        canvas.drawCircle(cx,cy,radius-1.2f*u(),stroke)
        drawTypeGlyph(canvas,cx,cy,prediction.move.type,23f*u())
        if (settings.showChargedCounter) drawChargeProgressDots(canvas,cx,cy,radius,prediction)
    }
'''
s = re.sub(
    r'    private fun drawCharged\(.*?\n    }\n\n    private fun drawChargeProgressDots',
    charged + '\n    private fun drawChargeProgressDots', s, count=1, flags=re.S
)

fast = '''    private fun drawMoveCircle(canvas: Canvas,cx: Float,cy: Float,radius: Float,move: MoveDef) {
        val circle=Path().apply { addCircle(cx,cy,radius,Path.Direction.CW) }
        canvas.save(); canvas.clipPath(circle)
        paint.style=Paint.Style.FILL
        paint.color=hudColor(Color.rgb(22,26,34),.84f)
        canvas.drawRect(cx-radius,cy-radius,cx+radius,cy+radius,paint)
        paint.color=hudColor(typeColor(move.type),.88f)
        val fillTop=cy+radius*.05f
        canvas.drawRect(cx-radius,fillTop,cx+radius,cy+radius,paint)
        paint.color=hudColor(Color.WHITE,.48f)
        canvas.drawRect(cx-radius,fillTop-1f*u(),cx+radius,fillTop+1f*u(),paint)
        canvas.restore()
        stroke.style=Paint.Style.STROKE
        stroke.color=hudColor(Color.rgb(10,13,18),.82f)
        stroke.strokeWidth=4.7f*u()
        canvas.drawCircle(cx,cy,radius,stroke)
        stroke.color=hudColor(Color.WHITE,.66f)
        stroke.strokeWidth=1.4f*u()
        canvas.drawCircle(cx,cy,radius-1.1f*u(),stroke)
        drawTypeGlyph(canvas,cx,cy,move.type,20f*u())
    }
'''
s = re.sub(
    r'    private fun drawMoveCircle\(.*?\n    }\n\n    private fun drawMoveLabel',
    fast + '\n    private fun drawMoveLabel', s, count=1, flags=re.S
)

labels = '''    private fun drawMoveLabel(canvas: Canvas,x: Float,y: Float,text: String) {
        paint.color=hudColor(Color.WHITE,.88f)
        paint.textAlign=Paint.Align.CENTER
        paint.textSize=12.5f*u()
        paint.typeface=Typeface.DEFAULT_BOLD
        paint.setShadowLayer(3f,0f,1f,Color.BLACK)
        canvas.drawText(text,x,y,paint)
        paint.clearShadowLayer()
    }
'''
s = re.sub(
    r'    private fun drawMoveLabel\(.*?\n    }\n\n    private fun drawReserves',
    labels + '\n    private fun drawReserves', s, count=1, flags=re.S
)

p.write_text(s)
