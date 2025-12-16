package com.example.drawit.data.remote.firestore.model

data class LayerFirestoreDto(
    val id: String,
    val bindings: Map< Int, List< LayerBindingFirestoreDto > >,
    val bitmap: String,
)
