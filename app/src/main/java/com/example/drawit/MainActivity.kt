package com.example.drawit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.drawit.ui.HoveringNavBar
import com.example.drawit.ui.screens.Tab
import com.example.drawit.ui.theme.DrawitTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    // firebase auth instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = (application as DrawItApplication)

        auth = Firebase.auth

        val currentUser = auth.currentUser

        if (currentUser == null) {
            auth.signInWithEmailAndPassword("test@test.ee", "[redacted]").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                } else {
                    // If sign in fails, display a message to the user.
                    android.util.Log.e( "AuthFlow", "firebase auth failed: ${task.exception}")
                }
            }
        }

        setContent {
            DrawitTheme {
                var selectedTab by remember { mutableStateOf(Tab.Feed) }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = Intent(this@MainActivity, PaintingActivity::class.java).let { intent ->
                                {
                                    startActivity(intent)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "new painting")
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,

                    bottomBar = {
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
                ) { }

                AppNav(
                    navCoordinator = app.navCoordinator,
                    paintingsRepository = app.paintingsRepository,
                    selectedTab = selectedTab,
                )
            }
        }
    }
}