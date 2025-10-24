package com.example.drawit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
        // Add placeholder layers
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
    };

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
    var paint = android.graphics.Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all visible layers
        for (i in layers.indices) {
            val layer = layers[i]

            if (layer.isActive)
                paint.alpha = 255
            else paint.alpha = 55

            canvas.drawBitmap(layer.bitmap, 0f, 0f, paint)
        }
    }

    fun invalidateLayers() {
        invalidate()
    }
}


class PaintingActivity : AppCompatActivity() {
    // binding for activity_painting.xml
    private lateinit var binding: ActivityPaintingBinding;
    // whole canvas manager
    // handles all drawing related actions
    private val canvasManager = CanvasManager()

    // touch handling vars
    private var lastTouchMidPoint: Array<Float> = arrayOf(0f, 0f)
    private var firstTouchDistance: Float = 0f
    private var canvasOffset: Array<Float> = arrayOf(0f, 0f)

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

            // refresh layer list
            syncLayersToView( )
        }

        syncLayersToView( );
        updateCanvasLayers( )
    }

    private fun syncLayersToView( ) {
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
                        if (index != activeIndex)
                            canvasManager.setActiveLayer(index)
                        else canvasManager.setActiveLayer(-1);
                    }
                }
            }

            // before "+" layer
            binding.layersContainer.addView(layerView, binding.layersContainer.childCount - 1)
        }

        updateCanvasLayers( )
    }

    private fun updateCanvasLayers() {
        ( binding.canvas as CanvasView ).apply {
            layers = canvasManager.getLayers()
            invalidateLayers()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // todo: canvas doesnt get moved or scaled
        if (event.pointerCount < 2) {
            super.onTouchEvent(event);
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
