package com.example.drawit.data.remote.firestore.model

import com.google.firebase.Timestamp

data class PaintingFirestoreDto(
    val id: String,
    val layers: List< LayerFirestoreDto >,
    val createdAt: Timestamp,
    val mode: String,
    val size: Int,
    val theme: String,
    val timeTaken: Long,
    val userId: String
)