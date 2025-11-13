package com.example.drawit.ui.viewmodels

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.set
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawit.painting.CanvasManager
import com.example.drawit.painting.CanvasView
import com.example.drawit.painting.Layer
import com.example.drawit.painting.LayerTransformInput
import com.example.drawit.painting.PaintTool
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.painting.effects.EffectManager
import com.example.drawit.painting.effects.GyroscopeEffect
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import java.util.Timer
import java.util.TimerTask

enum class CanvasGestureState {
    IDLE,
    PAINTING,
    GESTURE
}

class NewPaintingViewModel(
    // null so we can have previews
    val effectManager: EffectManager? = null
) : ViewModel( ) {
    private val canvasManager = CanvasManager()
    private val effectContext = effectManager?.createContext()

    private val _layers = MutableStateFlow<List<Layer>>(emptyList())
    val layers = _layers.asStateFlow()

    // null -> no layer, doesn't really matter, looks cleaner than idx -1
    private val _activeLayerIndex = MutableStateFlow<Int?>(null)
    val activeLayerIndex = _activeLayerIndex.asStateFlow()

    // lowk should refactor to graphics.Color
    private val _selectedColor = MutableStateFlow(Color(0xFFFFFFFF.toInt()))
    val selectedColor = _selectedColor.asStateFlow()

    private val _timeElapsedSeconds = MutableStateFlow(0)
    val timeElapsedSeconds = _timeElapsedSeconds.asStateFlow()

    private val _paintingSubmitResult = MutableSharedFlow<Result<String>?>(replay = 0)
    val paintingSubmitResult: SharedFlow<Result<String>?> = _paintingSubmitResult.asSharedFlow()

    private val _drawingState = MutableStateFlow(CanvasGestureState.IDLE)
    val drawingState = _drawingState.asStateFlow()

    private var _currentTool = MutableStateFlow(canvasManager.getTool())
    val currentTool = _currentTool.asStateFlow()


    private val _isColorpickerOpen = MutableStateFlow(false)
    val isColorpickerOpen = _isColorpickerOpen.asStateFlow()

    private val _isLayerEffectsDialogOpen = MutableStateFlow(false)
    val isLayerEffectsDialogOpen = _isLayerEffectsDialogOpen.asStateFlow()

    // current layer effects -> clicked add new
    private val _isNewEffectDialogOpen = MutableStateFlow(false)
    val isNewEffectDialogOpen = _isNewEffectDialogOpen.asStateFlow()
    // current layer effects -> clicked existing binding
    // null -> closed
    private val _editBindingDialog = MutableStateFlow<BaseEffect<*>?>(null)
    val editBindingDialog = _editBindingDialog.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()


    // internal vars, not exposed to UI
    // touch handling vars
    private var lastTouchMidPoint: Array<Float> = arrayOf(0f, 0f)
    private var firstTouchDistance: Float = 0f
    private var canvasOffset: Array<Float> = arrayOf(0f, 0f)

    // stored as pixel pos
    private var lastCanvasDrawPoint: Array<Int> = arrayOf(0, 0)

    // drawing timer
    private var startTime: Long = System.currentTimeMillis()

    // instead of 0 we have null (not paused state)
    private var lastPausedTime: Long? = null

    // not sure we can avoid the leak, unless we do painting logic outside of viewmodel
    @SuppressLint("StaticFieldLeak")
    private var canvasViewRef: CanvasView? = null

    fun openColorpicker() { _isColorpickerOpen.value = true }
    fun closeColorpicker() { _isColorpickerOpen.value = false }

    fun openLayerEffectsDialog() { _isLayerEffectsDialogOpen.value = true }
    fun closeLayerEffectsDialog() { _isLayerEffectsDialogOpen.value = false }

    fun openNewEffectDialog() { _isNewEffectDialogOpen.value = true }
    fun closeNewEffectDialog() { _isNewEffectDialogOpen.value = false }

    fun openBindingDialog(effect: BaseEffect<*>) { _editBindingDialog.value = effect }
    fun closeBindingDialog() { _editBindingDialog.value = null }

    fun getAvailableEffects(): List<BaseEffect<*>> { return effectManager?.getEffects()?.toList() ?: emptyList() }

    fun setLastCanvasDrawPoint(pos: Pair<Int, Int>) {
        lastCanvasDrawPoint[0] = pos.first
        lastCanvasDrawPoint[1] = pos.second
    }

    fun pausePainting() {
        _isPaused.value = true
        lastPausedTime = System.currentTimeMillis()
    }

    fun getTool(): PaintTool {
        return _currentTool.value
    }

    fun getBrushSize(): Int {
        return canvasManager.getBrushSize()
    }

    fun setTool(tool: PaintTool) {
        _currentTool.value = tool
        canvasManager.setTool(tool)
    }

    fun setBrushSize( size: Int ) { canvasManager.setBrushSize(size) }

    fun resumePainting() {
        // don't count paused time towards total time
        if (isPaused.value) {
            val pausedDuration = System.currentTimeMillis() - lastPausedTime!!
            startTime += pausedDuration
            lastPausedTime = 0L
            _isPaused.value = false
        }
    }

    fun getEffectsFromTypes(effectTypes: Set<Int>): List<BaseEffect<*>> {
        val effects = mutableListOf<BaseEffect<*>>()

        for (type in effectTypes) {
            val effect = effectManager?.getEffect(type)
            if (effect != null) {
                effects.add(effect)
            }
        }

        return effects
    }

    fun setGestureState(state: CanvasGestureState) {
        _drawingState.value = state
    }

    fun registerSensorListeners(sensorEventListener: SensorEventListener) {
        effectContext?.registerSensorListeners(sensorEventListener)
    }

    fun unregisterSensorListeners(sensorEventListener: SensorEventListener) {
        effectContext?.unregisterSensorListeners(sensorEventListener)
    }

    init {
        // invalidate layers for selected layer to show up on init
        syncLayersToState()

        val timer = Timer( )
        timer.scheduleAtFixedRate(object : TimerTask( ) {
            override fun run( ) {
                viewModelScope.launch(Dispatchers.Main) {
                    val timeSincePaused = if (isPaused.value) System.currentTimeMillis() - lastPausedTime!! else 0L

                    val currentTime = System.currentTimeMillis() - timeSincePaused
                    val timeDiffSeconds = ( currentTime - startTime ) / 1000

                    _timeElapsedSeconds.value = timeDiffSeconds.toInt()
                }
            }
        }, 0, 1000)
    }

    // same as syncLayersToView before, instead now we set state
    // and update in screen
    fun syncLayersToState() {
        // quite similar to how vue handles reactive refs
        _layers.value = canvasManager.getLayers()
        _activeLayerIndex.value = canvasManager.getActiveLayerIndex()

        viewModelScope.launch(Dispatchers.Main) {
            canvasViewRef?.layers = _layers.value
            canvasViewRef?.invalidateLayers()
        }
    }

    fun updatePreviewsAndSyncLayersToState() {
        android.util.Log.d("NewPaintingViewModel", "updatePreviewsAndSyncLayersToState - updating previews")
        for (layer in canvasManager.getLayers()) {
            layer.lastUpdatedTimestamp = System.currentTimeMillis()
        }
        syncLayersToState()
    }

    fun newLayerAction() {
        canvasManager.newLayerAction("Layer${canvasManager.getLayers().size + 1}")
        syncLayersToState()
    }

    fun setColor(color: Color) {
        canvasManager.setColor(color.toArgb())
        _selectedColor.value = color
    }

    fun setActiveLayer(index: Int?) {
        canvasManager.setActiveLayer(index)
        syncLayersToState()
    }

    fun onCanvasViewCreated(canvasView: CanvasView) {
        android.util.Log.d( "CanvasView", "onCanvasViewCreated - got canvas view ref" )

        canvasViewRef = canvasView
        syncLayersToState()
    }

    /**
     * Convert canvas touch position to bitmap paint position
     * @param layerIndex The layer index to get the bitmap from
     * @param viewSize The size of the canvas view as a pair (width, height)
     * @param canvasPos The position on the canvas as a pair (x, y)
     * @return The x and y position on the bitmap as a pair (x, y)
     */
    fun getBitmapPaintPosFromCanvas(layerIndex: Int, viewSize: Pair<Int, Int>, canvasPos: Pair<Int, Int>): Pair<Int, Int> {
        val viewW = viewSize.first.coerceAtLeast(1).toFloat()
        val viewH = viewSize.second.coerceAtLeast(1).toFloat()

        val layer = canvasManager.getLayer(layerIndex) ?: return Pair(0, 0)

        val bmpW = layer.bitmap.width
        val bmpH = layer.bitmap.height

        // 0..1 from canvas clicked pos * bitmap size => bitmap pos
        var bmpX = ((canvasPos.first / viewW) * bmpW).toInt()
        var bmpY = ((canvasPos.second / viewH) * bmpH).toInt()

        bmpX = bmpX.coerceIn(0, bmpW - 1)
        bmpY = bmpY.coerceIn(0, bmpH - 1)

        return Pair(bmpX, bmpY)
    }

    /**
     * Handle painting on the active layer
     * @param layerIndex The index of the layer to paint on
     * @param layerPos The position on the layer bitmap to paint at as a pair (x, y)
     */
    fun paintAt(layerIndex: Int, layerPos: Pair<Int, Int>, isPointerDown: Boolean = true) {
        val layer = canvasManager.getLayer(layerIndex) ?: return

        // same dispatcher as from API example
        // although this time we're not dealing with async
        viewModelScope.launch(Dispatchers.Default) {
            // if we we're in a constant drawing state, interpolate between last known point and current
            if (drawingState.value == CanvasGestureState.PAINTING) {
                val lastX = lastCanvasDrawPoint[0]
                val lastY = lastCanvasDrawPoint[1]

                // if its negative y, smaller x/y steps will be used -> causes gaps and defeats the whole purpose of lerping
                val distX = abs(layerPos.first - lastX)
                val distY = abs(layerPos.second - lastY)

                // we have max steps as the longest distance we've travelled in either
                // x or y axis
                val steps = max(distX, distY)

                for (i in 0..steps) {
                    val t = if (steps == 0) 0f else i.toFloat() / steps.toFloat()
                    val interpX = (1 - t) * lastX + t * layerPos.first
                    val interpY = (1 - t) * lastY + t * layerPos.second

                    layer.bitmap[interpX.toInt(), interpY.toInt()] = canvasManager.getColor()
                }
            }

            layer.bitmap[layerPos.first, layerPos.second] = canvasManager.getColor()

            lastCanvasDrawPoint[0] = layerPos.first
            lastCanvasDrawPoint[1] = layerPos.second

            android.util.Log.d( "Painting", "paintAt - painted at (${layerPos.first}, ${layerPos.second})" )

            // update layer state
            withContext(Dispatchers.Main) {
                if (!isPointerDown) {
                    updatePreviewsAndSyncLayersToState()
                } else {
                    syncLayersToState()
                }
            }
        }

    }

    fun getLayerBitmap(layerIndex: Int): Bitmap? {
        val layer = canvasManager.getLayer(layerIndex) ?: return null
        return layer.bitmap
    }


    /**
     * Update all layer effects, called when no layer is selected
     */
    fun updateAllLayerEffects() {
        // 1. reset effect states
        effectContext?.resetAllEffects()

        // 2. remove old listeners
        effectContext?.removeAllSensorListeners()

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
            val effect = effectManager?.getEffect(sensorType)

            if (effect != null) {
                android.util.Log.d(
                    "EffectContext",
                    "updateAllLayerEffects - adding listener for: ${effect.getEffectName()}"
                )

                // we need to add listeners to each effect
                effectContext?.addSensorListener(effect.getEffectType()) { effect, sensorEvent ->
                    // first update the sensor inner value to use
                    // in transform
                    effect.translateSensorEvent(sensorEvent)

                    for (layer in layers) {
                        val bindings = layer.effectBindings[effect.getEffectType()] ?: continue

                        val layerAppliedTransforms = mutableSetOf<LayerTransformInput>()

                        // ok we have a binding
                        for (layerEffectBinding in bindings) {
                            // i hope I can find a cleaner/dynamic casting way
                            val hasAppliedTransform =
                                layerAppliedTransforms.contains(layerEffectBinding.layerTransformInput)
                            when (effect) {
                                is GyroscopeEffect -> {
                                    val t = effect.getTransformInput(
                                        layerEffectBinding.effectOutputIndex
                                    )

                                    layer.applyEffectTranslation(
                                        t,
                                        layerEffectBinding.layerTransformInput,
                                        hasAppliedTransform
                                    )
                                }

                                else -> {
                                    @Suppress("UNCHECKED_CAST")
                                    val t = (effect as BaseEffect<Int>).getTransformInput(
                                        layerEffectBinding.effectOutputIndex
                                    )

                                    layer.applyEffectTranslation(
                                        t,
                                        layerEffectBinding.layerTransformInput,
                                        hasAppliedTransform
                                    )
                                }
                            }

                            layerAppliedTransforms.add(layerEffectBinding.layerTransformInput)
                        }
                    }

                    // update canvas when listener is done
                    viewModelScope.launch(Dispatchers.Main) {
                        syncLayersToState()
                    }
                }
            } else {
                android.util.Log.w(
                    "EffectContext",
                    "updateAllLayerEffects - no effect found for sensor type $sensorType"
                )
            }
        }

        // todo: reregister all sensors
    }

    /**
     * Handle sensor change events, passed from activity
     * @param sensorEvent The sensor event
     *
     * @desc Delegates the sensor event to the effect context for processing.
     */
    fun onSensorEvent(sensorEvent: SensorEvent) {
        effectContext?.onSensorChanged(sensorEvent = sensorEvent)
    }

    fun serializeForFirebase(timeTakenSeconds: Int): Map<String, Any> {
        return canvasManager.serializeForFirebase(timeTakenSeconds)
    }

    suspend fun submitPainting(timeTakenSeconds: Int) {
        try {
            val db = Firebase.firestore
            val dr = db.collection("paintings")
                .add(serializeForFirebase(timeTakenSeconds)).await()

            _paintingSubmitResult.emit(Result.success(dr.id))
        } catch (e: Exception) {
            _paintingSubmitResult.emit(Result.failure(e))
        }
    }
}