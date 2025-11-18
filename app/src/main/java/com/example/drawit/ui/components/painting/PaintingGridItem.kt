package com.example.drawit.ui.components.painting

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.drawit.domain.model.Layer
import com.example.drawit.domain.model.Painting
import com.example.drawit.utils.darken

@Composable
fun PaintingGridItem(
    painting: Painting = Painting(
        id = "",
        theme = "Red Moon",
        mode = "Free Mode",
        layers = mutableListOf(Layer(
            bitmap = androidx.core.graphics.createBitmap(128, 128, android.graphics.Bitmap.Config.ARGB_8888).apply {
                eraseColor( android.graphics.Color.RED )
            },
        ))
    ),
    onClick: ( ) -> Unit = { },
    onDelete: ( ) -> Unit = { }
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showDialog = true }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(5.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape( 5.dp )
                )
        ) {
            for ( layer in painting.layers ) {
                Image(
                    bitmap = layer.bitmap.asImageBitmap( ),
                    contentDescription = "Painting Layer",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Delete painting?",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text = painting.theme,
                    style = MaterialTheme.typography.displaySmall
                )
           },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onDelete( )
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContainerColor = darken(MaterialTheme.colorScheme.error, 0.3f),
                        disabledContentColor = darken(MaterialTheme.colorScheme.onError, 0.3f)
                    )
                ) {
                    Text(
                        text = "Delete"
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}