package com.example.drawit.ui.screens.main

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drawit.PaintingActivity
import com.example.drawit.data.local.room.repository.PaintingsRepository
import com.example.drawit.ui.components.painting.PaintingGridGallery
import com.example.drawit.ui.theme.DrawitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Preview
@Composable
fun ProfileScreen(
    paintingsRepository: PaintingsRepository? = null
) {
    var selected by remember { mutableStateOf(0) }
    val paintingsFlow = remember { paintingsRepository?.getAllPaintings() }
    val paintings by paintingsFlow?.collectAsState(initial = emptyList()) ?: mutableStateOf(emptyList())
    val ctx = LocalContext.current

    // preview support
    DrawitTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.displayLarge,
                )

                val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                val versionName = pInfo.versionName ?: "<not found>"
                val versionCode = pInfo.longVersionCode
                Text(
                    text = "$versionName - $versionCode",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .padding(start = 10.dp)
                )
            }

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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp)
                    ) {
                        val scope = rememberCoroutineScope()

                        PaintingGridGallery(
                            paintings = paintings,
                            onPaintingClick = { painting ->
                                val intent = Intent(ctx, PaintingActivity::class.java).apply {
                                    putExtra("paintingId", painting.id)
                                }
                                ctx.startActivity(intent)
                            },
                            onPaintingDelete = { painting ->
                                scope.launch(Dispatchers.IO) {
                                    paintingsRepository?.deletePainting(painting)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}