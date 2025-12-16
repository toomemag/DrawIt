package com.example.drawit

import android.Manifest
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.drawit.painting.effects.EffectManager
import com.example.drawit.ui.screens.NewPaintingScreen
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.ui.viewmodels.NewPaintingVMFactory
import com.example.drawit.ui.viewmodels.NewPaintingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PaintingActivity: ComponentActivity(), SensorEventListener {
    private lateinit var viewmodel: NewPaintingViewModel

    val effectManager: EffectManager by lazy { EffectManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DrawItApplication
        val paintingsRepository = app.paintingsRepository
        val paintingId = intent.getStringExtra("paintingId")
        val isDarkMode =
            (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        viewmodel = ViewModelProvider(
            this@PaintingActivity,
            NewPaintingVMFactory(
                effectManager = effectManager,
                paintingsRepository = paintingsRepository,
                initialPainting = null,
                isDarkMode = isDarkMode
            )
        )[NewPaintingViewModel::class]

        viewmodel._test_onPauseFromParent = {
            onPause()
        }
        viewmodel._test_onResumeFromParent = {
            onResume()
        }

        onBackPressedDispatcher.addCallback(this) {
            viewmodel.pausePainting()
        }

        lifecycleScope.launch {
            val initialPainting = if (paintingId?.isNotEmpty() == true) {
                paintingsRepository.getPaintingById(paintingId).firstOrNull()
            } else {
                null
            }

            if (initialPainting != null)
                viewmodel.setActivePainting(initialPainting)

            viewmodel.paintingSubmitResult.collectLatest { result ->
                result?.onSuccess { id ->
                    android.util.Log.d("PaintingSubmit", "painting submitted with id $id")
                }?.onFailure { err ->
                    android.util.Log.e("PaintingSubmit", "painting submit failed: ${err.localizedMessage}")
                }
            }
        }

        setContent {

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (isGranted) {
                        android.util.Log.d("Permissions", "BODY_SENSORS permission granted.")
                        // Permission is granted. We can now expect sensor events
                        onResume()
                    } else {
                        android.util.Log.w("Permissions", "BODY_SENSORS permission denied.")
                    }
                }
            )

            // Trigger the permission request when the Composable is first displayed.
            LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }

            DrawitTheme {
                NewPaintingScreen(
                    viewmodel = viewmodel,

                    onPostSubmit = {
                        finish()
                    },

                    onPostSaveAndExit = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::viewmodel.isInitialized)
            viewmodel.registerSensorListeners(this)
    }

    override fun onPause() {
        super.onPause()
        if (::viewmodel.isInitialized)
            viewmodel.unregisterSensorListeners(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        android.util.Log.d("PaintingActivity", "onSensorChanged - sensor event received ${event.toString()}")
        if (event != null && ::viewmodel.isInitialized) viewmodel.onSensorEvent(event)
    }
}