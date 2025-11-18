package com.example.drawit.ui.components.painting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drawit.domain.model.Painting

@Composable
fun PaintingGridGallery(
    paintings: List<Painting>,
    onPaintingClick: ( Painting ) -> Unit,
    onPaintingDelete: ( Painting ) -> Unit
) {
    if ( paintings.isEmpty() ) {
        Text(
            text = "No paintings, go create some!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items( paintings ) { painting ->
            PaintingGridItem(
                painting = painting,
                onClick = {
                    onPaintingClick( painting )
                },
                onDelete = {
                    onPaintingDelete( painting )
                }
            )
        }
    }
}