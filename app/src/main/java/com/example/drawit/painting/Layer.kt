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

// debug name more than anything
class Layer(var name: String = "Layer") {
    var bitmap: Bitmap = createBitmap(128, 128)
    // cant access canvasmanager from activity view
    var isActive: Boolean = true
    var offset: Array<Int> = arrayOf(0, 0)

    val effects: MutableList<BaseEffect<*>> = mutableListOf()



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

    fun addEffect(effect: BaseEffect<*>) {
        effects.plus(effect)
    }

    fun removeEffect(effect: BaseEffect<*>) {
        // each layer can have only one of each effect type
        val idx = effects.indexOfFirst { it.getEffectType() == effect.getEffectType() }
        if (idx >= 0) effects.removeAt(idx)
    }

    // todo: in the future could add relative scaling or set px transform
    fun addSensorTransform(sensorType: Int, transformInputType: LayerTransformInput) {
        // add new mapping for a sensor type
        // this expects sensor to already have translated the event
        // sensor should be able to transform based on given input list
        // sure, here we add a new entry to transform map, how to we pass what we want to sensor.transformInput?
    }
}