package com.example.drawit.painting.effects

import android.hardware.Sensor
import android.hardware.SensorEvent

data class Vector(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
)

// https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-overloads/
// "Instructs the Kotlin compiler to generate overloads for this function that substitute default parameter values."
// without it name can't be omitted when only passing a sensor object
class GyroscopeEffect @JvmOverloads constructor(
    sensor: Sensor,
    name: String = "Gyroscope",
    description: String = "Apply your device's rotation to layers"
) : BaseEffect<Vector>(sensor, name, description) {
    private val ret: Vector = Vector()

    override fun translateSensorEvent(sensorEvent: SensorEvent): Vector {
        val velX = sensorEvent.values[0]
        val velY = sensorEvent.values[1]
        val velZ = sensorEvent.values[2]

        ret.x += velX
        ret.y += velY
        ret.z += velZ

        return ret
    }

    override fun reset() {
        ret.x = 0f
        ret.y = 0f
        ret.z = 0f
    }

    override fun getInputOptions(): List<String> {
        return listOf("Pitch", "Yaw", "Roll")
    }
}