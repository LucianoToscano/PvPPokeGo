package com.lucianotoscano.pvppokego

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.caverock.androidsvg.SVG

class TypeIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun setType(type: String?) {
        if (type.isNullOrBlank()) {
            setImageDrawable(null)
            return
        }
        try {
            val svg = SVG.getFromAsset(context.assets, "type_icons/${type.lowercase()}.svg")
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            setImageDrawable(PictureDrawable(svg.renderToPicture()))
        } catch (_: Exception) {
            setImageDrawable(null)
        }
    }
}
