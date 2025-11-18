package com.example.drawit.painting.effects

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Context for effects to manage sensor listeners and other shared resources
 * @param effectManager The EffectManager instance to manage effects
 *
 * @property sensorListeners A map of sensor type to list of listeners
 */
class EffectContext(
    private var effectManager: EffectManager
) {
    val sensorListeners: MutableMap<Int, MutableList<(BaseEffect<*>, SensorEvent) -> Unit>> = mutableMapOf()

    /**
     * Adds a sensor listener for a specific sensor type and effect type
     * @param sensorType The sensor type to listen for
     * @param listener The listener function to call when the sensor event occurs
     *
     * @note reified type parameter E is used to enforce the effect type at runtime
     */
    inline fun addSensorListener(sensorType: Int, crossinline listener: (BaseEffect<*>, sensorEvent: SensorEvent) -> Unit) {
        val listeners = sensorListeners.getOrPut(sensorType) { mutableListOf() }
        android.util.Log.d("EffectContext", "addSensorListener registering event listener for sensor type $sensorType")

        listeners.add { baseEffect, sensorEventFromListener ->
            // enforce sensor type
            listener(baseEffect, sensorEventFromListener)
        }
    }

    /**
     * Resets all effects managed by the EffectManager
     */
    fun resetAllEffects() {
        for (effect in effectManager.availableEffects.values) {
            effect.reset()
        }
    }

    /**
     * Removes a specific sensor listener for a sensor type
     * @param sensorType The sensor type to remove the listener from
     * @param listener The listener function to remove
     *
     * @note This only removes the exact listener reference provided
     */
    fun removeSensorListener(sensorType: Int, listener: (BaseEffect<*>, SensorEvent) -> Unit) {
        sensorListeners[sensorType]?.remove(listener)
    }

    /**
     * Removes all sensor listeners for a specific sensor type
     * @param sensorType The sensor type to remove all listeners from
     */
    fun removeSensorListeners(sensorType: Int) {
        sensorListeners.remove(sensorType)
    }

    /**
     * Registers sensor listeners for all sensor types that have registered listeners
     * @param listener The SensorEventListener to register with the SensorManager
     */
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

    /**
     * Unregisters all sensor listeners from the SensorManager
     */
    fun removeAllSensorListeners() {
        for (key in sensorListeners.keys) {
            sensorListeners.remove(key)
        }
    }

    /**
     * Unregisters sensor listeners from the SensorManager
     * @param listener The SensorEventListener to unregister
     */
    fun unregisterSensorListeners(listener: SensorEventListener) {
        val sensorManager = effectManager.getSensorManager()
        sensorManager.unregisterListener(listener)
        android.util.Log.d("EffectContext", "unregisterSensorListeners - unregistered listener $listener")
    }

    /**
     * Called when a sensor event occurs
     * @param sensorEvent The SensorEvent that occurred
     */
    fun onSensorChanged(sensorEvent: SensorEvent) {
        val sensorType: Int = sensorEvent.sensor.type
        val effect: BaseEffect<*> = effectManager.getEffect(sensorType) ?: return

        val listeners = sensorListeners[sensorType] ?: return

        for (listener in listeners) {
            try {
                android.util.Log.d("EffectContext", "onSensorChanged - invoking listener for sensor \"${sensorEvent.sensor.name}\" (return=[${sensorEvent.values.joinToString(", ")}])")
                listener(effect, sensorEvent)
            } catch (e: Exception) {
                android.util.Log.e("EffectContext", "onSensorChanged - sensor \"${sensorEvent.sensor.name}\" listener errored", e)
            }
        }
    }

    /**
     * Destroys the EffectContext and clears all sensor listeners
     */
    fun destroy() {
        sensorListeners.clear()
    }
}