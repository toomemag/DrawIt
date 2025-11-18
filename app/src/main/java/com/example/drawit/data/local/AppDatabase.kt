package com.example.drawit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.drawit.data.local.room.dao.PaintingDao
import com.example.drawit.data.local.room.entity.LayerBindingEntity
import com.example.drawit.data.local.room.entity.PaintingEntity
import com.example.drawit.data.local.room.entity.PaintingLayerEntity

@Database(
    entities = [
        PaintingEntity::class,
        PaintingLayerEntity::class,
        LayerBindingEntity::class
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paintingDao(): PaintingDao
}