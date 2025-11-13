package com.example.drawit.painting.effects

import android.hardware.Sensor
import android.hardware.SensorEvent

// each class can have a different return value
// for example gyro has a vector (xyz,pyr)
// light has just a float/int (idk havent delve into docs that deep yet)
/**
 * Base effect class, to be extended by all effect classes
 * @param T The type of the effect's output values
 * @param sensor The sensor associated with the effect
 * @param name The name of the effect
 * @param description A brief description of the effect
 * @param inputOptions A list of input options available for the effect
 *
 * @desc Each effect is global and managed by the EffectManager. Paintings can request
 *       linked effects from the EffectManager to apply transformations based on sensor data.
 */
abstract class BaseEffect<T>(
    protected var sensor: Sensor,
    protected var name: String,
    protected var description: String,
    protected var inputOptions: List<String>
) {
    /**
     * Translate a SensorEvent into a list of effect output values
     * @param sensorEvent The SensorEvent to translate
     * @return A list of translated effect output values
     */
    abstract fun translateSensorEvent(sensorEvent: SensorEvent): List<T>

    /**
     * Transform an input value based on the effect's translated sensor values
     * @param index The index of the effect output value to use for transformation
     * @return The transformed input value
     */
    abstract fun getTransformInput(index: Int): T

    /**
     * Reset the effect's internal state
     */
    abstract fun reset()

    /**
     * Get the list of effect input options
     * @return A list of effect input option names
     *
     * @desc We need a way to get layer inputs to map to layer outputs (xy, scale, rotation, etc.).
     *       More importantly we need to let the UI know what inputs are available for binding.
     */
    fun getEffectOutputOptions(): List<String> {
        return inputOptions
    }

    /**
     * Get the name of the effect
     * @return The effect's name
     */
    fun getEffectName(): String {
        return name
    }

    /**
     * Get the description of the effect
     * @return The effect's description
     */
    fun getEffectDescription(): String {
        return description
    }

    /**
     * Get the sensor type associated with the effect
     * @return The sensor type
     */
    fun getEffectType(): Int {
        return sensor.type
    }
}