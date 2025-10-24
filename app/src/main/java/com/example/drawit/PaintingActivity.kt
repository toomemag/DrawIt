package com.example.drawit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.drawit.databinding.ActivityPaintingBinding
import com.example.drawit.databinding.DialogPausePaintingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.hypot
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import kotlin.math.abs
import kotlin.math.max

data class Layer(
    // debug names more than anything
    val name: String = "Layer",
    var bitmap: Bitmap = createBitmap(128, 128),
    // cant access canvasmanager from activity view
    var isActive: Boolean = true
)

class CanvasManager {
    private val layers = mutableListOf<Layer>()

    init {
        layers.add(Layer(name = "Layer1"))
    }

    fun setActiveLayer(index: Int) {
        if (index == -1) {
            // disable all layers
            for (layer in layers) layer.isActive = false
        } else {
            for (i in layers.indices) {
                layers[i].isActive = (i == index)
            }
        }
    }

    fun getActiveLayerIndex(): Int {
        for (layerIdx in layers.indices) {
            if (layers[layerIdx].isActive) {
                return layerIdx
            }
        }
        return -1
    }

    fun getLayers(): List<Layer> = layers.toList()

    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    fun removeLayer(layer: Layer) {
        layers.remove(layer)
    }

    fun getLayer(index: Int): Layer? = layers.getOrNull(index)
}


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
}


class PaintingActivity : AppCompatActivity() {
    // binding for activity_painting.xml
    private lateinit var binding: ActivityPaintingBinding
    // whole canvas manager
    // handles all drawing related actions
    private val canvasManager = CanvasManager()

    // touch handling vars
    private var lastTouchMidPoint: Array<Float> = arrayOf(0f, 0f)
    private var firstTouchDistance: Float = 0f
    private var canvasOffset: Array<Float> = arrayOf(0f, 0f)

    // frames arent consistent, if we move a finger along the canvas
    // some pixels in between get skipped -> need to track last drawn point
    private var wasDrawing: Boolean = false
    // stored as pixel pos
    private var lastCanvasDrawPoint: Array<Int> = arrayOf(0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaintingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Draw behind system bars and handle insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Ensure no app bar is shown even if theme is overridden
        supportActionBar?.hide()

        val mode = intent.getStringExtra("mode") ?: "free_mode"
        binding.titleText.text = if (mode == "daily_theme") "Theme" else "Freemode"

        // titlebar would be below the phone's status bar, so add padding to compensate
        val originalTitlebarPaddingTop = binding.titlebar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.titlebar) { v, insets ->
            // add status bar height to titlebar padding top
            val sb = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            // set new padding
            v.setPadding(v.paddingLeft, originalTitlebarPaddingTop + sb, v.paddingRight, v.paddingBottom)
            // gradient shader update
            v.post { updateOverlayStops() }
            insets
        }

