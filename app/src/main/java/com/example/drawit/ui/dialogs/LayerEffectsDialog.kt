package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.ui.components.painting.EffectListItem
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.utils.darken

@Preview
@Composable
fun LayerEffectsDialog(
    onDismiss: () -> Unit = { },

    onSelectEffect: (effect: BaseEffect<*>) -> Unit = { },
    onAddEffectClick: () -> Unit = { },

    effects: List<BaseEffect<*>> = listOf(),
) {
    val scroll = rememberScrollState()

    val configuration = LocalConfiguration.current
    val maxEffectsHeight = with(LocalDensity.current) {
        configuration.screenHeightDp * .5f
    }

    DrawitTheme { // wrap theme so preview knows what's up
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
                    .clip(RoundedCornerShape(40.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Layer Effects",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max=maxEffectsHeight.dp)
                        .verticalScroll(scroll)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            darken(MaterialTheme.colorScheme.surface, .1f)
                        )
                        .padding(10.dp)
                ) {
                    effects.forEach { effect ->
                        EffectListItem(
                            effect=effect,
                            onEffectClick = {
                                onSelectEffect(effect)
                            }
                        )
                    }

                    Button(
                        onClick = {
                            onAddEffectClick( )
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

                /*
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),

                    onClick = {
                        onDismiss()
                    },

                    enabled = true,

                    colors = ButtonColors(
                        containerColor = darken(MaterialTheme.colorScheme.tertiary, .3f),
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                        disabledContentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(text = "Close")
                }*/
            }
        }
    }
}