package com.example.drawit.data.remote.firestore.model

data class LayerBindingFirestoreDto(
    val id: String = "",
    val effectInputIndex: Int = -1,
    val layerTransformInput: String = "X_POS",
)
