package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.domain.model.Layer
import com.example.drawit.domain.model.LayerEffectBinding
import com.example.drawit.domain.model.LayerTransformInput
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.ui.components.painting.EffectBindingListItem
import com.example.drawit.utils.darken
import com.example.drawit.utils.modify


@Composable
fun EffectBindingDialog(
    effect: BaseEffect<*>,
    layer: Layer,
    onDismiss: () -> Unit,
) {
    // on effect/layer change, update binding list
    val bindings = remember( layer.effectBindings[effect.getEffectType()]?.size) {
        mutableStateListOf<LayerEffectBinding>().apply {
            addAll(layer.getEffectBindings(effect))
        }
    }

    val scroll = rememberScrollState()
    val configuration = LocalConfiguration.current
    val maxBindingsHeight = with(LocalDensity.current) {
        configuration.screenHeightDp.dp* .5f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .clip(
                    RoundedCornerShape(40.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
        ) {
            Text(
                text=effect.getEffectName(),
                style=MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text=effect.getEffectDescription(),
                style=MaterialTheme.typography.labelMedium,
                color=MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Center
            )

            // without container with rounding, scroll forces
            // inner container to not have rounded corners when scrolled
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max=maxBindingsHeight)
                        .verticalScroll(scroll)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            darken(MaterialTheme.colorScheme.surface, .1f)
                        )
                        .padding(10.dp)
                ) {
                    // existing bindings
                    bindings.forEachIndexed { index, layerEffectBinding ->
                        EffectBindingListItem(
                            effect = effect,
                            // cant use state, copied
                            binding = layer.effectBindings[effect.getEffectType()]!![index]
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        if (index < bindings.size - 1) {
                            Spacer(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = modify(
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                            a = .2f
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                    }


                    // add new binding
                    Button(
                        onClick = {
                            val newBinding = LayerEffectBinding(
                                effectOutputIndex = 0,
                                layerTransformInput = LayerTransformInput.X_POS
                            )

                            layer.effectBindings[effect.getEffectType()]!!.add( newBinding )
                            // also update state list
                            bindings.add( newBinding )
                        },

                        modifier = Modifier
                            .fillMaxWidth(),

                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = {
                    layer.removeEffectBinding(effect)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),

                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContentColor = Color.Gray,
                    disabledContainerColor = Color.DarkGray,
                )
            ) {
                Text(
                    text = "Remove Effect",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}