        // initial run on first layout
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // remove old listener
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // run shader update
                updateOverlayStops()
            }
        })

        // whenever any of these views have a layout change -> update shader stops
        val relayoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateOverlayStops() }
        binding.titlebar.addOnLayoutChangeListener(relayoutListener)
        binding.toolbar.addOnLayoutChangeListener(relayoutListener)
        binding.gridBackground.addOnLayoutChangeListener(relayoutListener)

        // on new painting click show a custom rendered popup
        findViewById<Button>(R.id.pausePainting).setOnClickListener {
            val dialogBinding = DialogPausePaintingBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.root)
                .create()

            // button listeners
            dialogBinding.ActivePaintingContinue.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.ActivePaintingSaveExit.setOnClickListener {
                // close activity
                // todo: save painting
                finish()
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            val newLayer = Layer(name = "Layer${canvasManager.getLayers().size + 1}")
            canvasManager.addLayer(newLayer)
            // set created layer as active (felt a bit more intuitive rather than clicking new and then selecting the layer)
            canvasManager.setActiveLayer(canvasManager.getLayers().size - 1)

            // refresh layer list
            syncLayersToView()
        }

        syncLayersToView()
        updateCanvasLayers()

        // canvas paint callback
        // todo: fix "Custom view `CanvasView` has setOnTouchListener called on it but does not override performClick"
        binding.canvas.setOnTouchListener { v, event ->
            // todo: >1 finger gesture crashes app, could be from here or from view event listener
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onLayerPaint(v as CanvasView, event)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 1) {
                        wasDrawing = true
                        onLayerPaint(v as CanvasView, event)
                    }
                    true
                }
                else -> {
                    // ACTION_UP & whatever, just cancel drawing state
                    wasDrawing = false
                    v?.performClick()
                    false
                }
            }
        }
    }

    private fun onLayerPaint(v: CanvasView, event: MotionEvent): Boolean {
        val activeIndex = canvasManager.getActiveLayerIndex()
        val layer = canvasManager.getLayer(activeIndex)

        if (layer == null) {
            // no active layer selected
            binding.titleText.text = "No active layer"
            // report click
            v.performClick()
            return false
        } else {
            // event.x/y are in view (CanvasView) coordinates
            val viewW = binding.canvas.width.toFloat().coerceAtLeast(1f)
            val viewH = binding.canvas.height.toFloat().coerceAtLeast(1f)
            val bmpW = layer.bitmap.width
            val bmpH = layer.bitmap.height

            val bmpX = ((event.x / viewW) * bmpW).toInt()
            val bmpY = ((event.y / viewH) * bmpH).toInt()

            val x = bmpX.coerceIn(0, bmpW - 1)
            val y = bmpY.coerceIn(0, bmpH - 1)

            // if we we're in a constant drawing state, interpolate between last known point and current
            if (wasDrawing) {
                val lastX = lastCanvasDrawPoint[0]
                val lastY = lastCanvasDrawPoint[1]

                // if its negative y, smaller x/y steps will be used -> causes gaps and defeats the whole purpose of lerping
                val distX = abs(x - lastX)
                val distY = abs(y - lastY)

                // we have max steps as the longest distance we've travelled in either
                // x or y axis
                val steps = max(distX, distY)

                for (i in 1..steps) {
                    val t = i.toFloat() / steps.toFloat()
                    val interpX = (lastX + t * (x - lastX)).toInt()
                    val interpY = (lastY + t * (y - lastY)).toInt()

                    layer.bitmap[interpX, interpY] = 0xFFFFFFFF.toInt()
                }
            }

            layer.bitmap[x, y] = 0xFFFFFFFF.toInt()

            lastCanvasDrawPoint[0] = x
            lastCanvasDrawPoint[1] = y

            binding.canvas.invalidateLayers()
            return true
        }
    }

    private fun syncLayersToView() {
        binding.layersContainer.removeViews(0, binding.layersContainer.childCount - 1)

        for (layer in canvasManager.getLayers()) {
            val layerView = TextView(this).apply {
                text = layer.name
                textSize = 18f
                setPadding(20, 20, 20, 20)
                setBackgroundColor( 0x22000000 )
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    val index = canvasManager.getLayers().indexOf(layer)
                    val activeIndex = canvasManager.getActiveLayerIndex()
                    if (index != -1) {
                        // bug: can click between layers and that selects no layer
                        if (index != activeIndex)
                            canvasManager.setActiveLayer(index)
                        else canvasManager.setActiveLayer(-1)

                        // update drawing area, if we switch layers without this here
                        // we would get stale active state inside canvas
                        // aka wrong layer (old active) rendered at full opacity
                        binding.canvas.invalidateLayers()
                    }
                }
            }

            // before "+" layer
            binding.layersContainer.addView(layerView, binding.layersContainer.childCount - 1)
        }

        updateCanvasLayers()
    }

    private fun updateCanvasLayers() {
        binding.canvas.apply {
            layers = canvasManager.getLayers()
            invalidateLayers()
        }
    }

    // canvas resizing and moving
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // todo: canvas doesnt get moved or scaled
        if (event.pointerCount < 2) {
            super.onTouchEvent(event)
            return true
        }

        when ( event.actionMasked ) {
            MotionEvent.ACTION_DOWN -> { // do we need ACTION_POINTER_DOWN aswell?
                // on press ( aka event down) we want to store the initial
                // touch position

                firstTouchDistance = hypot(
                    (event.getX(1) - event.getX(0)).toDouble(),
                    (event.getY(1) - event.getY(0)).toDouble()
                ).toFloat()

                lastTouchMidPoint[0] = (event.getX(0) + event.getX(1)) / 2f
                lastTouchMidPoint[1] = (event.getY(0) + event.getY(1)) / 2f
            }
            MotionEvent.ACTION_MOVE -> {
                // canvas pos
                val newX = (event.getX(0) + event.getX(1)) / 2f
                val newY = (event.getY(0) + event.getY(1)) / 2f

                val deltaX = newX - lastTouchMidPoint[0]
                val deltaY = newY - lastTouchMidPoint[1]

                // we got move offset from last touch
                canvasOffset[0] += deltaX
                canvasOffset[1] += deltaY

                // update touch!
                lastTouchMidPoint[0] = newX
                lastTouchMidPoint[1] = newY

                // canvas zoom
                val currentDistance = hypot(
                    (event.getX(1) - event.getX(0)).toDouble(),
                    (event.getY(1) - event.getY(0)).toDouble()
                ).toFloat()

                val scaleFactor = currentDistance / firstTouchDistance

                // does scale scale about mid or posxy?
                binding.canvas.scaleX *= scaleFactor
                binding.canvas.scaleY *= scaleFactor

                // reset for next move
                firstTouchDistance = currentDistance

                binding.canvas.translationX = canvasOffset[0]
                binding.canvas.translationY = canvasOffset[1]
            }
        }

        return super.onTouchEvent(event)
    }

    // grid background update
    private fun updateOverlayStops() {
        // get positions to draw gradient (hides grid) correctly
        val gridLoc = IntArray(2)
        val titleLoc = IntArray(2)
        val toolbarLoc = IntArray(2)
        binding.gridBackground.getLocationOnScreen(gridLoc)
        binding.titlebar.getLocationOnScreen(titleLoc)
        binding.toolbar.getLocationOnScreen(toolbarLoc)

        // ypos relative to grid
        val titlebarBottomY = titleLoc[1] + binding.titlebar.height
        val toolbarTopY = toolbarLoc[1]

        // clamp if needed (shouldnt be necessary, but just in case)
        val start = titlebarBottomY.coerceIn(0, binding.gridBackground.height)
        val end = toolbarTopY.coerceIn(start, binding.gridBackground.height)
        binding.gridBackground.updateOverlayStops(start, end)
    }
}