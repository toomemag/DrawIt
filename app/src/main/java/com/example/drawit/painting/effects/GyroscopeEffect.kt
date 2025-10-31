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
    description: String = "Apply your device's rotation to layers",
    inputOptions: List<String> = listOf("Pitch", "Yaw", "Roll")
) : BaseEffect<Float>(sensor, name, description, inputOptions) {
    private val ret: MutableList<Float> = mutableListOf(0f, 0f, 0f)

    override fun translateSensorEvent(sensorEvent: SensorEvent): List<Float> {
        val velX = sensorEvent.values[0]
        val velY = sensorEvent.values[1]
        val velZ = sensorEvent.values[2]

        ret[0] += velX
        ret[1] += velY
        ret[2] += velZ

        return ret
    }

    override fun transformInput(inputValues: List<Float>): List<Float> {
        val outputValues: MutableList<Float> = mutableListOf(0f, 0f, 0f)

        for (i in ret.indices) {
            // if we have null in list means its masked out
            if (inputValues[i] == null) continue

            // todo: right now we're setting strict values, in the future we schould have either
            //       relative
            //          - need to add customizable scale, eg coerce gyrosensor output -90 to 90, scale for 90
            //            eg for layer pos should be pos.x = width * coercedValue / scale
            //       or absolute
            //          - same example, pos.x = mutableValue
            //            mutable because we should still be able to scale the raw sensor return value
            //            by an arbitrary factor
            outputValues[i] = ret[i]
        }

        return outputValues.toList()
    }

    override fun reset() {
        for (i in ret.indices) ret[i] = 0f
    }
}