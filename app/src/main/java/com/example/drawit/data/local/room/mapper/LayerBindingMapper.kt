package com.example.drawit.data.local.room.mapper

import com.example.drawit.data.local.room.entity.LayerBindingEntity
import com.example.drawit.domain.model.LayerEffectBinding
import java.util.UUID

fun LayerEffectBinding.toEntity(layerId: String, effectType: Int): LayerBindingEntity {
    return LayerBindingEntity(
        id = UUID.randomUUID().toString(),
        layerId = layerId,
        effectType = effectType,
        effectOutputIndex = this.effectOutputIndex,
        layerTransformInput = this.layerTransformInput.name
    )
}

fun LayerBindingEntity.toDomain(): LayerEffectBinding {
    return LayerEffectBinding(
        effectOutputIndex = this.effectOutputIndex,
        layerTransformInput = com.example.drawit.domain.model.LayerTransformInput.valueOf(this.layerTransformInput)
    )
}