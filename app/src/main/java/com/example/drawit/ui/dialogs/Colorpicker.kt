package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.ui.theme.DrawitTheme
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Preview
@Composable
fun ColorpickerDialog(
    onColorPicked: (color: Color) -> Unit = { },
    onDismissRequest: () -> Unit = { },
    initialColor: Color = Color(255, 255, 255, 255)
) {
    // https://github.com/skydoves/colorpicker-compose
    val controller = rememberColorPickerController()

    var innerColor: Color = initialColor

    // https://developer.android.com/develop/ui/compose/components/dialog
    DrawitTheme {
        Dialog(
            onDismissRequest = onDismissRequest,
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
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        innerColor = colorEnvelope.color
                    },
                    initialColor = initialColor
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    controller = controller,
                    tileOddColor = Color.White,
                    tileEvenColor = Color.Gray,
                    initialColor = initialColor
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    controller = controller,
                    initialColor = initialColor
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                /*
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.weight(1f),

                        onClick = {
                            onDismissRequest()
                        },

                        enabled = true,

                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                            disabledContentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(10.dp)
                    )*/

                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.weight(1f),

                        onClick = {
                            onColorPicked(innerColor)
                            onDismissRequest()
                        },

                        enabled = true,

                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(
                            text = "Select",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                //}
            }
        }
    }
}