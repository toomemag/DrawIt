package com.example.drawit.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LayerWithBindings(
    @Embedded val layer: PaintingLayerEntity,
    @Relation(parentColumn = "id", entityColumn = "layerId")
    val bindings: List<LayerBindingEntity>
)