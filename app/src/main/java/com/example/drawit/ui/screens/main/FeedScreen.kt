package com.example.drawit.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.drawit.data.local.room.repository.PaintingsRepository

@Composable
fun FeedScreen(
    paintingsRepository: PaintingsRepository
) {
    val paintings by paintingsRepository.getAllPaintings().collectAsState(initial=emptyList())

    if ( paintings.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "No paintings, go create some",
                style = MaterialTheme.typography.displayMedium
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(paintings) { painting ->
            Text(
                text = "Painting in ${painting.mode} with theme as ${painting.theme}",
            )
        }
    }
}