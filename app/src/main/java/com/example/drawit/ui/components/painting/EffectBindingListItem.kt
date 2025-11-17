package com.example.drawit.ui.components.painting

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drawit.painting.LayerEffectBinding
import com.example.drawit.painting.LayerTransformInput
import com.example.drawit.painting.effects.BaseEffect

@Composable
fun EffectBindingListItem(
    effect: BaseEffect<*>,
    binding: LayerEffectBinding
) {
    var expandedOutput by remember { mutableStateOf(false) }
    var expandedInput by remember { mutableStateOf(false) }
    val effectOuputOptions = effect.getEffectOutputOptions()
    val layerInputOptions = LayerTransformInput.entries.toTypedArray()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Button(
            onClick = { expandedOutput = !expandedOutput },
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = effectOuputOptions[binding.effectOutputIndex],
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            DropdownMenu(
                expanded = expandedOutput,
                onDismissRequest = { },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            ) {
                for ((index, output) in effectOuputOptions.withIndex()) {
                    DropdownMenuItem(
                        text = { Text(text = output) },
                        onClick = {
                            binding.effectOutputIndex = index
                            expandedOutput = false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .width(10.dp)
        )

        Button(
            onClick = { expandedInput = !expandedInput },
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = binding.layerTransformInput.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            DropdownMenu(
                expanded = expandedInput,
                onDismissRequest = { },
                modifier = Modifier.weight(1f),
            ) {
                for (input in layerInputOptions) {
                    DropdownMenuItem(
                        text = { Text(text = input.name) },
                        onClick = {
                            binding.layerTransformInput = input
                            expandedInput = false
                        }
                    )
                }
            }
        }
    }
}