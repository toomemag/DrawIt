package com.example.drawit.painting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * Custom view for rendering a canvas with multiple layers. Each layer can have
 *  its own bitmap and offset. Inactive layers are drawn with reduced opacity.
 *
 * @param context The context of the view
 * @param attrs The attribute set for the view
 *
 * @property layers The list of layers to be drawn on the canvas
 * @property paint The paint object used for drawing
 * @property bitmapScaleRect The rectangle used for scaling bitmaps to fit the view
 */
class CanvasView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var layers: List<Layer> = emptyList()

    // pixel art style
    var paint = android.graphics.Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    private val bitmapScaleRect = Rect()

    /* unless i find a cleaner way to show the grid, won't be added
        maybe only show it if the zoom is over a certain threshold,
        kind of like figma does it?
        also probably doing it as a shader is quite a lot more performant :rofl:

    private val baseGridSize = 2048
    private var guidelineGrid: Bitmap = createBitmap(baseGridSize, baseGridSize);

    init {
        // 128x128 layers, could do a global layer size later, wouldn't need to hardcode size
        val everyNPixel = baseGridSize / 128

        for (y in 0..< baseGridSize) {
            for (x in 0 ..< baseGridSize) {
                if (y % everyNPixel == 0 || x % everyNPixel == 0) {
                    guidelineGrid.setPixel(x, y, 0x20FFFFFF)
                } else {
                    guidelineGrid.setPixel(x, y, Color.BLACK)
                }
            }
        }
    }*/


    /**
     * Draws the layers onto the canvas. Active layers are drawn with full opacity,
     *  while inactive layers are drawn with reduced opacity.
     *
     * @param canvas The canvas to draw on
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // scale bitmap to fill the view size
        bitmapScaleRect.set(0, 0, width, height)

        // canvas.drawBitmap(guidelineGrid, null, bitmapScaleRect, paint)

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

    /**
     * Invalidates the view, causing it to be redrawn.
     */
    fun invalidateLayers() {
        invalidate()
        android.util.Log.d( "CanvasView", "layers invalidated, redraw triggered" )
    }

    /**
     * Handles click events on the view.
     * @return true to indicate the click was handled
     */
    override fun performClick(): Boolean {
        // todo: think we could move some click handling logic from PaintingActivity here later
        //       have to see as we have some state management in PaintingActivity
        super.performClick()
        return true
    }
}