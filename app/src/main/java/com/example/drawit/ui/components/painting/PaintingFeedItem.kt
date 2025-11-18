package com.example.drawit.ui.components.painting

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drawit.domain.model.Layer
import com.example.drawit.domain.model.Painting
import com.example.drawit.ui.theme.DrawitTheme

@Preview
@Composable
fun PaintingFeedItem(
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
    
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { }
) {
    DrawitTheme { // preview styles
        // todo: figure out when to play effects
        //       could do on click to focus and then apply sensor listeners
        
        // https://stackoverflow.com/a/67741362
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape( 10.dp )
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

            Row(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp),

                horizontalArrangement = Arrangement.spacedBy( 10.dp )
            ) {

                Text(
                    text = painting.theme,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "- ${ painting.mode } (draft)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}