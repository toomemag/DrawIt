package com.example.drawit.painting.effects

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class EffectContext(
    private var effectManager: EffectManager
) {
    // sensor type -> list of listeners
    val sensorListeners: MutableMap<Int, MutableList<(BaseEffect<*>, SensorEvent) -> Unit>> = mutableMapOf()

    inline fun <reified E : BaseEffect<*>> addSensorListener(sensorType: Int, crossinline listener: (E, sensorEvent: SensorEvent) -> Unit) {
        val listeners = sensorListeners.getOrPut(sensorType) { mutableListOf() }
        listeners.add { baseEffect, sensorEventFromListener ->
            // enforce sensor type
            if (baseEffect is E) {
                listener(baseEffect, sensorEventFromListener)
            }
        }
    }

    fun resetAllEffects() {
        for (effect in effectManager.availableEffects.values) {
            effect.reset()
        }
    }

    fun removeSensorListener(sensorType: Int, listener: (BaseEffect<*>, SensorEvent) -> Unit) {
        sensorListeners[sensorType]?.remove(listener)
    }

    fun removeSensorListeners(sensorType: Int) {
        sensorListeners.remove(sensorType)
    }

    // both unregister and register need a "context"
    // provided by a view which extends SensorEventListener
    fun registerSensorListeners(listener: SensorEventListener) {
        val sensorManager = effectManager.getSensorManager()
        for (sensorType in sensorListeners.keys) {
            val sensor = sensorManager.getDefaultSensor(sensorType)
            if (sensor != null) {
                // SENSOR_DELAY_NORMAL  - 200_000 microseconds aka 200 ms
                // SENSOR_DELAY_UI      -  60_000 microseconds aka 60 ms
                // SENSOR_DELAY_GAME    -  20_000 microseconds aka 20 ms
                // SENSOR_DELAY_FASTEST -       0 microseconds
                sensorManager.registerListener(
                    listener,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                android.util.Log.d("EffectContext", "registerSensorListeners - registered listener for sensor type $sensorType")
            } else {
                android.util.Log.w("EffectContext", "registerSensorListeners - no sensor found for type $sensorType")
            }
        }
    }

    fun unregisterSensorListeners(listener: SensorEventListener) {
        val sensorManager = effectManager.getSensorManager()
        sensorManager.unregisterListener(listener)
        android.util.Log.d("EffectContext", "unregisterSensorListeners - unregistered listener $listener")
    }

    fun onSensorChanged(sensorEvent: SensorEvent) {
        val sensorType: Int = sensorEvent.sensor.type
        val effect: BaseEffect<*>? = effectManager.getEffect(sensorType)

        if (effect == null) return

        val listeners = sensorListeners[sensorType] ?: return

        for (listener in listeners) {
            try {
                listener(effect, sensorEvent)
            } catch (e: Exception) {
                android.util.Log.e("EffectContext", "onSensorChanged - sensor \"${sensorEvent.sensor.name}\" listener errored", e)
            }
        }
    }

    fun destroy() {
        sensorListeners.clear()
    }
}