package com.example.drawit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.utils.darken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Preview
@Composable
fun PausePainting(
    onSaveAndExit: () -> Unit = { },
    onBackToPainting: () -> Unit = { },
    onDiscardAndExit: () -> Unit = {},
    hasDrawn: Boolean = false
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }
    DrawitTheme {
        Dialog(
            onDismissRequest = onBackToPainting,
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
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .align(alignment = androidx.compose.ui.Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    onClick = {
                        onSaveAndExit()
                    },
                    enabled = true,
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = darken(MaterialTheme.colorScheme.primary, .3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (hasDrawn) "Save & Exit" else "Exit",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.surface,
                    )
                }

                if(hasDrawn){
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        onClick = {
                            //onDiscardAndExit()
                            showDiscardConfirm=true

                        },
                        enabled = true,
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = darken(MaterialTheme.colorScheme.primary, .3f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Discard & Exit",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.surface,
                        )
                    }}
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    onClick = {
                        onBackToPainting()
                    },
                    enabled = true,

                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = darken(MaterialTheme.colorScheme.secondary, .3f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "Back To Painting",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                if (showDiscardConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDiscardConfirm = false },
                        title = {
                            Text(
                                text = "Discard painting?",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure?",
                                style = MaterialTheme.typography.displaySmall
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onDiscardAndExit()
                                },
                                colors = ButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    disabledContainerColor = darken(MaterialTheme.colorScheme.error, 0.3f),
                                    disabledContentColor = darken(MaterialTheme.colorScheme.onError, 0.3f)
                                )
                            ) {
                                Text(
                                    text = "Discard"
                                )
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDiscardConfirm = false }
                            ) {
                                Text(
                                    text = "Cancel"
                                )
                            }
                        }
                    )
                }


            }
        }
    }
}