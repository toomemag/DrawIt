package com.example.drawit.painting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.example.drawit.painting.effects.BaseEffect

/**
 * Represents a layer transform input option
 * Used in LayerEffectBinding to map effect outputs to layer transform inputs
 * eg. map gyro yaw to layer x position
 */
enum class LayerTransformInput {
    X_POS,
    Y_POS,
    ROTATION,
    SCALE
}

/**
 * Binds an effect output to a layer transform input
 * @param effectInputIndex The index of the effect output to bind
 * @param layerTransformInput The layer transform input to bind to
 */
data class LayerEffectBinding(
    var effectInputIndex: Int,
    var layerTransformInput: LayerTransformInput,
)

/**
 * Represents a drawable layer with effects
 * @param name The name of the layer, used for debugging
 *
 * @property bitmap: The bitmap representing the layer's pixel data
 * @property isActive: Whether the layer is active (paintable)
 * @property offset: The x and y offset of the layer (for transforming position)
 * @property effectBindings: A map of effect type to list of LayerEffectBindings
 * @property paint: The Paint object used for drawing the layer
 */
// debug name more than anything
class Layer(var name: String = "Layer") {
    var bitmap: Bitmap = createBitmap(128, 128)

    var isActive: Boolean = true
    var offset: Array<Int> = arrayOf(0, 0)

    val effectBindings: MutableMap<Int, MutableList<LayerEffectBinding>> = mutableMapOf()

    // paint with no anti-aliasing or bitmap filtering for pixel art style
    var paint = android.graphics.Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

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
}