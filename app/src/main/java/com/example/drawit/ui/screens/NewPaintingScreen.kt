package com.example.drawit.ui.screens

import android.graphics.Bitmap
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.drawit.painting.CanvasView
import com.example.drawit.painting.PaintTool
import com.example.drawit.ui.components.painting.ToolButton
import com.example.drawit.ui.dialogs.AvailableEffectsDialog
import com.example.drawit.ui.dialogs.LayerEffectsDialog
import com.example.drawit.ui.dialogs.ColorpickerDialog
import com.example.drawit.ui.dialogs.EffectBindingDialog
import com.example.drawit.ui.dialogs.PausePainting
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.ui.viewmodels.CanvasGestureState
import com.example.drawit.ui.viewmodels.NewPaintingViewModel
import com.example.drawit.utils.dynamicLightenDarken
import com.example.drawit.utils.invert
import com.example.drawit.utils.modify
import kotlinx.coroutines.launch


@Composable
fun NewPaintingScreen(
    viewmodel: NewPaintingViewModel,
    modifier: Modifier = Modifier.fillMaxSize(),
    canvasSizeDp: Dp  = 380.dp,

    onPostSubmit: () -> Unit = { },
    onPostSaveAndExit: () -> Unit = { },
) {
    val layers by viewmodel.layers.collectAsState()
    val activeIndex by viewmodel.activeLayerIndex.collectAsState()
    val timeElapsedSeconds by viewmodel.timeElapsedSeconds.collectAsState()
    val selectedColor by viewmodel.selectedColor.collectAsState()
    val drawingState by viewmodel.drawingState.collectAsState()

    val currentTool by viewmodel.currentTool.collectAsState()

    val colorpickerOpen by viewmodel.isColorpickerOpen.collectAsState()
    val isPaintingPaused by viewmodel.isPaused.collectAsState()
    val isLayerEffectsOpen by viewmodel.isLayerEffectsDialogOpen.collectAsState()
    val isNewEffectDialogOpen by viewmodel.isNewEffectDialogOpen.collectAsState()
    val editBindingDialog by viewmodel.editBindingDialog.collectAsState()
    val scope = rememberCoroutineScope()

    if (isSystemInDarkTheme()) {
        viewmodel.setColor(Color(0xFFffffff))
    } else {
        viewmodel.setColor(Color(0xFF000000))
    }

    DrawitTheme {
        Box(
            modifier = modifier.background(
                MaterialTheme.colorScheme.background
            ).fillMaxSize(),
        ) {
            when (colorpickerOpen) {
                true -> {
                    ColorpickerDialog(
                        onColorPicked = { color ->
                            viewmodel.setColor(color)
                        },
                        onDismissRequest = {
                            viewmodel.closeColorpicker()
                        },
                        initialColor = selectedColor
                    )
                }

                else -> {}
            }

            when (isPaintingPaused) {
                true -> {
                    PausePainting(
                        hasDrawn = viewmodel.hasDrawn(),
                        onSaveAndExit = {
                            viewmodel.savePaintingToLocalDatabase()

                            onPostSaveAndExit()
                        },
                        onBackToPainting = {
                            viewmodel.resumePainting()
                        },
                    )
                }

                else -> {}
            }

            when (isLayerEffectsOpen) {
                true -> {
                    val bindings = layers[viewmodel.activeLayerIndex.value ?: 0].effectBindings
                    val effects = viewmodel.getEffectsFromTypes(bindings.keys)

                    LayerEffectsDialog(
                        onDismiss = {
                            viewmodel.closeLayerEffectsDialog()
                        },
                        effects = effects,
                        onSelectEffect = { effect ->
                            // close current dialog, open effect dialog
                            viewmodel.closeLayerEffectsDialog()
                            viewmodel.openBindingDialog(effect)
                        },
                        onAddEffectClick = {
                            // close current dialog, open available effects dialog
                            viewmodel.closeLayerEffectsDialog()
                            viewmodel.openNewEffectDialog()
                        }
                    )
                }

                else -> {}
            }

            when (isNewEffectDialogOpen) {
                true -> {
                    if (activeIndex == null) {
                        // should not happen, close dialog
                        android.util.Log.d("NewPaintingScreen", "isNewEffectDialogOpen - active layer is null, closing dialog")
                        viewmodel.closeNewEffectDialog()
                    } else {
                        val activeLayer = layers[activeIndex!!]

                        // get all types we already have for layer
                        val bindings = activeLayer.effectBindings

                        // only show bindings that don't exist for given layer
                        val availableEffects = viewmodel.getAvailableEffects().filter { effect ->
                            !bindings.containsKey(effect.getEffectType())
                        }

                        AvailableEffectsDialog(
                            availableEffects = availableEffects,
                            onEffectSelected = { effect ->
                                // add effect to layer
                                activeLayer.addEffectBinding(effect)

                                // close current
                                viewmodel.closeNewEffectDialog()
                                // open effect binding dialog
                                viewmodel.openBindingDialog(effect)
                            },
                            onDismiss = {
                                viewmodel.closeNewEffectDialog()
                            }
                        )
                    }

                }

                else -> {}
            }

            when (editBindingDialog != null) {
                true -> {
                    if (activeIndex == null) {
                        // should not happen, close dialog
                        android.util.Log.d("NewPaintingScreen", "editBindingDialog - active layer is null, closing dialog")
                        viewmodel.closeBindingDialog()
                    } else {
                        val effect = editBindingDialog!!
                        val layer = layers[activeIndex!!]

                        EffectBindingDialog(
                            effect = effect,
                            layer = layer,
                            onDismiss = {
                                viewmodel.closeBindingDialog()
                                viewmodel.openLayerEffectsDialog()
                            }
                        )
                    }
                } else -> {}
            }

//        AndroidView(factory = { ctx ->
//            GridBackgroundView(ctx).apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//            }
//        }, modifier = Modifier.matchParentSize())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(6.dp, 32.dp, 6.dp, 6.dp)
                    .align(Alignment.TopCenter)
            ) {
                // title row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // fill all available space, justify-between type
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // back
                        IconButton(
                            onClick = {
                                viewmodel.pausePainting()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Text(
                            text = "Theme",
                            fontSize = 30.sp,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f))

                // time + complete painting
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(
                            "%d:%02d",
                            timeElapsedSeconds / 60,
                            timeElapsedSeconds % 60
                        ),
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                viewmodel.submitPainting(timeElapsedSeconds.toInt())
                                onPostSubmit()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // canvas area, fill all possible space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    // move it up a bit
                    .padding(0.dp, 0.dp, 0.dp, 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { viewmodel.undo() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "undo", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { viewmodel.redo() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "redo", tint = Color.Black)
                    }
                }

                // center canvas initially
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(canvasSizeDp)
                            .height(canvasSizeDp)
                            // todo: dashed like before
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
                                RoundedCornerShape(7.dp)
                            )
                            .padding(2.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                CanvasView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )

                                    setOnTouchListener { v, event ->
                                        // almost same as before, we dont handle gestures in canvas anymore, parent has to handle them
                                        // pass touch events to viewmodel
                                        if (event.pointerCount > 1) {
                                            viewmodel.setGestureState(CanvasGestureState.GESTURE)

                                            return@setOnTouchListener false
                                        }

                                        // reset gesture state when in gesture & last finger lifted
                                        if (event.pointerCount == 1 && drawingState == CanvasGestureState.GESTURE && event.actionMasked == MotionEvent.ACTION_UP) {
                                            viewmodel.setGestureState(CanvasGestureState.IDLE)
                                            return@setOnTouchListener false
                                        }

                                        // after gesturing we can't paint
                                        if (drawingState == CanvasGestureState.GESTURE) return@setOnTouchListener false

                                        val activeLayerIdx = viewmodel.activeLayerIndex.value
                                        if (activeLayerIdx == null) {
                                            viewmodel.setGestureState(CanvasGestureState.IDLE)

                                            v.performClick()
                                            return@setOnTouchListener false
                                        }

                                        when (event.actionMasked) {
                                            MotionEvent.ACTION_DOWN -> {
                                                viewmodel.setGestureState(CanvasGestureState.PAINTING)

                                                val bitmapPos =
                                                    viewmodel.getBitmapPaintPosFromCanvas(
                                                        activeLayerIdx,
                                                        Pair(v.width, v.height),
                                                        Pair(event.x.toInt(), event.y.toInt()),
                                                    )

                                                //Fill
                                                if (viewmodel.currentTool.value == PaintTool.FILL) {
                                                    viewmodel.fillAt(bitmapPos)
                                                    return@setOnTouchListener true
                                                }

                                                viewmodel.startStroke(activeLayerIdx)
                                                viewmodel.setGestureState(CanvasGestureState.PAINTING)
                                                viewmodel.setLastCanvasDrawPoint(bitmapPos)

                                                return@setOnTouchListener true
                                            }

                                            MotionEvent.ACTION_MOVE -> {
                                                val bitmapPos =
                                                    viewmodel.getBitmapPaintPosFromCanvas(
                                                        activeLayerIdx,
                                                        Pair(v.width, v.height),
                                                        Pair(event.x.toInt(), event.y.toInt()),
                                                    )

                                                viewmodel.paintAt(
                                                    activeLayerIdx,
                                                    bitmapPos,
                                                )

                                                return@setOnTouchListener true
                                            }

                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                // paint last pixel if we're drawing
                                                if (drawingState == CanvasGestureState.PAINTING) {
                                                    val bitmapPos =
                                                        viewmodel.getBitmapPaintPosFromCanvas(
                                                            activeLayerIdx,
                                                            Pair(v.width, v.height),
                                                            Pair(event.x.toInt(), event.y.toInt()),
                                                        )

                                                    if (viewmodel.currentTool.value != PaintTool.FILL) {
                                                        viewmodel.paintAt(
                                                            activeLayerIdx,
                                                            bitmapPos,
                                                            false
                                                        )
                                                        viewmodel.endStroke(activeLayerIdx)
                                                    }
                                                }

                                                viewmodel.setGestureState(CanvasGestureState.IDLE)

                                                v.performClick()
                                                return@setOnTouchListener true
                                            }

                                            else -> {
                                                v.performClick()
                                                return@setOnTouchListener false
                                            }
                                        }
                                    }

                                    viewmodel.onCanvasViewCreated(this)
                                }
                            },
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }

            // tools
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // todo: round + bg
                    ToolButton(
                        onClick = { viewmodel.setTool(PaintTool.PEN) },
                        isSelected = currentTool == PaintTool.PEN,
                        icon = Icons.Default.BorderColor,
                        iconContentDescription = "pen",
                        buttonModifier = Modifier.testTag("penButton")

                    )

                    ToolButton(
                        onClick = { viewmodel.setTool(PaintTool.BRUSH) },
                        isSelected = currentTool == PaintTool.BRUSH,
                        icon = Icons.Default.Brush,
                        iconContentDescription = "brush",
                        buttonModifier = Modifier.testTag("brushButton")

                    )

                    ToolButton(
                        onClick = { viewmodel.setTool(PaintTool.FILL) },
                        isSelected = currentTool == PaintTool.FILL,
                        icon = Icons.Default.FormatColorFill,
                        iconContentDescription = "fill",
                        buttonModifier = Modifier.testTag("fillButton")

                    )

                    ToolButton(
                        onClick = { viewmodel.setTool(PaintTool.ERASER) },
                        isSelected = currentTool == PaintTool.ERASER,
                        icon = Icons.Default.Folder,
                        iconContentDescription = "fill",
                        iconModifier = Modifier.rotate(-90f),
                        buttonModifier = Modifier.testTag("eraserButton")

                    )

                    Box(modifier = Modifier.weight(1f))

                    FilledIconButton(
                        onClick = {
                            viewmodel.openColorpicker()
                        },
                        modifier = Modifier
                            .padding(end = 10.dp),
                        colors = IconButtonColors(
                            containerColor = dynamicLightenDarken(selectedColor, .8f),
                            contentColor = modify(
                                selectedColor,
                                a = selectedColor.alpha.coerceAtLeast(0.2f)
                            ),
                            disabledContainerColor = modify(invert(selectedColor), a = 1f),
                            disabledContentColor = selectedColor,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "change color",
                            tint = selectedColor
                        )
                    }

                    Button(
                        onClick = {
                            viewmodel.openLayerEffectsDialog()
                        },

                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledContainerColor = modify(MaterialTheme.colorScheme.secondary, a = .6f),
                            disabledContentColor = modify(MaterialTheme.colorScheme.onSecondary, a = .9f)
                        ),

                        enabled = activeIndex != null
                    ) {
                        Text(text = "effects")
                    }
                }

                // layers
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            viewmodel.newLayerAction()
                        },

                        modifier = Modifier.size(80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(modify(MaterialTheme.colorScheme.secondaryContainer, a = .5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add layer",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    for ((index, layer) in layers.withIndex()) {
                        val borderColor =
                            if (index == activeIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        android.util.Log.d("NewPaintingScreen", "layer $index last updated (active=$activeIndex): ${layer.lastUpdatedTimestamp}")

                        // need to run async to generate preview bitmap
                        val previewBitmap by produceState<Bitmap?>(
                            initialValue = null,
                            key1 = layer.lastUpdatedTimestamp
                        ) {
                            // https://stackoverflow.com/a/59589270
                            value = viewmodel.getLayerBitmap(index)
                        }

                        Box(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(80.dp)
                                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                                .clickable {
                                    if (activeIndex != index)
                                        viewmodel.setActiveLayer(index)
                                    else
                                        viewmodel.setActiveLayer(null)
                                }
                        ) {
                            previewBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "layer preview",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}