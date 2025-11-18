package com.example.drawit.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "layer_bindings",
    foreignKeys = [
        ForeignKey(
            entity = PaintingLayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["layerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["layerId"])]
)
data class LayerBindingEntity (
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    // effects constructed into map when read in
    val effectType: Int,
    val layerId: String,
    val effectOutputIndex: Int,
    val layerTransformInput: String, // e.g. "X_POS", "Y_POS" wv
)