package com.example.drawit

import android.app.Application

class DrawItApplication : Application() {
    val navCoordinator by lazy { NavCoordinator() }
}
