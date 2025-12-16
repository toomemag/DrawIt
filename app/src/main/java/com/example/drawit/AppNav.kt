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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawit.data.local.room.repository.PaintingsRepository
import com.example.drawit.domain.model.authentication.AuthenticationRepository
import com.example.drawit.ui.screens.AuthenticationScreen
import com.example.drawit.ui.screens.MainViewScreen
import com.example.drawit.ui.screens.Tab
import com.example.drawit.ui.viewmodels.auth.AuthenticationVMFactory

@Composable
fun AppNav(
    navCoordinator: NavCoordinator,
    paintingsRepository: PaintingsRepository,
    authenticationRepository: AuthenticationRepository,
    selectedTab: Tab
) {
    val navController = rememberNavController()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val ctx = LocalContext.current
    // todo: auth later, step 5
    val startDestination = if (authenticationRepository.isLoggedIn()) Screen.MainScreen.route else Screen.AuthenticationScreen.route

    LaunchedEffect(navController) {
        navCoordinator.events.flowWithLifecycle(lifecycle)
            .collect { event ->
                when (event) {
                    is NavEvent.ToPaintingDetail -> {
                        navController.navigate("painting/${event.paintingId}")
                    }
                    is NavEvent.ToNewPainting -> {
                        navController.navigate("new_painting")
                    }
                    is NavEvent.Back -> {
                        val activity = ctx as? PaintingActivity

                        // in paitnignactivity, call to show dialog
                        if (activity != null) {
                            activity.onBackPressedDispatcher.onBackPressed()
                        } else {
                            // else just pop view stack
                            navController.popBackStack()
                        }
                    }
                    is NavEvent.ToMainView -> {
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo(0)
                        }
                    }
                    is NavEvent.ToAuthView -> {
                        navController.navigate(Screen.AuthenticationScreen.route) {
                            popUpTo(0)
                        }
                    }
                }
            }
    }

    NavHost(navController, startDestination = startDestination) {
        composable(Screen.AuthenticationScreen.route) {
            AuthenticationScreen(
                viewmodel = viewModel(
                    factory = AuthenticationVMFactory(
                        navCoordinator = navCoordinator,
                        authenticationRepository = authenticationRepository
                    )
                )
            )
        }

        composable(Screen.MainScreen.route) {
            MainViewScreen(
                paintingsRepository = paintingsRepository,
                selectedTab = selectedTab
            )
        }

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