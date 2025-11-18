package com.example.drawit.domain.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Base64
import androidx.core.graphics.createBitmap
import com.example.drawit.painting.effects.BaseEffect

data class Layer(
    val id: String = "",
    var bitmap: Bitmap = createBitmap(128, 128),
    var isActive: Boolean = true,
    var offset: Array<Int> = arrayOf(0, 0),
    val effectBindings: MutableMap<Int, MutableList<LayerEffectBinding>> = mutableMapOf(),
    var lastUpdatedTimestamp: Long = 0,
    // paint with no anti-aliasing or bitmap filtering for pixel art style
    var paint: Paint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }
) {
    /**
     * Sets the position offset of the layer
     * @param x The x offset
     * @param y The y offset
     */
    fun setPos(x: Int, y: Int) {
        offset[0] = x
        offset[1] = y
    }

    /**
     * Draws the layer's bitmap onto the given canvas
     * @param canvas The canvas to draw on
     * @param width The width to draw the bitmap
     * @param height The height to draw the bitmap
     * @param rect The destination rectangle to draw the bitmap into (defaults to offset and size)
     * @param paint The paint to use for drawing (defaults to layer's paint)
     */
    fun spewToCanvas(canvas: Canvas, width: Int, height: Int, rect: Rect = Rect(offset[0], offset[1], width, height), paint: android.graphics.Paint = this.paint) {
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    /**
     * Adds an effect binding for the given effect
     * @param effect The effect to bind
     */
    fun addEffectBinding(effect: BaseEffect<*>) {
        effectBindings.putIfAbsent(effect.getEffectType(), mutableListOf())
    }

    /**
     * Gets the effect bindings for the given effect
     * @param effect The effect to get bindings for
     * @return The list of LayerEffectBindings for the effect
     * @throws IllegalArgumentException if the effect is not bound to the layer
     */
    fun getEffectBindings(effect: BaseEffect<*>): MutableList<LayerEffectBinding> {
        if (!effectBindings.containsKey(effect.getEffectType())) {
            throw IllegalArgumentException("Effect not bound to layer")
        }

        return effectBindings[effect.getEffectType()]!!
    }

    /**
     * Removes the effect binding for the given effect
     * @param effect The effect to remove bindings for
     */
    fun removeEffectBinding(effect: BaseEffect<*>) {
        effectBindings.remove(effect.getEffectType())
    }

    /**
     * Applies the effect translation to the layer based on the given input mode
     * @param inputMode The layer transform input mode to apply, eg pos, size etc
     */
    fun <EffectTransformReturn>applyEffectTranslation(effectTransformResult: EffectTransformReturn, inputMode: LayerTransformInput, accumulate: Boolean = false) {
        when(inputMode) {
            LayerTransformInput.X_POS -> {
                // todo: better types :sob:
                val transformed = (effectTransformResult as Float).toInt()
                if ( !accumulate )
                    offset[0] = transformed
                else
                    offset[0] += transformed
            }
            LayerTransformInput.Y_POS -> {
                val transformed = (effectTransformResult as Float).toInt()
                if ( !accumulate )
                    offset[1] = transformed
                else
                    offset[1] += transformed
            }
            else -> {
                // other input modes not implemented yet
            }
        }
    }

    fun serializeForFirebase(): Map<String, Any> {
        // serialize bitmap, make string of it, b64encode
        val bytes = ByteArray(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(java.nio.ByteBuffer.wrap(bytes))
        val b64bitmap = Base64.encode(bytes, Base64.DEFAULT).toString(Charsets.UTF_8)

        val bindingsSerialized = mutableMapOf<String, Any>()
        for ( (effectType, bindings) in effectBindings ) {
            val bindingList = bindings.map { binding ->
                mapOf(
                    "effectInputIndex" to binding.effectOutputIndex,
                    "layerTransformInput" to binding.layerTransformInput.name
                )
            }
            bindingsSerialized[effectType.toString()] = bindingList
        }

        return mapOf(
            "bitmap" to b64bitmap,
            // todo: if we add scaling later, would be nice if we had a separate serialization for bindings
            "bindings" to bindingsSerialized,
        )
    }
}