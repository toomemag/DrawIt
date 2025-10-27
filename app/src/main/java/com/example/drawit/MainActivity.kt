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

class MainActivity : AppCompatActivity() {

    // binding for activity_main.xml
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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