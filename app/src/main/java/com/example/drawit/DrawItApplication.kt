package com.example.drawit

import android.app.Application
import com.example.drawit.data.local.AppDatabaseProvider
import com.example.drawit.data.local.room.repository.PaintingsRepository

class DrawItApplication : Application() {
    val navCoordinator by lazy { NavCoordinator() }
    private val database by lazy { AppDatabaseProvider.getDatabase(this) }

    val paintingsRepository by lazy {
        PaintingsRepository(
            paintingDao = database.paintingDao()
        )
    }
}
