package com.example.drawit.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun invert(color: Color): Color {
    val r = 1f - color.red
    val g = 1f - color.green
    val b = 1f - color.blue
    val a = color.alpha
    return Color( r, g, b, a)
}

fun modify(color: Color, r: Float? = null, g: Float? = null, b: Float? = null, a: Float? = null): Color {
    return Color(
        red = r ?: color.red,
        green = g ?: color.green,
        blue = b ?: color.blue,
        alpha = a ?: color.alpha
    )
}

fun darken(color: Color, factor: Float): Color {
    return Color(
        red = color.red * (1 - factor),
        green = color.green * (1 - factor),
        blue = color.blue * (1 - factor),
        alpha = color.alpha
    )
}

fun lighten(color: Color, factor: Float): Color {
    return Color(
        red = color.red + (1 - color.red) * factor,
        green = color.green + (1 - color.green) * factor,
        blue = color.blue + (1 - color.blue) * factor,
        alpha = color.alpha
    )
}

fun dynamicLightenDarken(color: Color, scale: Float): Color {
    // todo: rework, might have to look into color theory a bit
    //       one resource could be Material You
    val l = color.luminance()
    val light = l > .8f
    val amount = if ( light ) ( l - .5f ) / .5f else ( .5f - l ) / .5f

    return if ( light ) darken(color, amount * scale) else lighten(color, amount * scale)
}