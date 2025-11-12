package com.example.drawit.painting.effects

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

/**
 * Global singleton for managing available sensor effects.
 * Creates all middlewares for available sensors and maps to corresponding effect classes.
 * @param context Application context for getting sensor manager
 *
 * EffectManager member fields descriptions
 * @property sensorManager SensorManager instance for accessing device sensors
 * @property availableEffects Map of sensor type to corresponding BaseEffect instance
 * @property sensorToEffectClass Map of sensor type to corresponding BaseEffect class
 *
 * Idea behind EffectManager and EffectContext:
 * EffectManager holds global effect instances for each sensor type.
 * EffectContext is created per view/component that needs to listen to sensor events and
 *  manages sensor event listeners and routes events to registered callbacks.
 *
 * Example usage:
 *      val ctx = effectManager.createContext()
 *      ctx.addSensorListener(Sensor.TYPE_GYROSCOPE) { gyroscopeEffect, sensorEvent ->
 *          // handle gyroscope sensor event
 *      }
 *
 * In the view's onSensorChanged:
 *      ctx.onSensorChanged(sensorEvent)
 *
 * Context calls listening listeners 📞
 */

class EffectManager(
    private val context: Context
) {
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java) ?: throw Exception("no sensor manager")

    // map sensor type to first sensor
    var availableEffects: MutableMap<Int, BaseEffect<*>> = mutableMapOf()

    // https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview#kotlin
    // names set in effects, keeping old map here to copypaste name later
//    var sensorTypeToName: Map<Int, String> = mapOf(
//        Sensor.TYPE_ACCELEROMETER to "Accelerometer",
//        Sensor.TYPE_AMBIENT_TEMPERATURE to "Ambient Temperature",
//        Sensor.TYPE_GRAVITY to "Gravity",
//        Sensor.TYPE_GYROSCOPE to "Gyroscope",
//        Sensor.TYPE_LIGHT to "Light Sensor",
//        Sensor.TYPE_LINEAR_ACCELERATION to "Linear Acceleration",
//        Sensor.TYPE_MAGNETIC_FIELD to "Magnetometer",
//        Sensor.TYPE_ORIENTATION to "Orientation", // deprecated
//        Sensor.TYPE_PRESSURE to "Barometer",
//        Sensor.TYPE_PROXIMITY to "Proximity",
//        Sensor.TYPE_RELATIVE_HUMIDITY to "Humidity",
//        Sensor.TYPE_ROTATION_VECTOR to "Rotation Vector",
//        Sensor.TYPE_TEMPERATURE to "Temperature" // deprecated
//    )

    var sensorToEffectClass: Map<Int, Class<out BaseEffect<*>>> = mapOf(
        Sensor.TYPE_GYROSCOPE to GyroscopeEffect::class.java
    )

    init {
        // https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        for (sensor in sensors) {
            if (!sensorToEffectClass.containsKey(sensor.type)) {
                android.util.Log.i("EffectManager", "init - no effect class mapped for sensor: ${sensor.name} of type ${sensor.type}")
                continue
            }

            // add first effect wrapper for sensor to map
            availableEffects[sensor.type] = sensorToEffectClass[sensor.type]!!.getDeclaredConstructor(Sensor::class.java).newInstance(sensor)
            android.util.Log.i("EffectManager", "init - added effect ${sensorToEffectClass[sensor.type]} for sensor ${sensor.name}")
        }

        // maybe map sensors to corresponding effect classes here later
    }

    /**
     * Creates a new EffectContext associated with this EffectManager.
     * EffectContext manages sensor event listeners and effect instances for a specific view or component.
     * @return A new EffectContext instance
     */
    fun createContext(): EffectContext {
        return EffectContext(this)
    }

    /**
     * Gets the BaseEffect instance for the specified sensor type.
     * @param sensorType The sensor type constant (e.g., Sensor.TYPE_GYROSCOPE)
     * @return The corresponding BaseEffect instance, or null if not available
     */
    fun getEffect(sensorType: Int): BaseEffect<*>? {
        return availableEffects[sensorType]
    }

    /**
     * Gets all available BaseEffect instances.
     * @return A collection of all BaseEffect instances
     */
    fun getEffects(): MutableCollection<BaseEffect<*>> {
        return availableEffects.values
    }

    /**
     * Gets the SensorManager instance.
     * @return The SensorManager
     */
    fun getSensorManager(): SensorManager {
        return sensorManager
    }

    /**
     * @unused https://stackoverflow.com/a/45952201 - may be useful at some point
     * Gets the BaseEffect instance of the specified type.
     * @return The corresponding BaseEffect instance of type T, or null if not available
     */
    inline fun <reified T : BaseEffect<*>> getEffect(): T? {
        val entry = sensorToEffectClass.entries.firstOrNull { it.value == T::class.java } ?: return null
        return availableEffects[entry.key] as? T
    }
}
