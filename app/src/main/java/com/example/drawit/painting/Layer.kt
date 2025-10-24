package com.example.drawit.painting

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

data class Layer(
    // debug names more than anything
    val name: String = "Layer",
    var bitmap: Bitmap = createBitmap(128, 128),
    // cant access canvasmanager from activity view
    var isActive: Boolean = true
)