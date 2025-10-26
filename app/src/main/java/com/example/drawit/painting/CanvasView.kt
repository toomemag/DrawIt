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
        bitmapScaleRect.set(0, 0, 0, 0)

        val anySelected = layers.stream().anyMatch { layer -> layer.isActive }
        val globalAlpha = if (!anySelected) 255 else 55

        // todo: do we have to scale the offset relative to the canvas size?
        //       right now 100 offset means 100px device screen not 100px canvas

        // draw all layers
        for (i in layers.indices) {
            val layer = layers[i]
            bitmapScaleRect.left = layer.offset[0]
            bitmapScaleRect.top = layer.offset[1]
            bitmapScaleRect.right = layer.offset[0] + width
            bitmapScaleRect.bottom = layer.offset[1] + height

            paint.alpha = if (layer.isActive) 255 else globalAlpha
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