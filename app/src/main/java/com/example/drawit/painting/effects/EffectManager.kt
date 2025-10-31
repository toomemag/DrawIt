package com.example.drawit.painting.effects

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

// todo: make global singleton
//       creates all midllewares for available sensors and maps to
//       corresponding effect classes
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

    // this literally came to me in a dream
    // what if we create a "context" in views from manager, so that all events are passed to context's sensorevent listener
    // context filters events out by sensor types and calls listening callbacks
    //
    // ctx = effectManager.createContext()
    // ctx.addSensorListener(Sensor.TYPE_GYROSCOPE, gyroscopeEffect -> {
    //      ...
    // }
    // ...
    // inside onSensorChanged in view we call
    //      ctx.onSensorChanged(sensorEvent)
    // context calls listening listeners 📞
    //
    // why not here? context would be tied to a view's lifecycle, not global manager
    // so that way we don't have to worry about unregistering listeners when view is destroyed etc
    fun createContext(): EffectContext {
        return EffectContext(this)
    }

    fun getEffect(sensorType: Int): BaseEffect<*>? {
        return availableEffects[sensorType]
    }

    fun getEffects(): MutableCollection<BaseEffect<*>> {
        return availableEffects.values
    }

    fun getSensorManager(): SensorManager {
        return sensorManager
    }

    // https://stackoverflow.com/a/45952201
    // maybe it's useful at any point
    inline fun <reified T : BaseEffect<*>> getEffect(): T? {
        val entry = sensorToEffectClass.entries.firstOrNull { it.value == T::class.java } ?: return null
        return availableEffects[entry.key] as? T
    }
}
