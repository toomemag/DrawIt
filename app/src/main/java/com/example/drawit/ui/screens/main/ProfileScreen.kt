package com.example.drawit.ui.screens.main

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import com.example.drawit.MainActivity
import com.example.drawit.PaintingActivity
import com.example.drawit.data.local.room.repository.LocalPaintingsRepository
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.data.remote.repository.FirestoreDrawItRepository
import com.example.drawit.domain.model.Painting
import com.example.drawit.ui.components.painting.PaintingGridGallery
import com.example.drawit.ui.theme.DrawitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun renderUploadedPaintings(userPaintings: List<Painting>, firestore: FirestoreDrawItRepository) {
    val scope = rememberCoroutineScope()

    if ( userPaintings.isEmpty( ) ) {
        Text(
            text = "You have not uploaded any paintings yet. Go upload some!",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        return@renderUploadedPaintings
    }

    PaintingGridGallery(
        paintings = userPaintings,
        onPaintingClick = { painting ->
            // todo: preview
        },
        onPaintingDelete = { painting ->
            scope.launch(Dispatchers.IO) {
                firestore.delete(painting.id)
            }
        }
    )
}

@Preview
@Composable
fun ProfileScreen(
    paintingsRepository: LocalPaintingsRepository? = null,
    navController: NavController? = null
) {
    var selected by remember { mutableStateOf(0) }
    val paintingsFlow = remember { paintingsRepository?.getAllPaintings() }
    val paintings by paintingsFlow?.collectAsState(initial = emptyList()) ?: mutableStateOf(emptyList())
    val ctx = LocalContext.current

    val app = ctx.applicationContext as com.example.drawit.DrawItApplication
    val firestore = app.firestorePaintingsRepository
    val firebaseAuth = app.authenticationRepository

    val fetchedPaintings = remember { mutableStateOf<NetworkResult<*>>(NetworkResult.Loading) }
    val isRefreshing = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // preview support
    DrawitTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val user = firebaseAuth.getCurrentUser()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = user?.displayName ?: "Username",
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
                IconButton(onClick = {
                    scope.launch {
                        firebaseAuth.logout()
                        val activity = (ctx as? Activity)
                        val intent = Intent(ctx, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(intent)
                        activity?.finish()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                }
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
                        if ( user == null ) {
                            Text(
                                text = "Something went wrong.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            return@Column
                        }

                        LaunchedEffect(user.uid) {
                            // todo: route userid, rn no friends support

                            // don't show loading state on refresh if we already have data
                            if (fetchedPaintings.value is NetworkResult.Success) {
                                isRefreshing.value = true
                                fetchedPaintings.value = firestore.getUserPaintings(user.uid)
                                isRefreshing.value = false
                            } else {
                                fetchedPaintings.value = firestore.getUserPaintings(user.uid)
                            }
                        }




                        when ( val result = fetchedPaintings.value ) {
                            is NetworkResult.Success<*> -> {
                                // unsafe whatever
                                val userPaintings = result.data as List< Painting >

                                renderUploadedPaintings(
                                    userPaintings = userPaintings,
                                    firestore = firestore
                                )
                            }
                            is NetworkResult.Error -> {
                                // todo: could toast
                                Text(
                                    text = "Error fetching paintings.${result.message}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is NetworkResult.Loading -> {
                                if (!isRefreshing.value) {
                                    Text(
                                        text = "Loading paintings...",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Show cached data while refreshing
                                    if (fetchedPaintings.value is NetworkResult.Success<*>) {
                                        renderUploadedPaintings(
                                            userPaintings = (fetchedPaintings.value as NetworkResult.Success<*>).data as List<Painting>,
                                            firestore = firestore
                                        )
                                    }
                                }
                            }
                        }
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
