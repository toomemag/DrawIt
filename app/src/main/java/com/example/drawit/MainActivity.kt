package com.example.drawit

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.drawit.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.Button
import com.example.drawit.databinding.DialogNewPaintingBinding
import android.content.Intent
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    // binding for activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // firebase auth instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_apiexample
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // on new painting click show a custom rendered popup
        findViewById<Button>(R.id.NewPainting).setOnClickListener {
            val dialogBinding = DialogNewPaintingBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.root)
                .create()

            // button listeners
            dialogBinding.NewPaintingDailyTheme.setOnClickListener {
                startActivity(Intent(this, PaintingActivity::class.java).putExtra("mode", "daily_theme"))
                dialog.dismiss()
            }

            dialogBinding.NewPaintingFreeMode.setOnClickListener {
                startActivity(Intent(this, PaintingActivity::class.java).putExtra("mode", "free_mode"))
                dialog.dismiss()
            }

            dialogBinding.NewPaintingCancel.setOnClickListener { dialog.dismiss() }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}