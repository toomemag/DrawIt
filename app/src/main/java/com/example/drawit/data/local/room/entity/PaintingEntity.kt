package com.example.drawit.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "paintings")
data class PaintingEntity (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timeTaken: Long,
    val size: Int,
    val theme: String,
    val mode: String
)