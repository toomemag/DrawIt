package com.example.drawit

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.drawit.databinding.ActivityPaintingBinding
import com.example.drawit.databinding.DialogPausePaintingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PaintingActivity : AppCompatActivity() {
    // binding for activity_painting.xml
    private lateinit var binding: ActivityPaintingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaintingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Draw behind system bars and handle insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Ensure no app bar is shown even if theme is overridden
        supportActionBar?.hide()

        val mode = intent.getStringExtra("mode") ?: "free_mode"
        binding.titleText.text = if (mode == "daily_theme") "Theme" else "Freemode"

        // titlebar would be below the phone's status bar, so add padding to compensate
        val originalTitlebarPaddingTop = binding.titlebar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.titlebar) { v, insets ->
            // add status bar height to titlebar padding top
            val sb = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            // set new padding
            v.setPadding(v.paddingLeft, originalTitlebarPaddingTop + sb, v.paddingRight, v.paddingBottom)
            // gradient shader update
            v.post { updateOverlayStops() }
            insets
        }

        // initial run on first layout
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // remove old listener
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // run shader update
                updateOverlayStops()
            }
        })

        // whenever any of these views have a layout change -> update shader stops
        val relayoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateOverlayStops() }
        binding.titlebar.addOnLayoutChangeListener(relayoutListener)
        binding.toolbar.addOnLayoutChangeListener(relayoutListener)
        binding.gridBackground.addOnLayoutChangeListener(relayoutListener)

        // on new painting click show a custom rendered popup
        findViewById<Button>(R.id.pausePainting).setOnClickListener {
            val dialogBinding = DialogPausePaintingBinding.inflate(layoutInflater)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.root)
                .create()

            // button listeners
            dialogBinding.ActivePaintingContinue.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.ActivePaintingSaveExit.setOnClickListener {
                // close activity
                // todo: save painting
                finish()
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun updateOverlayStops() {
        // get positions to draw gradient (hides grid) correctly
        val gridLoc = IntArray(2)
        val titleLoc = IntArray(2)
        val toolbarLoc = IntArray(2)
        binding.gridBackground.getLocationOnScreen(gridLoc)
        binding.titlebar.getLocationOnScreen(titleLoc)
        binding.toolbar.getLocationOnScreen(toolbarLoc)

        // ypos relative to grid
        val titlebarBottomY = titleLoc[1] + binding.titlebar.height
        val toolbarTopY = toolbarLoc[1]

        // clamp if needed (shouldnt be necessary, but just in case)
        val start = titlebarBottomY.coerceIn(0, binding.gridBackground.height)
        val end = toolbarTopY.coerceIn(start, binding.gridBackground.height)
        binding.gridBackground.updateOverlayStops(start, end)
    }
}
