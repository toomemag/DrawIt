package com.example.drawit.painting.effects


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log

class LightSensorEffect @JvmOverloads constructor(
    sensor: Sensor,
    name: String = "Light",
    description: String = "Reacts to the ambient light level.",
    // This effect provides one output: the light level in lux.
    inputOptions: List<String> = listOf("Illuminance (lux)")
) : BaseEffect<Float>(sensor, name, description, inputOptions) {
    // Stores the most recent light sensor value.
    private var illuminance: Float = 0f

    /**
     * Translates a light sensor event into a single illuminance value.
     * @param sensorEvent The sensor event to translate.
     * @return A list containing the single illuminance value.
     */
    override fun translateSensorEvent(sensorEvent: SensorEvent): List<Float> {
        illuminance = sensorEvent.values[0]
        illuminance = illuminance*0.02f // A big number, gonna tune it down
        return listOf(illuminance)
    }

    /**
     * Returns the stored illuminance value.
     * @param index The index of the output value (always 0 for this effect).
     * @return The illuminance value.
     */
    override fun getTransformInput(index: Int): Float {
        return illuminance
    }

    /**
     * Resets the illuminance value to its default.
     */
    override fun reset() {
        illuminance = 0f
    }
}
