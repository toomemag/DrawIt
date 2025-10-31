package com.example.drawit.painting.effects

import android.hardware.Sensor
import android.hardware.SensorEvent

// each class can have a different return value
// for example gyro has a vector (xyz,pyr)
// light has just a float/int (idk havent delve into docs that deep yet)
abstract class BaseEffect<T>(
    protected var sensor: Sensor,
    protected var name: String,
    protected var description: String
) {
    // what do we need for effects
    // initial idea was to have a mapping input -> output
    // so, each effect is global, lives under effectmanager
    // from there, paintings can ask linked effects for their effect object from effectmanager
    // and do their business

    // each sensor has a different values field output from their event
    // these middleware classes translate them
    // eventlisteners have to be set up in views though, since I think that's
    // the only context we can listen for sensor updates
    abstract fun translateSensorEvent(sensorEvent: SensorEvent): T
    abstract fun reset()

    // we need a way to get layer inputs to map to layer outputs (xy, scale, rotation, have to think what else)
    // for eg gyroscope we have xyz rotation values
    // layer should store all data about its' effects
    // we only deal with ""raw"" data in effect classes meaning simple input -> output mapping
    // what we do need is to let the layer know what inputs are available
    abstract fun getInputOptions(): List<String>
    // when an effect input, eg. gyro.yaw is applied to layer.x, all others not set
    // we need to pass [null, inputValue, null] to <something> to get the output parameter we want to modify
    // this means layers would need transform functions for each applied effect (but how can we implement that, input types vary)

    fun getEffectName(): String {
        return name
    }

    fun getEffectDescription(): String {
        return description
    }

    fun getEffectType(): Int {
        return sensor.type
    }
}