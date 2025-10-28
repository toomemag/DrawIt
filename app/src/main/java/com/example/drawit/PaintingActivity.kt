package com.example.drawit

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
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
import androidx.core.graphics.set
import com.example.drawit.painting.CanvasManager
import kotlin.math.abs
import kotlin.math.max
import com.example.drawit.painting.CanvasView
import com.example.drawit.painting.Layer
import com.example.drawit.painting.effects.EffectContext
import com.example.drawit.painting.effects.GyroscopeEffect
import com.google.android.material.button.MaterialButton
//import top.defaults.colorpicker.ColorPickerPopup
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


enum class CanvasGestureState {
    IDLE,
    PAINTING,
    GESTURE
}

class PaintingActivity : AppCompatActivity(), SensorEventListener {
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
    private var drawingState: CanvasGestureState = CanvasGestureState.IDLE

    // stored as pixel pos
    private var lastCanvasDrawPoint: Array<Int> = arrayOf(0, 0)
    private lateinit var effectContext: EffectContext

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

        effectContext = (application as DrawItApplication).effectManager.createContext()
        effectContext.addSensorListener<GyroscopeEffect>(Sensor.TYPE_GYROSCOPE) { effect, sensorEvent ->
            val ret = effect.translateSensorEvent(sensorEvent)

            val layers = canvasManager.getLayers()

            if (layers.size > 1 && canvasManager.getActiveLayerIndex() == -1) {
                layers[0].setPos(ret.y.toInt().coerceIn(-60, 60), ret.x.toInt().coerceIn(-60, 60))
                updateCanvasLayers()
            }
        }

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
                //       could implement a serializer in CanvasManager and store it locally
                //       would be overriden when a new painting is saved and one exists
                finish()
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            canvasManager.newLayerAction("Layer${canvasManager.getLayers().size + 1}")

