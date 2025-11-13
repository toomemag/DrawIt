package com.example.drawit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
                    floatingActionButtonPosition = FabPosition.End
                ) { }

                AppNav(
                    app.navCoordinator
                )
            }
        }
    }
}