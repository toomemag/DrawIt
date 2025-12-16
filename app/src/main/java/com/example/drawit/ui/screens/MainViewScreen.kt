package com.example.drawit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.drawit.data.local.room.repository.LocalPaintingsRepository
import com.example.drawit.ui.screens.main.FeedScreen
import com.example.drawit.ui.screens.main.FriendsScreen
import com.example.drawit.ui.screens.main.ProfileScreen

enum class Tab {
    Feed, // local DAO items atm,
    Friends, // or uploads wv, comes later
    Profile // profile page, uplaoded (later) & DAO paintings
}

@Composable
fun MainViewScreen(
    paintingsRepository: LocalPaintingsRepository,
    selectedTab: Tab
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (selectedTab) {
            Tab.Feed -> FeedScreen(
                paintingsRepository = paintingsRepository
            )
            Tab.Friends -> FriendsScreen()
            Tab.Profile -> ProfileScreen(
                paintingsRepository = paintingsRepository
            )
        }
    }
}