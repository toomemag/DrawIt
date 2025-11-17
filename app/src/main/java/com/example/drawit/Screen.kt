package com.example.drawit

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object NewPainting : Screen("new_painting")
    data class PaintingDetail(val paintingId: String) : Screen("painting/$paintingId")
}