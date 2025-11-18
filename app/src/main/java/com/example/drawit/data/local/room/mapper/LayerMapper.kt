package com.example.drawit.data.local.room.mapper

import android.graphics.BitmapFactory
import android.util.Base64
import com.example.drawit.data.local.room.entity.LayerBindingEntity
import com.example.drawit.data.local.room.entity.PaintingLayerEntity
import com.example.drawit.domain.model.Layer
import com.example.drawit.domain.model.LayerEffectBinding
import com.example.drawit.domain.model.LayerTransformInput
import java.nio.ByteBuffer
import java.util.UUID

fun Layer.toEntities(paintingId: String): Pair<PaintingLayerEntity, List<LayerBindingEntity>> {
    val bytes = ByteArray(this.bitmap.byteCount)
    this.bitmap.copyPixelsToBuffer(ByteBuffer.wrap(bytes))
    val b64encoded = Base64.encodeToString(bytes, Base64.DEFAULT)

    val layerId = UUID.randomUUID().toString()
    val layerEntity = PaintingLayerEntity(
        id = layerId,
        paintingId = paintingId,
        bitmap = b64encoded
    )

    val bindingEntities = mutableListOf<LayerBindingEntity>()

    for ((effectType, bindings) in this.effectBindings) {
        for (binding in bindings) {
            bindingEntities.add(LayerBindingEntity(
                id = UUID.randomUUID().toString(),
                layerId = layerId,
                effectType = effectType,
                effectOutputIndex = binding.effectOutputIndex,
                layerTransformInput = binding.layerTransformInput.name
            ))
        }
    }

    return Pair(layerEntity, bindingEntities)
}

fun PaintingLayerEntity.toDomain(bindings: List<LayerBindingEntity>): Layer {
    val bytesDecoded = Base64.decode(this.bitmap, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(bytesDecoded, 0, bytesDecoded.size)
    val effectMap = mutableMapOf<Int, MutableList<LayerEffectBinding>>()

    for (binding in bindings) {
        val inputEnum = LayerTransformInput.valueOf(binding.layerTransformInput)
        val list = effectMap.getOrPut(binding.effectType) { mutableListOf() }
        list.add(LayerEffectBinding(
            effectOutputIndex = binding.effectOutputIndex,
            layerTransformInput = inputEnum
        ))
    }

    return Layer(
        bitmap = bitmap,
        effectBindings = effectMap,
    )
}