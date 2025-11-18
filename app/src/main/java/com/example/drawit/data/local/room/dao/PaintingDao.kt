package com.example.drawit.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.drawit.data.local.room.entity.LayerBindingEntity
import com.example.drawit.data.local.room.entity.PaintingEntity
import com.example.drawit.data.local.room.entity.PaintingLayerEntity
import com.example.drawit.data.local.room.entity.PaintingWithLayers
import kotlinx.coroutines.flow.Flow

@Dao
interface PaintingDao {
    // ksp warning, https://developer.android.com/reference/androidx/room/Transaction.html
    @Transaction
    @Query("SELECT * FROM paintings")
    fun getAllPaintings(): Flow<List<PaintingWithLayers>>

    @Transaction
    @Query("SELECT * FROM paintings WHERE id = :paintingId")
    fun getPaintingById(paintingId: String): Flow<PaintingWithLayers?>

    @Upsert
    suspend fun upsertPaintingEntity(paintingEntity: PaintingEntity)

    @Upsert
    suspend fun upsertLayerEntity(layer: PaintingLayerEntity)

    @Upsert
    suspend fun upsertBindingEntity(binding: LayerBindingEntity)

    @Upsert
    suspend fun upsertPaintingWithLayers(paintingWithLayers: PaintingWithLayers) {
        upsertPaintingEntity(paintingWithLayers.painting)
        paintingWithLayers.layers.forEach { layerWithBindingEntity ->
            upsertLayerEntity(layerWithBindingEntity.layer)
            layerWithBindingEntity.bindings.forEach { bindingEntity ->
                upsertBindingEntity(bindingEntity)
            }
        }
    }

    @Delete
    suspend fun deletePaintingEntity(painting: PaintingEntity)

    suspend fun deletePaintingWithLayers(paintingWithLayers: PaintingWithLayers) {
        deletePaintingEntity(paintingWithLayers.painting)
    }

    @Query("DELETE FROM paintings")
    suspend fun deleteAllPaintings()
}