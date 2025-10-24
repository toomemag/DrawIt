package com.example.drawit.painting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class CanvasView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var layers: List<Layer> = emptyList()
    var paint = android.graphics.Paint().apply {
        // blurry otherwise
        isAntiAlias = false
        isFilterBitmap = false
    }
    private val bitmapScaleRect = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // scale bitmap to fill the view size
        bitmapScaleRect.set(0, 0, width, height)

        // draw all layers
        for (i in layers.indices) {
            val layer = layers[i]

            paint.alpha = if (layer.isActive) 255 else 55
            canvas.drawBitmap(layer.bitmap, null, bitmapScaleRect, paint)
        }
    }

    fun invalidateLayers() {
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}