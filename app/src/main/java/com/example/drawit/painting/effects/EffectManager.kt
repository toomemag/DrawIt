import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.example.drawit.painting.effects.BaseEffect
import com.example.drawit.painting.effects.GyroscopeEffect

// todo: make global singleton
//       creates all midllewares for available sensors and maps to
//       corresponding effect classes
class EffectManager(private val context: Context) {
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
                android.util.Log.i("EffectManager", "no effect class mapped for sensor: ${sensor.name} of type ${sensor.type}")
                continue
            }

            // add first effect wrapper for sensor to map
            availableEffects[sensor.type] = sensorToEffectClass[sensor.type]!!.getDeclaredConstructor(Sensor::class.java).newInstance(sensor)
        }

        // maybe map sensors to corresponding effect classes here later
    }

    // we'd have to cast and check for sensor in View
    // a better solution would be to have a "subscribe" system
    // so a view can subscribe to whatever effect it wants
    fun handleSensorEvent(sensorEvent: android.hardware.SensorEvent): Any? {
        // do we have an effect for this sensor type?
        val effect = availableEffects[sensorEvent.sensor.type] ?: return null

        // delegate to effect
        return effect.translateSensorEvent(sensorEvent)
    }

    fun getAvailableEffects(): Map<Int, BaseEffect<*>> {
        return availableEffects.toMap()
    }
}