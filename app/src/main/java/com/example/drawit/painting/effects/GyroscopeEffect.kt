package com.example.drawit.painting.effects
import android.hardware.Sensor
import android.hardware.SensorEvent

data class Vector(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
)

class GyroscopeEffect(
    sensor: Sensor,
    name: String = "Gyroscope"
) : BaseEffect<Vector>(sensor, name) {
    private val ret: Vector = Vector()

    override fun translateSensorEvent(sensorEvent: SensorEvent): Vector {
        val velX = sensorEvent.values[0];
        val velY = sensorEvent.values[0];
        val velZ = sensorEvent.values[0];

        ret.x += velX;
        ret.y += velY;
        ret.z += velZ;

        return ret;
    }
}