            // refresh layer list
            syncLayersToView()
        }


        // DONE!: dark mode -> white text on white bg, popup background doesnt change based on system theme
        // TODO: choose colors for this type of menu.
        findViewById<MaterialButton>(R.id.colorPicker).setOnClickListener {

            ColorPickerDialog.Builder(this)
                .setTitle("Select a Color")
                .setPositiveButton("Select", ColorEnvelopeListener { envelope: ColorEnvelope, _ ->
                    val color = envelope.color
                    canvasManager.setColor(color)
                    findViewById<MaterialButton>(R.id.colorPicker).setIconTint(ColorStateList.valueOf(color))
                })

                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .show()
        }

        syncLayersToView()
        updateCanvasLayers()

        // canvas paint callback
        binding.canvas.setOnTouchListener { v, event ->
            // dragging comments:
            //  first finger is in canvas, zoom is doomed
            //  first finger outside of canvas, second finger inside, all fine.
            //
            // ideas:
            //  could only paint on POINTER_UP when no move happened
            //  that way we could wait for pinching gesture so we can cancel painting
            if (event.pointerCount > 1) {
                // cancel drawing action
                drawingState = CanvasGestureState.GESTURE

                // delegate to activity's onTouchEvent
                onTouchEvent(event)
                return@setOnTouchListener false
            }


            // reset gesture state when in gesture & last finger lifted
            if (event.pointerCount == 1 && drawingState == CanvasGestureState.GESTURE && event.actionMasked == MotionEvent.ACTION_UP) {
                drawingState = CanvasGestureState.IDLE
                return@setOnTouchListener false
            }

            // after gesturing we can't paint
            if (drawingState == CanvasGestureState.GESTURE) return@setOnTouchListener false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // painting on pointer up
                    drawingState = CanvasGestureState.PAINTING

                    // store paint pos
                    // think we could lose 1 pixel start accuracy if we don't do this
                    val layers = canvasManager.getLayers()
                    if (layers.isNotEmpty()) {
                        val pressedPos = getBitmapPaintPosFromCanvas(layers[0], event.x, event.y)

                        lastCanvasDrawPoint[0] = pressedPos[0]
                        lastCanvasDrawPoint[1] = pressedPos[1]
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // bug: sometimes when gesturing, first finger down moves and causes a paint
                    //      we can fix this by adding something similar to debounce
                    //      aka wait 200-300ms before allowing painting after finger was down
                    //      OR wait for distance moved to match at least a 1px threshold
                    onLayerPaint(v as CanvasView, event)
                    true
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // delegate to activity's onTouchEvent
                    //onTouchEvent(event)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    // paint last pixel if we we're drawing
                    if (drawingState == CanvasGestureState.PAINTING)
                        onLayerPaint(v as CanvasView, event)

                    // update previews (needs refactoring to refresh canvases, no need to delete and recreate)
                    syncLayersToView( )

                    drawingState = CanvasGestureState.IDLE

                    true
                }
                else -> {
                    v?.performClick()
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        effectContext.registerSensorListeners(this)
    }

    override fun onPause() {
        super.onPause()
        effectContext.unregisterSensorListeners(this)
    }

    private fun getBitmapPaintPosFromCanvas(layer: Layer, x: Float, y: Float): Array<Int> {
        val viewW = binding.canvas.width.toFloat().coerceAtLeast(1f)
        val viewH = binding.canvas.height.toFloat().coerceAtLeast(1f)

        val bmpW = layer.bitmap.width
        val bmpH = layer.bitmap.height

        val bmpX = ((x / viewW) * bmpW).toInt()
        val bmpY = ((y / viewH) * bmpH).toInt()

        val x = bmpX.coerceIn(0, bmpW - 1)
        val y = bmpY.coerceIn(0, bmpH - 1)

        return arrayOf(x, y)
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
            val bitmapPaintPos = getBitmapPaintPosFromCanvas(layer, event.x, event.y)
            val x = bitmapPaintPos[0]
            val y = bitmapPaintPos[1]

            // if we we're in a constant drawing state, interpolate between last known point and current
            if (drawingState == CanvasGestureState.PAINTING) {
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

                    layer.bitmap[interpX, interpY] = canvasManager.getColor()
                }
            }

            layer.bitmap[x, y] = canvasManager.getColor()

            lastCanvasDrawPoint[0] = x
            lastCanvasDrawPoint[1] = y

            binding.canvas.invalidateLayers()
            return true
        }
    }

    // layers row update
    private fun syncLayersToView() {
        // todo: could separate into addNewLayer and refreshLayers
        // right now clearing all older views and think this is performance overhead
        if (binding.layersContainer.childCount > 1)
            binding.layersContainer.removeViews(1, binding.layersContainer.childCount - 1)

        // scale to dp
        val layerPreviewSizePx = (80 * resources.displayMetrics.density).toInt()
        val borderWidthPx = resources.displayMetrics.density.toInt()

        // update all canvas layer previews
        for (layer in canvasManager.getLayers()) {
            val previewBitmap = androidx.core.graphics.createBitmap(layerPreviewSizePx, layerPreviewSizePx)
            val previewCanvas = Canvas(previewBitmap)
            layer.spewToCanvas(previewCanvas, layerPreviewSizePx, layerPreviewSizePx)

            // todo: light mode support
            var borderColor = 0xFF404040.toInt()
            if (layer.isActive) {
                borderColor = 0XFF808080.toInt()
            }

            val layerContainer = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = ViewGroup.MarginLayoutParams(
                    layerPreviewSizePx,
                    layerPreviewSizePx
                ).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                setBackgroundColor(borderColor)
            }

            val layerPreviewView = android.widget.ImageView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    topMargin = 4
                    bottomMargin = 4
                    leftMargin = 4
                    rightMargin = 4
                }

                setImageBitmap(previewBitmap)

                // border
                setBackgroundColor(android.graphics.Color.BLACK)
                setPadding(borderWidthPx, borderWidthPx, borderWidthPx, borderWidthPx)

                setOnClickListener {
                    val index = canvasManager.getLayers().indexOf(layer)
                    val activeIndex = canvasManager.getActiveLayerIndex()
                    if (index != -1) {
                        // bug: can click between layers and that selects no layer
                        if (index != activeIndex) {
                            // if we had no layer selected before -> in preview mode
                            // if we switch from preview to active layer (edit mode)
                            // layer pos offsets from event listeners stay
                            if (activeIndex == -1) {
                                // reset all layer offsets
                                for (l in canvasManager.getLayers()) {
                                    l.setPos(0, 0)
                                }
                            }
                            canvasManager.setActiveLayer(index)
                        } else {
                            canvasManager.setActiveLayer(-1)

                            // reset sensor data
                            // for gyro sets all pos fields to 0, otherwise phone rotation will
                            // be carried over preview switches
                            effectContext.resetAllEffects()
                        }

                        // update drawing area, if we switch layers without this here
                        // we would get stale active state inside canvas
                        // aka wrong layer (old active) rendered at full opacity
                        binding.canvas.invalidateLayers()

                        // update border colors (no better way to do it atm)
                        syncLayersToView()
                    }
                }
            }

            layerContainer.addView(layerPreviewView)

            // Debug logging
            layerContainer.post {
                android.util.Log.d("LayerDebug", "layerContainer: ${layerContainer.width}x${layerContainer.height}, measured: ${layerContainer.measuredWidth}x${layerContainer.measuredHeight}")
                android.util.Log.d("LayerDebug", "layerPreviewView: ${layerPreviewView.width}x${layerPreviewView.height}, measured: ${layerPreviewView.measuredWidth}x${layerPreviewView.measuredHeight}")
                android.util.Log.d("LayerDebug", "layerPreviewView layoutParams: ${layerPreviewView.layoutParams}")
            }

            // add after the "+" layer (which is at index 0)
            binding.layersContainer.addView(layerContainer, binding.layersContainer.childCount)
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

        // bug: zoom freaks  out if too zoomed in (i guess upper bound is hit?)
        // bug: ACTION_POINTER_DOWN isn't called when 2nd finger lands

        when ( event.actionMasked ) {
            MotionEvent.ACTION_POINTER_DOWN -> { // do we need ACTION_POINTER_DOWN aswell?
                // on press ( aka event down) we want to store the initial
                // touch position

                firstTouchDistance = hypot(
                    (event.getX(1) - event.getX(0)).toDouble(),
                    (event.getY(1) - event.getY(0)).toDouble()
                ).toFloat()

                lastTouchMidPoint[0] = (event.getX(0) + event.getX(1)) / 2f
                lastTouchMidPoint[1] = (event.getY(0) + event.getY(1)) / 2f

                android.util.Log.d( "PaintingActivity::onTouchEvent(2+gesture)", "ACTION_DOWN dist=${firstTouchDistance} midpoint=<${lastTouchMidPoint[0]},${lastTouchMidPoint[1]}>" )
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

                val scaleFactor = Math.clamp( currentDistance / firstTouchDistance, .2f, 5f)

                // does scale scale about mid or posxy?
                binding.canvasContainer.scaleX *= scaleFactor
                binding.canvasContainer.scaleY *= scaleFactor

                // reset for next move
                firstTouchDistance = currentDistance

                binding.canvasContainer.translationX = canvasOffset[0]
                binding.canvasContainer.translationY = canvasOffset[1]
            }
        }

        return super.onTouchEvent(event)
    }

    // sensor events
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        effectContext.onSensorChanged(sensorEvent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // from SensorEventListener, don't need it i think
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