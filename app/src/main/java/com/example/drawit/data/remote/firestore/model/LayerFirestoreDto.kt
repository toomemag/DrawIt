package com.example.drawit.data.remote.firestore.model

data class LayerFirestoreDto(
    val id: String = "",
    val bindings: Map< String, List< LayerBindingFirestoreDto > > = emptyMap(),
    val bitmap: String = "",
)
