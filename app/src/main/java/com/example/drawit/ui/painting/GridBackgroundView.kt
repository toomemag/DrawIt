package com.example.drawit.ui.painting

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import android.content.res.Configuration

/**
 * A custom View that draws a grid background with an overlay gradient
 * to fade out the grid under the main canvas painting area.
 *
 * @param context The Context the view is running in
 * @param attrs The attributes of the XML tag that is inflating the view
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource
 *
 * @property cell The size of each grid cell in pixels
 * @property gridPaint The Paint object used to draw the grid lines
 * @property overlayPaint The Paint object used to draw the overlay gradient
 * @property titlebarBottom The bottom Y coordinate of the title bar
 * @property toolbarTop The top Y coordinate of the toolbar
 * @property bgColor The background color of the view
 */
class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cell = 20f
    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = false
        strokeWidth = 1f
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var titlebarBottom: Int = 0
    private var toolbarTop: Int = 0

    private var bgColor: Int = Color.WHITE

    init {
        // do we have dark mode enabled
        val isDarkMode = ( context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ) == Configuration.UI_MODE_NIGHT_YES

        (background as? ColorDrawable)?.let { bgColor = it.color }

        // a bit sad that there is no ternary op in kotlin :(
        val darkened = ColorUtils.blendARGB(bgColor, if (isDarkMode) Color.WHITE else Color.BLACK, 0.18f)
        gridPaint.color = ColorUtils.setAlphaComponent(darkened, 120)
        overlayPaint.color = bgColor
    }

    /**
     * Updates the overlay gradient stops based on the title bar and toolbar positions.
     * @param titlebarBottomY The bottom Y coordinate of the title bar
     * @param toolbarTopY The top Y coordinate of the toolbar
     */
    fun updateOverlayStops(titlebarBottomY: Int, toolbarTopY: Int) {
        titlebarBottom = titlebarBottomY.coerceAtLeast(0)
        toolbarTop = toolbarTopY.coerceAtLeast(titlebarBottom)

        // build new gradient shader over grid with updated y1->y2 for grid
        // width/height from view
        buildShader(width, height)
        invalidate()
    }

    /**
     * Builds the overlay gradient shader.
     * @param unusedViewW The width of the view (unused)
     * @param viewH The height of the view
     *
     * @desc Creates a vertical linear gradient shader that fades the grid (overlayed on grid lines)
     *       between the title bar and toolbar areas.
     */
    private fun buildShader(unusedViewW: Int, viewH: Int) {
        // 80dp fade area
        val density = resources.displayMetrics.density
        val fade = 160f * density

        val cOpaque = ColorUtils.setAlphaComponent(bgColor, 200)
        val cClear = ColorUtils.setAlphaComponent(bgColor, 0)

        val colors = intArrayOf(
            cOpaque,         // 0 -> fully opaque
            cOpaque,         // topFadeStart -> still opaque
            cClear,          // start -> fully transparent
            cClear,          // end -> still transparent
            cOpaque,         // botFadeEnd -> fully opaque
            cOpaque          // 1 -> fully opaque
        )

        // gradient wants float positions
        val positions = floatArrayOf(
            0f,
            titlebarBottom.toFloat() / viewH,
            (titlebarBottom.toFloat() + fade ) / viewH,
            (toolbarTop.toFloat() - fade ) / viewH,
            (toolbarTop.toFloat() ) / viewH,
            1f
        )

        overlayPaint.shader = LinearGradient(
            0f, 0f, 0f, viewH.toFloat(),
            colors, positions, Shader.TileMode.CLAMP
        )
    }

    /**
     * Called when the size of the view changes.
     * @param w The new width of the view
     * @param h The new height of the view
     * @param oldw The old width of the view
     * @param oldh The old height of the view
     *
     * @desc Rebuilds the overlay gradient shader to fit the new size.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildShader(w, h)
    }

    /**
     * Draws the grid and overlay gradient on the canvas.
     * @param canvas The Canvas to draw on
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // vert lines
        var x = 0f
        while (x <= width) {
            canvas.drawLine(x + 0.5f, 0f, x + 0.5f, height.toFloat(), gridPaint)
            x += cell
        }

        // horiz lines
        var y = 0f
        while (y <= height) {
            canvas.drawLine(0f, y + 0.5f, width.toFloat(), y + 0.5f, gridPaint)
            y += cell
        }

        // draw overlay gradient on top of grid
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    }
}
