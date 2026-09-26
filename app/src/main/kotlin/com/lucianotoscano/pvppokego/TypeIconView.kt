package com.lucianotoscano.pvppokego

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.caverock.androidsvg.SVG

class TypeIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    fun setType(type: String?) = setAsset(type, "type_icons")
    fun setTypeSymbol(type: String?) = setAsset(type, "type_symbols")

    private fun setAsset(type: String?, folder: String) {
        if (type.isNullOrBlank()) {
            setImageDrawable(null)
            return
        }
        try {
            val svg = SVG.getFromAsset(context.assets, "$folder/${type.lowercase()}.svg")
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            scaleType = ScaleType.CENTER_INSIDE
            setImageDrawable(PictureDrawable(svg.renderToPicture()))
        } catch (_: Exception) {
            setImageDrawable(null)
        }
    }
}
