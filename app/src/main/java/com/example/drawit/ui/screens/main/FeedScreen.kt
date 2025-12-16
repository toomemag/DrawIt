package com.example.drawit.ui.screens.main

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drawit.PaintingActivity
import com.example.drawit.data.local.room.repository.LocalPaintingsRepository
import com.example.drawit.ui.components.painting.PaintingFeedItem
import com.example.drawit.utils.darken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    paintingsRepository: LocalPaintingsRepository
) {
    val paintingsFlow = remember { paintingsRepository.getAllPaintings() }
    val paintings = paintingsFlow.collectAsState(initial=emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val dialogContextPainting = remember { mutableStateOf(paintings.value.firstOrNull()) }

    val ctx = LocalContext.current

    if ( paintings.value.isEmpty()) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 80.dp, top = 10.dp)
        ) {
            items(paintings.value.reversed()) { painting ->
                PaintingFeedItem(
                    painting = painting,
                    onClick = {
                        val intent = Intent(ctx, PaintingActivity::class.java).apply {
                            putExtra("paintingId", painting.id)
                        }
                        ctx.startActivity(intent)
                    },
                    onLongClick = {
                        showDialog = true
                        dialogContextPainting.value = painting
                    }
                )
            }
        }

        val scope = rememberCoroutineScope()

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
                        text = "Are you sure?",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if ( dialogContextPainting.value == null ) {
                                showDialog = false
                                return@Button
                            }

                            scope.launch(Dispatchers.IO) {
                                paintingsRepository.deletePainting(dialogContextPainting.value!!)
                                showDialog = false
                            }
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
}