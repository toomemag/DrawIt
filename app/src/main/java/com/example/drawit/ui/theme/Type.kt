package com.example.drawit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.drawit.R

var drawnFont = FontFamily(
    Font(R.font.drawn, FontWeight.Light)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Bold, fontSize = 52.sp),
    displayMedium = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal, fontSize = 40.sp),
    displaySmall = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal, fontSize = 28.sp),

    titleLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    titleMedium = TextStyle(fontFamily = drawnFont),
    titleSmall = TextStyle(fontFamily = drawnFont),

    bodyLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = drawnFont),
    bodySmall = TextStyle(fontFamily = drawnFont),

    labelLarge = TextStyle(fontFamily = drawnFont),
    labelMedium = TextStyle(fontFamily = drawnFont),
    labelSmall = TextStyle(fontFamily = drawnFont)
)