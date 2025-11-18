package com.example.drawit.domain.model

/**
 * Represents a layer transform input option
 * Used in LayerEffectBinding to map effect outputs to layer transform inputs
 * eg. map gyro yaw to layer x position
 */
enum class LayerTransformInput {
    X_POS,
    Y_POS,
    ROTATION,
    SCALE,
    ALPHA
}