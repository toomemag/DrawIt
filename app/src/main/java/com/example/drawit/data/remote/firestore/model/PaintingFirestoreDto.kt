package com.example.drawit.data.remote.firestore.model

import com.google.firebase.Timestamp

data class PaintingFirestoreDto(
    val id: String = "",
    val layers: List< LayerFirestoreDto > = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val mode: String = "none",
    val size: Int = 0,
    val theme: String = "none",
    val timeTaken: Long = 0L,
    val userId: String = "",
)