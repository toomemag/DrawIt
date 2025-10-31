package com.example.drawit.painting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.example.drawit.painting.effects.BaseEffect

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
}