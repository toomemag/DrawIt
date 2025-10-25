package com.example.drawit.painting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.graphics.createBitmap

// debug name more than anything
class Layer(var name: String = "Layer") {
    var bitmap: Bitmap = createBitmap(128, 128)
    // cant access canvasmanager from activity view
    var isActive: Boolean = true

    var paint = android.graphics.Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    fun spewToCanvas(canvas: Canvas, width: Int, height: Int, rect: Rect = Rect(0, 0, width, height), paint: android.graphics.Paint = this.paint) {
        canvas.drawBitmap(bitmap, null, rect, paint)
    }
}