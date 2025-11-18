package com.example.drawit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // primary action buttons
    primary = Color(0xFF6D9BFF),
    onPrimary = Color(0xFF0C1A33),

    // light blue buttons (secondary actions)
    secondary = Color(0xFF8FB0FF),
    onSecondary = Color(0xFF0D1526),

    // cancel buttons / neutral actions
    tertiary = Color(0xFF3A3A3A),
    onTertiary = Color(0xFFE0E0E0),

    // main background
    background = Color(0xFF121212),
    // border, text directly on background
    onBackground = Color(0xEB777777),

    // dialogs
    surface = Color(0xFF2A2A2A),
    // dialog text
    onSurface = Color(0xFFECECEC),
    // dialog text description
    onSurfaceVariant = Color(0xFFB3B3B3),

    // paint tools
    primaryContainer = Color(0xFF2B2F3A),
    onPrimaryContainer = Color(0xFF9EC0FF),
    secondaryContainer = Color(0xFF1F2530),
    onSecondaryContainer = Color(0xFFB5C8FF),

    error = Color(0xFFCF6679),
    onError = Color(0xFF1A0C0C),
)

private val LightColorScheme = lightColorScheme(
    // primary action buttons
    primary = Color(0xFF3060d7),
    onPrimary = Color( 0xFFFFFFFF),

    // light blue buttons
    secondary = Color(0xFF6786d2),
    onSecondary = Color(0xffffffff),

    // cancel buttons
    tertiary = Color(0xFFE7E7E7),
    onTertiary = Color(0xFF444444),

    // main background
    background = Color(0xFFffffff),
    // border, text directly on background
    onBackground = Color(0x40000000),

    // dialogs
    surface = Color(0xFFd9d9d9),
    // dialog text
    onSurface = Color(0xFF000000),
    // dialog text description
    onSurfaceVariant = Color(0xFF575757),

    // paint tools
    primaryContainer = Color(0xFFd3d3d3),
    onPrimaryContainer = Color(0xFF3060d7),
    secondaryContainer = Color(0x00d3d3d3),
    onSecondaryContainer = Color(0xFF666666),

    error = Color(0xFFe06d6d),
    onError = Color(0xFFffffff),
)

@Composable
fun DrawitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}