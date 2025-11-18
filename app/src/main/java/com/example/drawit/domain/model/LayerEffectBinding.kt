package com.example.drawit.domain.model

/**
 * Binds an effect output to a layer transform input
 * @param effectOutputIndex The index of the effect output to bind
 * @param layerTransformInput The layer transform input to bind to
 */
data class LayerEffectBinding(
    val id: String = "",
    val layerId: String = "",
    var effectOutputIndex: Int,
    var layerTransformInput: LayerTransformInput,
)