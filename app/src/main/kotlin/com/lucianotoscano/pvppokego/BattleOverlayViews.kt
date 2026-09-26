package com.lucianotoscano.pvppokego

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.MoveData

object TypeColors {
    private val colors = mapOf(
        "bug" to Color.rgb(145, 193, 47),
        "dark" to Color.rgb(90, 84, 101),
        "dragon" to Color.rgb(11, 109, 195),
        "electric" to Color.rgb(244, 210, 60),
        "fairy" to Color.rgb(236, 143, 230),
        "fighting" to Color.rgb(206, 65, 107),
        "fire" to Color.rgb(255, 157, 85),
        "flying" to Color.rgb(137, 170, 227),
        "ghost" to Color.rgb(82, 105, 173),
        "grass" to Color.rgb(99, 188, 90),
        "ground" to Color.rgb(217, 120, 69),
        "ice" to Color.rgb(115, 206, 192),
        "normal" to Color.rgb(145, 154, 162),
        "poison" to Color.rgb(181, 103, 206),
        "psychic" to Color.rgb(250, 113, 121),
        "rock" to Color.rgb(197, 183, 140),
        "steel" to Color.rgb(90, 142, 162),
        "water" to Color.rgb(80, 144, 214)
    )

    fun color(type: String?): Int = colors[type?.lowercase()] ?: Color.GRAY
}

/**
 * Pokemon-GO-like charge indicator: a neutral circular base fills from the
 * bottom with the charged move type color as estimated energy accumulates.
 */
class ChargeProgressCircle(context: Context) : View(context) {
    var fraction: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }
    var type: String = "normal"
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.6f)
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pad = dp(2f)
        val rect = RectF(pad, pad, width - pad, height - pad)

        paint.style = Paint.Style.FILL
        paint.color = Color.argb(235, 103, 104, 108)
        canvas.drawOval(rect, paint)

        if (fraction > 0f) {
            val save = canvas.save()
            val clipTop = rect.bottom - rect.height() * fraction
            val circle = Path().apply { addOval(rect, Path.Direction.CW) }
            canvas.clipPath(circle)
            canvas.clipRect(rect.left, clipTop, rect.right, rect.bottom)
            paint.color = TypeColors.color(type)
            canvas.drawOval(rect, paint)
            canvas.restoreToCount(save)
        }

        canvas.drawOval(rect, border)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}

/** Compact clickable charged-move control used by the classic overlay. */
class ChargedMoveChip(context: Context) : LinearLayout(context) {
    private val circleFrame = FrameLayout(context)
    private val progressCircle = ChargeProgressCircle(context)
    private val icon = TypeIconView(context)
    private val countBadge = TextView(context)
    private val moveLabel = TextView(context)

    var buttonIndex: Int = 0
    var onSelectRequested: ((Int, View) -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(2), dp(2), dp(2), dp(2))
        minimumHeight = dp(44)
        isClickable = true

        circleFrame.addView(progressCircle, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER))
        circleFrame.addView(icon, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.CENTER))
        circleFrame.addView(
            countBadge,
            FrameLayout.LayoutParams(dp(22), dp(22), Gravity.BOTTOM or Gravity.END).apply {
                setMargins(0, 0, dp(1), dp(1))
            }
        )
        addView(circleFrame, LayoutParams(dp(64), dp(64)))

        countBadge.gravity = Gravity.CENTER
        countBadge.textSize = 12f
        countBadge.setTextColor(Color.WHITE)
        countBadge.setShadowLayer(2f, 0f, 1f, Color.BLACK)
        countBadge.background = badgeBackground(false)

        moveLabel.gravity = Gravity.CENTER
        moveLabel.textSize = 11f
        moveLabel.setTextColor(Color.WHITE)
        moveLabel.setShadowLayer(3f, 0f, 1f, Color.BLACK)
        moveLabel.maxLines = 2
        moveLabel.setPadding(dp(1), 0, dp(1), 0)
        addView(moveLabel, LayoutParams(dp(92), ViewGroup.LayoutParams.WRAP_CONTENT))

        setOnClickListener { onSelectRequested?.invoke(buttonIndex, this) }
    }

    fun bind(progress: ChargedMoveProgress?, displayName: String? = null) {
        if (progress == null) {
            progressCircle.fraction = 0f
            progressCircle.type = "normal"
            icon.setTypeSymbol(null)
            countBadge.text = "?"
            countBadge.background = badgeBackground(false)
            moveLabel.text = "Carregado ▼"
            return
        }

        val move = progress.move
        val ratio = if (move.energyCost <= 0) 0f
        else progress.currentEnergy.toFloat() / move.energyCost.toFloat()

        progressCircle.type = move.type
        progressCircle.fraction = ratio.coerceIn(0f, 1f)
        icon.setTypeSymbol(move.type)
        countBadge.text = progress.fastMovesNeeded.toString()
        countBadge.background = badgeBackground(progress.ready)
        moveLabel.text = "${displayName ?: move.name} ▼"
    }

    fun bindMoveOnly(move: MoveData?, displayName: String? = null) {
        progressCircle.fraction = 0f
        progressCircle.type = move?.type ?: "normal"
        icon.setTypeSymbol(move?.type)
        countBadge.text = "?"
        countBadge.background = badgeBackground(false)
        moveLabel.text = "${displayName ?: move?.name ?: "Carregado"} ▼"
    }

    private fun badgeBackground(ready: Boolean) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(if (ready) Color.rgb(20, 170, 82) else Color.rgb(28, 124, 160))
        setStroke(dp(1), Color.WHITE)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

/** Compact clickable fast-move control used by the classic overlay. */
class FastMoveChip(context: Context) : LinearLayout(context) {
    private val icon = TypeIconView(context)
    private val label = TextView(context)
    var onSelectRequested: ((View) -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(3), dp(2), dp(3), dp(2))
        minimumHeight = dp(44)
        isClickable = true

        addView(icon, LayoutParams(dp(44), dp(44)))
        addView(label, LayoutParams(dp(98), ViewGroup.LayoutParams.WRAP_CONTENT))
        label.gravity = Gravity.CENTER
        label.textSize = 11f
        label.setTextColor(Color.WHITE)
        label.setShadowLayer(3f, 0f, 1f, Color.BLACK)
        label.maxLines = 2
        setOnClickListener { onSelectRequested?.invoke(this) }
    }

    fun bind(move: MoveData?, displayName: String? = null) {
        icon.setType(move?.type)
        label.text = "${displayName ?: move?.name ?: "Ataque rápido"} ▼"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
