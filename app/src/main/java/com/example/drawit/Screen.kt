package com.example.drawit

sealed class Screen(val route: String) {
    object AuthenticationScreen : Screen("auth_screen")
    object MainScreen : Screen("main_screen")
    object NewPainting : Screen("new_painting")

    data class PaintingDetail(val paintingId: String) : Screen("painting/$paintingId")
}