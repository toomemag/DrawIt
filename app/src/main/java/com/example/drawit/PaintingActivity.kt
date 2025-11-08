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
import com.example.drawit.databinding.DialogAddedEffectsBinding
import com.example.drawit.databinding.DialogConfirmPaintingSubmitBinding
import com.example.drawit.databinding.DialogNewEffectBinding
import com.example.drawit.databinding.NodeEffectBinding
import com.example.drawit.painting.CanvasManager
import kotlin.math.abs
import kotlin.math.max
import com.example.drawit.painting.CanvasView
import com.example.drawit.painting.Layer
import com.example.drawit.painting.LayerTransformInput
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.painting.effects.EffectContext
import com.example.drawit.painting.effects.GyroscopeEffect
import com.example.drawit.ui.effects.EffectEditDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.util.Timer
import java.util.TimerTask


enum class CanvasGestureState {
    IDLE,
    PAINTING,
    GESTURE
}

/**
 * Main painting activity
 *
 * @property binding The view binding for the activity
 * @property canvasManager The manager for handling canvas layers and drawing
 * @property lastTouchMidPoint The last midpoint of touch events for gesture handling
 * @property firstTouchDistance The initial distance between touch points for gesture handling
 * @property canvasOffset The current offset of the canvas for panning
 * @property drawingState The current state of canvas gestures (idle, painting, or gesture)
 * @property lastCanvasDrawPoint The last point drawn on the canvas for interpolation
 * @property effectContext The context for managing sensor-based effects
 */
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

    private var drawingState: CanvasGestureState = CanvasGestureState.IDLE

    // stored as pixel pos
    private var lastCanvasDrawPoint: Array<Int> = arrayOf(0, 0)
    private lateinit var effectContext: EffectContext

    private var startTime: Long = System.currentTimeMillis()
    private var lastPausedTime: Long = 0L

    private fun isPaused(): Boolean {
        return lastPausedTime != 0L
    }

    private fun pausePaintingTimer() {
        lastPausedTime = System.currentTimeMillis()
    }

    private fun resumePaintingTimer() {
        // don't count paused time towards total time
        if (isPaused()) {
            val pausedTime = System.currentTimeMillis() - lastPausedTime
            startTime += pausedTime
            lastPausedTime = 0L
        }
    }

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

        // on pause painting click show pause popup
        findViewById<Button>(R.id.pausePainting).setOnClickListener {
            val pauseDialogBinding = DialogPausePaintingBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(pauseDialogBinding.root)
                .create()

            // button listeners
            pauseDialogBinding.ActivePaintingContinue.setOnClickListener {
                // back to painting
                dialog.dismiss()

                resumePaintingTimer()
            }

            pauseDialogBinding.ActivePaintingSaveExit.setOnClickListener {
                // close activity
                // todo: save painting
                //       could implement a serializer in CanvasManager and store it locally
                //       would be overriden when a new painting is saved and one exists
                //
                // todo: also autosaving, so 2 local saves (draft & autosave)
                finish()
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            pausePaintingTimer()
        }

        // submit dialog
        findViewById<MaterialButton>(R.id.submitPainting).setOnClickListener {
            val sumbitDialogBinding = DialogConfirmPaintingSubmitBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(sumbitDialogBinding.root)
                .create()

            sumbitDialogBinding.SubmitPaintingSubmit.setOnClickListener {
                val db = Firebase.firestore

                // should always be paused though, dialog pauses anyway
                val timeTakenSeconds = ((if (isPaused()) lastPausedTime - startTime else System.currentTimeMillis() - startTime) / 1000).toInt()

                db.collection("paintings").add(
                    canvasManager.serializeForFirebase(timeTakenSeconds)
                ).addOnSuccessListener { dr ->
                    android.util.Log.d("FirebaseDB", "DocumentSnapshot added with ID: ${dr.id}")
                    // push back to mainactivity

                    dialog.dismiss()
                    finish()
                }.addOnFailureListener { p0 ->
                    android.util.Log.e("FirebaseDB", "Error adding document", p0)
                }
            }

            sumbitDialogBinding.SubmitPaintingCancel.setOnClickListener {
                dialog.dismiss()
                resumePaintingTimer()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            pausePaintingTimer()
        }

        findViewById<TextView>(R.id.newLayer).setOnClickListener {
            canvasManager.newLayerAction("Layer${canvasManager.getLayers().size + 1}")

            // refresh layer list
            syncLayersToView()
        }


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

        // effects button takes us to current layer added effects dialog
        findViewById<Button>(R.id.effectsButton).setOnClickListener { openAddedEffectsDialog( ) }

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

        // update time taken
        val timer = Timer( )
        timer.scheduleAtFixedRate(object : TimerTask( ) {
            override fun run( ) {
                runOnUiThread {
                    val timeSincePaused = if (isPaused()) System.currentTimeMillis() - lastPausedTime else 0L

                    val currentTime = System.currentTimeMillis() - timeSincePaused
                    val timeDiffSeconds = ( currentTime - startTime ) / 1000

                    val minutes = timeDiffSeconds / 60
                    val seconds = timeDiffSeconds % 60

                    binding.titleTimer.text = String.format( "%d:%02d", minutes, seconds )
                }
            }
        }, 0, 1000)
    }

    /**
     * Open dialog showing added effects on current layer
     */
    private fun openAddedEffectsDialog() {
        // no layer no effects
        if (canvasManager.getActiveLayerIndex() == -1) return
        val layer = canvasManager.getLayer(canvasManager.getActiveLayerIndex())!!

        val dialogBinding = DialogAddedEffectsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.closeAddedEffects.setOnClickListener {
            dialog.dismiss()
        }

        if (layer.effectBindings.isNotEmpty()) {
            dialogBinding.noEffectsAddedText.height = 0

            for (effectBinding in layer.effectBindings) {
                val effectItem = NodeEffectBinding.inflate(this.layoutInflater, dialogBinding.effectsListView, false)
                val effect = (application as DrawItApplication).effectManager.getEffect(effectBinding.key)!!

                effectItem.root.findViewById<TextView>(R.id.effectName).text = effect.getEffectName()
                effectItem.root.findViewById<TextView>(R.id.effectDesc).text = effect.getEffectDescription()

                effectItem.root.setOnClickListener {
                    // close available dialog, open edit dialog
                    dialog.dismiss()

                    EffectEditDialog(this, layer, effect, onEffectRemoved = {
                        // refresh effect list since each layer can have one of each effect type
                        // we need to update available effects list to add current effect back
                        openAddedEffectsDialog()
                    }, onEffectEditClose = {
                        // reopen added effects dialog on edit close
                        openAddedEffectsDialog()
                    }).show()
                    //       and then layer serialization and backend (prob firebase)
                    //       and then lastly one draft save/load system locally
                }

                dialogBinding.effectsListView.addView(effectItem.root)
            }
        }

        dialogBinding.addNewEffect.setOnClickListener {
            dialog.dismiss()
            openEffectSelectionDialog()
        }

        dialog.show()
    }

    /**
     * Open dialog for selecting new effect to add to current layer
     */
    private fun openEffectSelectionDialog() {
        val dialogBinding = DialogNewEffectBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        val activeLayer = canvasManager.getLayer(canvasManager.getActiveLayerIndex())!!

        dialogBinding.closeNewEffectsDialog.setOnClickListener {
            dialog.dismiss()
            openAddedEffectsDialog()
        }

        val selectableEffects = (application as DrawItApplication).effectManager.getEffects().filter { effect -> !activeLayer.effectBindings.containsKey(effect.getEffectType()) }

        if (selectableEffects.isNotEmpty()) {
            dialogBinding.noEffectText.height = 0

            // dialogBinding.effectsListView
            // push new effect layout to list
            for (selectableEffect in selectableEffects) {
                // one binding for each effect
                if (activeLayer.effectBindings.containsKey(selectableEffect.getEffectType())) {
                    continue
                }

                val effectItem = NodeEffectBinding.inflate(this.layoutInflater, dialogBinding.effectsListView, false)

                effectItem.root.findViewById<TextView>(R.id.effectName).text = selectableEffect.getEffectName()
                effectItem.root.findViewById<TextView>(R.id.effectDesc).text = selectableEffect.getEffectDescription()

                effectItem.root.setOnClickListener {
                    // dismiss selection dialog
                    dialog.dismiss()

                    // added anyways, remove from selectable list
                    // listener handles when effect is deleted to put it back in the list
                    dialogBinding.effectsListView.removeView(effectItem.root)

                    // create new binding in layer
                    activeLayer.addEffectBinding(selectableEffect)
                    // we can pass effects as references because
                    // we're not directly modifying them
                    // we're creating a "middleware" type of binding system in layers
                    // so for each added effect there exists a key (effect sensor type)
                    // and a list of LayerEffectBindings

                    EffectEditDialog(this, activeLayer, selectableEffect, onEffectRemoved = {
                        // refresh effect selection list
                        openEffectSelectionDialog()
                    }, onEffectEditClose = {
                        // reopen added effects dialog on edit close
                        openEffectSelectionDialog()
                    }).show()
                }

                dialogBinding.effectsListView.addView(effectItem.root)
            }
        }

        dialog.show()
    }

    /**
     * Register sensor listeners on resume, method required by SensorEventListener
     */
    override fun onResume() {
        super.onResume()
        effectContext.registerSensorListeners(this)
    }

    /**
     * Unregister sensor listeners on pause, method required by SensorEventListener
     */
    override fun onPause() {
        super.onPause()
        effectContext.unregisterSensorListeners(this)
    }

    /**
     * Convert canvas touch position to bitmap paint position
     * @param layer The layer to get the bitmap from
     * @param x The x position on the canvas
     * @param y The y position on the canvas
     * @return The x and y position on the bitmap as an array
     */
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

    /**
     * Handle painting on the active layer
     * @param v The canvas view
     * @param event The motion event
     * @return True if the paint action was handled, false otherwise
     */
    private fun onLayerPaint(v: CanvasView, event: MotionEvent): Boolean {
        val activeIndex = canvasManager.getActiveLayerIndex()
        val layer = canvasManager.getLayer(activeIndex)

        if (layer == null) {
            // no active layer selected
            // todo: show toast? title should 100% only stay as theme
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

                // frames arent consistent, if we move a finger along the canvas
                // some pixels in between get skipped -> need to track last drawn point
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

    /**
     * Sync the canvas layers on the bottom to the layer preview views
     */
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
                            // no sensor events! we're not in preview mode
                            onPause()

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

                            // update all layers and their effects
                            updateAllLayerEffects()
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

    /**
     * Update the canvas view with the current layers from the canvas manager
     */
    private fun updateCanvasLayers() {
        binding.canvas.apply {
            layers = canvasManager.getLayers().reversed()
            invalidateLayers()
        }
    }

    // canvas resizing and moving
    /**
     * Handle touch events for canvas gestures (panning and zooming)
     * @param event The motion event
     * @return True if the event was handled, false otherwise
     *
     * @desc 2 bugs!
     *       - ACTION_POINTER_DOWN isn't called when 2nd finger lands
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount < 2) {
            super.onTouchEvent(event)
            return true
        }

        when ( event.actionMasked ) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // on press ( aka event down) we want to store the initial
                // touch position

                firstTouchDistance = hypot(
                    (event.getX(1) - event.getX(0)).toDouble(),
                    (event.getY(1) - event.getY(0)).toDouble()
                ).toFloat() * (1 / binding.canvasContainer.scaleX)

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

                // use distance from initial zoom, not accumulative
                // meaning if we take current zoom pos (eg 0.5x, first zoom pos 1x
                //      where x represents some units between pointer 1 and 2, not the scale)
                // new zoom = 0.5x = 1x / 0.5x
                // first 0.5x, current 2x, new zoom 2x / 0.5x
                val scaleFactor = Math.clamp( currentDistance / firstTouchDistance, .2f, 8f)

                binding.canvasContainer.scaleX = scaleFactor
                binding.canvasContainer.scaleY = scaleFactor

                binding.canvasContainer.translationX = canvasOffset[0]
                binding.canvasContainer.translationY = canvasOffset[1]
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * Handle sensor change events, method required by SensorEventListener
     * @param sensorEvent The sensor event
     *
     * @desc Delegates the sensor event to the effect context for processing.
     */
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        effectContext.onSensorChanged(sensorEvent)
    }

    /**
     * Handle sensor accuracy changes, method required by SensorEventListener
     * @param sensor The sensor
     * @param accuracy The new accuracy
     *
     * @desc unused method from SensorEventListener interface
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // from SensorEventListener, don't need it i think
    }

    /**
     * Update the overlay gradient stops on the grid background
     *
     * @desc Calculates the positions of the title bar and toolbar relative to the grid background
     *       and updates the gradient overlay stops accordingly to ensure proper rendering.
     */
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

    /**
     * Update all layer effects, called when no layer is selected
     */
    private fun updateAllLayerEffects() {
        // 1. reset effect states
        effectContext.resetAllEffects()

        // 2. remove old listeners
        effectContext.removeAllSensorListeners()

        // 3. get all layer effects
        val effectsToListen = mutableSetOf<Int>()

        val layers = canvasManager.getLayers()
        for (layer in layers) {
            for (effectBinding in layer.effectBindings) {
                val effectType = effectBinding.key
                effectsToListen.add(effectType)
            }
        }

        android.util.Log.d("EffectContext", "updateAllLayerEffects - effects to listen for: $effectsToListen")

        // 4. register all effects
        for (sensorType in effectsToListen) {
            val effect = (application as DrawItApplication).effectManager.getEffect(sensorType)
            if (effect != null) {
                android.util.Log.d("EffectContext", "updateAllLayerEffects - adding listener for: ${effect.getEffectName()}")
                // we need to add listeners to each effect
                effectContext.addSensorListener(effect.getEffectType()) { effect, sensorEvent ->
                    // first update the sensor inner value to use
                    // in transform
                    effect.translateSensorEvent(sensorEvent)

                    for (layer in layers) {
                        val bindings = layer.effectBindings[effect.getEffectType()] ?: continue

                        val layerAppliedTransforms = mutableSetOf<LayerTransformInput>()

                        // ok we have a binding
                        for (layerEffectBinding in bindings) {
                            // i hope I can find a cleaner/dynamic casting way
                            val hasAppliedTransform = layerAppliedTransforms.contains(layerEffectBinding.layerTransformInput)
                            when (effect) {
                                is GyroscopeEffect -> {
                                    val t = effect.getTransformInput(
                                        layerEffectBinding.effectInputIndex
                                    )

                                    layer.applyEffectTranslation(t, layerEffectBinding.layerTransformInput, hasAppliedTransform)
                                }
                                else -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val t = (effect as BaseEffect<Int>).getTransformInput(
                                        layerEffectBinding.effectInputIndex
                                    )

                                    layer.applyEffectTranslation(t, layerEffectBinding.layerTransformInput, hasAppliedTransform)
                                }
                            }

                            layerAppliedTransforms.add(layerEffectBinding.layerTransformInput)
                        }
                    }

                    // update canvas when listener is done
                    updateCanvasLayers()
                }
            } else {
                android.util.Log.w("EffectContext", "updateAllLayerEffects - no effect found for sensor type $sensorType")
            }
        }

        // re-register listeners
        onResume()
    }
}