package com.example.drawit

import android.content.Intent
import android.os.Bundle
import android.view.ViewAnimationUtils
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.drawit.ui.HoveringNavBar
import com.example.drawit.ui.screens.Tab
import com.example.drawit.ui.theme.DrawitTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.math.hypot
import kotlin.math.pow

// modified from https://easings.net/#easeOutBack !!!
class EaseOutBackInterpolator(
    private val c1: Float = 1.70158f
) : AnticipateInterpolator(0f) {
    override fun getInterpolation(t: Float): Float {
        val c3 = c1 + 1.0f
        var ret = 1f + c3 * (t - 1.0).pow(3.0).toFloat() + c1 * (t - 1.0).pow(2.0).toFloat()

        if ( ret > 1 )
            ret = 2 - ret

        return ret
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splash.setOnExitAnimationListener { splashView ->
            val diag = hypot(
                splashView.view.width.toDouble() / 2,
                splashView.view.height.toDouble() / 2
            ).toFloat()

            val circularReveal = ViewAnimationUtils.createCircularReveal(
                splashView.view,
                splashView.view.width / 2,
                splashView.view.height / 2,
                diag,
                0f
            )

            circularReveal.duration = 1000L
            circularReveal.interpolator = EaseOutBackInterpolator(4.2f)


            circularReveal.doOnEnd {
                splashView.remove()
            }

            circularReveal.start()
        }

        val app = (application as DrawItApplication)

        setContent {
            DrawitTheme {
                var isLoggedIn by remember { mutableStateOf(app.authenticationRepository.isLoggedIn()) }
                var selectedTab by remember { mutableStateOf(Tab.Feed) }

                LaunchedEffect(Unit) {
                    Firebase.auth.addAuthStateListener { auth ->
                        isLoggedIn = auth.currentUser != null
                    }
                }

                Scaffold(
                    floatingActionButton = {
                        if ( !isLoggedIn) {
                            return@Scaffold
                        }

                        FloatingActionButton(
                            onClick = Intent(this@MainActivity, PaintingActivity::class.java).let { intent ->
                                {
                                    app.navCoordinator.toNewPainting()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "new painting")
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,

                    bottomBar = {
                        if ( !isLoggedIn) {
                            return@Scaffold
                        }


                        HoveringNavBar(
                            activeTab = selectedTab,
                            onSelect = { tab ->
                                selectedTab = tab
                                android.util.Log.d( "MainActivity", "selected ${tab.name}" )
                            },
                            modifier = Modifier
                                .padding(top = 0.dp, start = 20.dp, end = 20.dp, bottom = 20.dp )
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                    ) {
                        AppNav(
                            navCoordinator = app.navCoordinator,
                            paintingsRepository = app.paintingsRepository,
                            selectedTab = selectedTab,
                            authenticationRepository = app.authenticationRepository
                        )
                    }

                }
            }
        }
    }
}