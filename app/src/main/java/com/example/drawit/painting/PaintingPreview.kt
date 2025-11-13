package com.example.drawit.painting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * Custom view extending Canvas for rendering a painting preview.
 *  Used in feeds and user profiles.
 *
 * @param context The context of the view
 * @param attrs The attribute set for the view
 *
 * @property layers The list of layers to be drawn on the canvas
 * @property paint The paint object used for drawing
 * @property bitmapScaleRect The rectangle used for scaling bitmaps to fit the view
 */
class PaintingPreview(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var layers: List<Layer> = emptyList()

    var paint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    private val bitmapScaleRect = Rect()


    /**
     * Draws the layers onto the canvas. Used for feed preview rendering.
     *  Effects are only shown when clicked and opened (different component).
     *
     * @param canvas The canvas to draw on
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmapScaleRect.set(0, 0, width, height)

        // draw all layers
        for (layer in layers) {
            canvas.drawBitmap(layer.bitmap, null, bitmapScaleRect, paint)
        }
    }
}