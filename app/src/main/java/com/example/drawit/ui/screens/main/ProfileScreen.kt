package com.example.drawit.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drawit.data.local.room.repository.PaintingsRepository
import com.example.drawit.ui.theme.DrawitTheme

@Preview
@Composable
fun ProfileScreen(
    paintingsRepository: PaintingsRepository? = null
) {
    var selected by remember { mutableStateOf(0) }

    // preview support
    DrawitTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) { }

            SecondaryTabRow(
                tabs = {
                    Tab(
                        selected = selected == 0,
                        onClick = {
                            selected = 0
                        }
                    ) {
                        Text(
                            text = "Uploaded",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }

                    Tab(
                        selected = selected == 1,
                        onClick = {
                            selected = 1
                        }
                    ) {
                        Text(
                            text = "Drafts",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                },
                selectedTabIndex = selected,
                modifier = Modifier
                    .height(40.dp)
            )

            when (selected) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "Uploads (step 5)",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                1 -> {
                    // todo: instagram-like gallery
                    if (paintingsRepository != null)
                        FeedScreen(paintingsRepository)
                }
            }
        }
    }
}