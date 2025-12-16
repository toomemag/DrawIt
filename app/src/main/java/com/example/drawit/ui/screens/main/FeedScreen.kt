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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.drawit.DrawItApplication
import com.example.drawit.PaintingActivity
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.domain.model.Painting
import com.example.drawit.ui.components.painting.PaintingFeedItem

@Composable
fun FeedScreen( ) {
    var paintings = remember { mutableStateListOf<Painting>( ) }

    val ctx = LocalContext.current
    val app = ctx.applicationContext as DrawItApplication
    val currentUser = app.authenticationRepository.getCurrentUser()!!

    LaunchedEffect(currentUser.uid) {
        paintings.clear( )
        val ownPaintings = app.firestorePaintingsRepository.getUserPaintings(currentUser.uid)

        if ( ownPaintings is NetworkResult.Success )
            paintings.addAll(ownPaintings.data.reversed())

        val friends = app.firestorePaintingsRepository.getFriendsForUser(currentUser.uid)

        when ( friends ) {
            is NetworkResult.Success -> {
                android.util.Log.d( "FeedScreen", "got friends for user ${currentUser.uid}: ${friends.data.joinToString { it.friendId }}" )

                for ( friend in friends.data ) {
                    val friendPaintings = app.firestorePaintingsRepository.getUserPaintings(friend.friendId)

                    if ( friendPaintings is NetworkResult.Success ) {
                        android.util.Log.d( "FeedScreen", "got paintings for friend ${friend.friendId}: ${friendPaintings.data.size} paintings" )
                        paintings.addAll(friendPaintings.data)
                    } else {
                        android.util.Log.d(
                            "FeedScreen",
                            "Failed to get paintings for friend ${friend.friendId} -> ${if (friendPaintings is NetworkResult.Error) friendPaintings.message else friendPaintings.toString()}"
                        )
                    }
                }
            }
            else -> {
                android.util.Log.d( "FeedScreen", "Failed to get friends for user ${currentUser.uid} -> ${ if ( friends is NetworkResult.Error ) friends.message else friends.toString( ) }" )
                return@LaunchedEffect
            }
        }
    }

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
                text = "No paintings to show in your feed",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 80.dp, top = 10.dp)
        ) {
            items(paintings) { painting ->
                PaintingFeedItem(
                    painting = painting,
                    onClick = {
                        val intent = Intent(ctx, PaintingActivity::class.java).apply {
                            putExtra("paintingId", painting.id)
                        }
                        ctx.startActivity(intent)
                    }
                )
            }
        }
    }
}