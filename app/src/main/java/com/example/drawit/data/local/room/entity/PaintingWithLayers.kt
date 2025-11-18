package com.example.drawit.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PaintingWithLayers (
    @Embedded val painting: PaintingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "paintingId",
        entity = PaintingLayerEntity::class
    )
    val layers: List<LayerWithBindings>
)