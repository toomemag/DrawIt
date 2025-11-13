package com.example.drawit

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNav(
    navCoordinator: NavCoordinator
) {
    val navController = rememberNavController()

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(navController) {
        navCoordinator.events.flowWithLifecycle(lifecycle)
            .collect {
                when (it) {
                    is NavEvent.ToPaintingDetail -> {
                        navController.navigate("painting/${it.paintingId}")
                    }
                    is NavEvent.ToNewPainting -> {
                        navController.navigate("new_painting")
                    }
                    is NavEvent.Back -> {
                        navController.popBackStack()
                    }
                }
            }
    }

    NavHost(navController, startDestination = Screen.NewPainting.route) {
        composable(Screen.NewPainting.route) {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val intent = Intent(context, PaintingActivity::class.java)
                context.startActivity(intent)
                navController.popBackStack()
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading Painting Activity...")
            }
        }
    }
}