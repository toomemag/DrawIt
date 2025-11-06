package com.example.drawit.ui.effectpopups

import android.app.Activity
import android.graphics.Typeface
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.example.drawit.R
import com.example.drawit.painting.Layer
import com.example.drawit.painting.LayerEffectBinding
import com.example.drawit.painting.LayerTransformInput
import com.example.drawit.painting.effects.BaseEffect
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Wrapper class for creating new effect edit
 */

class EffectEditDialog(
    private val activity: Activity,
    private val layer: Layer,
    private val effect: BaseEffect<*>,
    private val onEffectRemoved: (() -> Unit)? = null,
    private val onEffectEditClose: (() -> Unit)? = null
) {
    private val effectsContainer: LinearLayout = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            // max width possible
            LinearLayout.LayoutParams.MATCH_PARENT,
            // match content
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private val innerPadding = 12
    private val dialogPadding = 10
    private val titleFontSize = 26 // sp
    private val descFontSize = 16 // sp

    fun show() {
        // todo: unless it's possible to mimic the style as in other popups, might aswell ditch this diea
        val scale = activity.resources.displayMetrics.density
        val effectBindings = layer.getEffectBindings(effect)

        val innerPaddingPx = (innerPadding * scale + 0.5f).toInt()
        val dialogPaddingPx = (dialogPadding * scale + 0.5f).toInt()

        val typeface: Typeface? = try {
            ResourcesCompat.getFont(activity, R.font.drawn)
        } catch (e: Exception) {
            null
        }

        // container for popup content
        // top -> bottom, effect name, description and then bindings for effect inputs
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                // max width possible
                LinearLayout.LayoutParams.MATCH_PARENT,
                // match content
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(dialogPaddingPx, dialogPaddingPx, dialogPaddingPx, dialogPaddingPx)
            }
        }

        val dialog = MaterialAlertDialogBuilder(activity)
                    .setView(container)
                    .create()

        val effectTitleText = android.widget.TextView(activity).apply {
            text = effect.getEffectName()
            textSize = titleFontSize.toFloat()
            setPadding(0, 0, 0, dialogPaddingPx / 2)
            setTypeface(typeface)
        }

        val effectDescriptionText = android.widget.TextView(activity).apply {
            text = effect.getEffectDescription()
            textSize = descFontSize.toFloat()
            setPadding(0, 0, 0, dialogPaddingPx)
            setTypeface(typeface)
        }

        // push title and desc
        container.addView(effectTitleText)
        container.addView(effectDescriptionText)

        // match style from previous dialogs where all content
        // is wrapped inside drawable bg
        val dialogBg = AppCompatResources.getDrawable(activity, R.drawable.dialog_bg)

        val inputsContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                // max width possible
                LinearLayout.LayoutParams.MATCH_PARENT,
                // match content
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(innerPaddingPx, innerPaddingPx, innerPaddingPx, innerPaddingPx)
            }
        }

        if (dialogBg != null) {
            inputsContainer.background = dialogBg
        }

        // now bindings for effect inputs
        // get current bindings from layer, show those
        // we can get what input option is selected from mask
        // eg. effect returns [ x, y, z ], mask is set as [ val, null, null ]
        // => x is selected input option
        // todo: have to think how we indicate what it's bound to, probably enum?
        // and new button for adding new binding

        // todo: temp to test
        updateEffectBindingsView()

        val addNewBinding = android.widget.Button(activity).apply {
            text = "Add New Binding"
            setOnClickListener {
                effectBindings.add(LayerEffectBinding(
                    effectInputIndex = 0,
                    layerTransformInput = LayerTransformInput.X_POS
                ))

                updateEffectBindingsView()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        inputsContainer.addView(effectsContainer)
        inputsContainer.addView(addNewBinding)

        val spacer = android.view.View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dialogPadding * 2
            )
        }

        val closeDialog = android.widget.Button(activity).apply {
            text = "Close"
            setOnClickListener {
                dialog.dismiss()
                onEffectEditClose?.invoke()
            }
        }

        val removeEffect = android.widget.Button(activity).apply {
            text = "Remove Effect from Layer"
            setOnClickListener {
                layer.removeEffectBinding(effect)
                dialog.dismiss()
                onEffectRemoved?.invoke()
            }
            setTextColor(0xFFFF5555.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // aand push buttons
        container.addView(inputsContainer)
        container.addView(spacer)
        container.addView(closeDialog)
        container.addView(removeEffect)

        dialog.show()
    }

    private fun updateEffectBindingsView() {
        effectsContainer.removeAllViews()

        val effectBindings = layer.getEffectBindings(effect)

        val scale = activity.resources.displayMetrics.density
        val dialogPaddingPx = (dialogPadding * scale + 0.5f).toInt()

        val typeface: Typeface? = try {
            ResourcesCompat.getFont(activity, R.font.drawn)
        } catch (e: Exception) {
            null
        }

        if (effectBindings.isNotEmpty()) {
            for (effectBinding in effectBindings) {
                val bindingText = android.widget.TextView(activity).apply {
                    text = "Input: ${effect.getEffectInputOptions()[effectBinding.effectInputIndex]} -> Layer: ${effectBinding.layerTransformInput}"
                    textSize = descFontSize.toFloat()
                    setPadding(0, 0, 0, dialogPaddingPx / 2)
                    setTypeface(typeface)
                }

                effectsContainer.addView(bindingText)
            }
        }
    }
}