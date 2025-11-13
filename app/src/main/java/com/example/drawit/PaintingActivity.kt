package com.example.drawit

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.drawit.painting.effects.EffectManager
import com.example.drawit.ui.screens.NewPaintingScreen
import com.example.drawit.ui.theme.DrawitTheme
import com.example.drawit.ui.viewmodels.NewPaintingVMFactory
import com.example.drawit.ui.viewmodels.NewPaintingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaintingActivity: ComponentActivity(), SensorEventListener {
    private val viewmodel: NewPaintingViewModel by viewModels {
        NewPaintingVMFactory(effectManager)
    }
    private lateinit var effectManager: EffectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        effectManager = EffectManager(this)

        lifecycleScope.launch {
            viewmodel.paintingSubmitResult.collectLatest { result ->
                result?.onSuccess { id ->
                    android.util.Log.d("PaintingSubmit", "painting submitted withid $id")
                }?.onFailure { err ->
                    android.util.Log.e("PaintingSubmit", "painting submit failed: ${err.localizedMessage}")
                }
            }
        }

        lifecycleScope.launch {
            viewmodel.layers.collectLatest {

            }
        }

        setContent {
            DrawitTheme {
                NewPaintingScreen(
                    viewmodel = viewmodel,

                    onPostSubmit = {

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
        viewmodel.registerSensorListeners(this)
    }

    override fun onPause() {
        super.onPause()
        viewmodel.unregisterSensorListeners(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) { }
}