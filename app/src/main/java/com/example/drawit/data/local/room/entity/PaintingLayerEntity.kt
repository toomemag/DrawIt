package com.example.drawit.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(
    tableName = "layers",
    foreignKeys = [
        ForeignKey(
            entity = PaintingEntity::class,
            parentColumns = ["id"],
            childColumns = ["paintingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paintingId"])]
)
data class PaintingLayerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val paintingId: String,
    val bitmap: String, // b64 encoded bitmap
)