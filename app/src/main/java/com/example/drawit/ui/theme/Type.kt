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
    headlineLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Bold, fontSize = 45.sp),
    headlineMedium = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.SemiBold, fontSize = 37.sp),
    headlineSmall = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.SemiBold, fontSize = 35.sp),

    displayLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Bold, fontSize = 40.sp),
    displayMedium = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal, fontSize = 35.sp),

    titleLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    titleMedium = TextStyle(fontFamily = drawnFont, fontSize = 26.sp),
    titleSmall = TextStyle(fontFamily = drawnFont, fontSize = 23.sp),

    bodyLarge = TextStyle(fontFamily = drawnFont, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = drawnFont, fontSize = 26.sp),
    bodySmall = TextStyle(fontFamily = drawnFont, fontSize = 23.sp),

    labelLarge = TextStyle(fontFamily = drawnFont, fontSize = 30.sp),
    labelMedium = TextStyle(fontFamily = drawnFont, fontSize = 26.sp),
    labelSmall = TextStyle(fontFamily = drawnFont, fontSize = 23.sp)
)