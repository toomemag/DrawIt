package com.example.drawit.domain.model

data class Painting(
    val size: Int = 128,
    val layers: MutableList<Layer> = mutableListOf(),
    val timeTaken: Long = 0L,
    val theme: String = "",
    val mode: String = "",
)
