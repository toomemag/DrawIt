package com.example.drawit.painting.effects

import android.hardware.Sensor
import android.hardware.SensorEvent

// each class can have a different return value
// for example gyro has a vector (xyz,pyr)
// light has just a float/int (idk havent delve into docs that deep yet)
abstract class BaseEffect<T>(
    protected var sensor: Sensor,
    protected var name: String
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
}