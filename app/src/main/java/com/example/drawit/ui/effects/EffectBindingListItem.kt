package com.example.drawit.ui.effects

import android.app.Activity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import com.example.drawit.painting.LayerEffectBinding
import com.example.drawit.painting.LayerTransformInput
import com.example.drawit.painting.effects.BaseEffect


/**
 * Wrapper class for building an effect binding list item view
 *
 * @param activity The activity context to use for building views
 * @param effect The effect associated with the binding
 * @param effectBinding The LayerEffectBinding instance to bind
 *
 * @property effectInputOptions The list of effect input options from the effect
 */
class EffectBindingListItem(
    private val activity: Activity,
    effect: BaseEffect<*>,
    private val effectBinding: LayerEffectBinding
) {
    private val effectInputOptions = effect.getEffectInputOptions()

    /**
     * Builds the effect binding list item view
     * @return The constructed View representing the effect binding list item
     */
    fun build(): View {
        val listItemContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                // max width possible
                LinearLayout.LayoutParams.MATCH_PARENT,
                // match content
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val effectOutputSelection = Spinner(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // weight
            )
            id = "effect_output_selection_spinner".hashCode()
        }

        val layerInputOption = Spinner(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // weight
            )
            id = "layer_input_option_spinner".hashCode()
        }

        listItemContainer.addView(effectOutputSelection)
        listItemContainer.addView(layerInputOption)

        // dropdown selection listeners
        effectOutputSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                effectBinding.effectInputIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        layerInputOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                effectBinding.layerTransformInput = LayerTransformInput.entries[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        // from: https://developer.android.com/develop/ui/views/components/spinner + api example
        // set adapters and initial selections for spinners
        ArrayAdapter(
            activity,
            android.R.layout.simple_spinner_item,
            effectInputOptions
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            effectOutputSelection.adapter = adapter
            effectOutputSelection.setSelection(effectBinding.effectInputIndex)
        }

        ArrayAdapter(
            activity,
            android.R.layout.simple_spinner_item,
            LayerTransformInput.entries.toTypedArray()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            layerInputOption.adapter = adapter
            layerInputOption.setSelection(effectBinding.layerTransformInput.ordinal)
        }

        return listItemContainer
    }
}