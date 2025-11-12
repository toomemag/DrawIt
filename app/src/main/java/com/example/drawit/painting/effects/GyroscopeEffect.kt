package com.example.drawit.painting.effects

import android.hardware.Sensor
import android.hardware.SensorEvent

/**
 * Gyroscope effect class, extends BaseEffect
 * Translates gyroscope sensor events into cumulative pitch, yaw, roll values
 */

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

    /**
     * Translate sensor event into cumulative pitch, yaw, roll values
     * @param sensorEvent The sensor event to translate
     * @return A list of cumulative pitch, yaw, roll values
     *
     * @desc This method takes a gyroscope sensor event and updates the cumulative pitch, yaw, and
     *       roll values by adding the angular velocities from the sensor event to the existing values.
     *       It returns the updated list of cumulative values.
     *
     * @note This needs to be called every event before translations, as the return
     *       values would be stale otherwise in transformInput.
     */
    override fun translateSensorEvent(sensorEvent: SensorEvent): List<Float> {
        val velX = sensorEvent.values[0]
        val velY = sensorEvent.values[1]
        val velZ = sensorEvent.values[2]

        ret[0] += velX
        ret[1] += velY
        ret[2] += velZ

        return ret
    }

    /**
     * Transform input based on cumulative pitch, yaw, roll values
     * @param index The index of the input to transform (0: Pitch, 1: Yaw, 2: Roll)
     * @return The transformed value
     */
    override fun getTransformInput(index: Int): Float {
        // todo add scaling/px etc
        return ret[index]
    }

    override fun reset() {
        for (i in ret.indices) ret[i] = 0f
    }
}