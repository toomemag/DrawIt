package com.example.drawit.painting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.example.drawit.painting.effects.BaseEffect

enum class LayerTransformInput {
    X_POS,
    Y_POS,
    ROTATION,
    SCALE
}

// for effect bindings
// map effect to some interface
// what does the interface need
// masked effect input, eg, transform [ x, y, z ] to [ null, LayerTransformInput, null ]
// on effect input add, default to whatever the first option is
// now when thinking of it might be better to just use index :rofl:
// cant have multiple mappings in one anyway
// mapped from effect to binding in layer
data class LayerEffectBinding(
    var effectInputIndex: Int,
    var layerTransformInput: LayerTransformInput,
)

// debug name more than anything
class Layer(var name: String = "Layer") {
    var bitmap: Bitmap = createBitmap(128, 128)
    // cant access canvasmanager from activity view
    var isActive: Boolean = true
    var offset: Array<Int> = arrayOf(0, 0)

    val effectBindings: MutableMap<Int, MutableList<LayerEffectBinding>> = mutableMapOf()

    var paint = android.graphics.Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    fun setPos(x: Int, y: Int) {
        offset[0] = x
        offset[1] = y
    }

    fun spewToCanvas(canvas: Canvas, width: Int, height: Int, rect: Rect = Rect(offset[0], offset[1], width, height), paint: android.graphics.Paint = this.paint) {
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    fun addEffectBinding(effect: BaseEffect<*>) {
        // make new list if none exists
        effectBindings.putIfAbsent(effect.getEffectType(), mutableListOf())
    }

    fun getEffectBindings(effect: BaseEffect<*>): MutableList<LayerEffectBinding> {
        if (!effectBindings.containsKey(effect.getEffectType())) {
            throw IllegalArgumentException("Effect not bound to layer")
        }

        return effectBindings[effect.getEffectType()]!!
    }

    fun removeEffectBinding(effect: BaseEffect<*>) {
        effectBindings.remove(effect.getEffectType())
    }

    // todo: in the future could add relative scaling or set px transform
    fun addSensorTransform(sensorType: Int, transformInputType: LayerTransformInput) {
        // add new mapping for a sensor type
        // this expects sensor to already have translated the event
        // sensor should be able to transform based on given input list
        // sure, here we add a new entry to transform map, how to we pass what we want to sensor.transformInput?
    }
}