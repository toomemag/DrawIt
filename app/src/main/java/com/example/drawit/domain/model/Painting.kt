package com.example.drawit.domain.model

data class Painting(
    val id: String,
    val size: Int = 128,
    val layers: MutableList<Layer> = mutableListOf(),
    var timeTaken: Long = 0L,
    val theme: String = "",
    val mode: String = "",
)
