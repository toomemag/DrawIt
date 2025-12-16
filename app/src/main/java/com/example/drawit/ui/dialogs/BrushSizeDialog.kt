package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.ui.theme.DrawitTheme
import kotlin.math.roundToInt

@Composable
fun BrushSizeDialog(
    currentSize: Int,
    onSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    minSize: Int = 1,
    maxSize: Int = 60
) {
    var sliderValue by remember(currentSize) { mutableFloatStateOf(currentSize.toFloat()) }

    DrawitTheme {
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
                    .padding(20.dp),
            ) {
                Text(
                    text = "Brush size",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val diameter = sliderValue.roundToInt().coerceIn(2, 56).dp
                    Box(
                        modifier = Modifier
                            .size(diameter)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "${sliderValue.roundToInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { v ->
                        sliderValue = v
                        onSizeSelected(v.roundToInt())
                    },
                    valueRange = minSize.toFloat()..maxSize.toFloat()
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onDismiss() },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary,
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}

