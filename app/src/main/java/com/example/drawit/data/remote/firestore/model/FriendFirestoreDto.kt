package com.example.drawit.data.remote.firestore.model

import com.google.firebase.Timestamp

data class FriendFirestoreDto(
    val ids: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)