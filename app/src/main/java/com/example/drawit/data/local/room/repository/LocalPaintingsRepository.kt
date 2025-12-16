package com.example.drawit.data.local.room.repository

import com.example.drawit.data.local.room.dao.PaintingDao
import com.example.drawit.data.local.room.mapper.toDomain
import com.example.drawit.data.local.room.mapper.toEntityWithRelations
import com.example.drawit.domain.model.Painting
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalPaintingsRepository (
    private val paintingDao: PaintingDao
) {
    fun getAllPaintings(): Flow<List<Painting>> {
        return paintingDao.getAllPaintings().map { list ->
            list.map { it.toDomain( ) }
        }
    }

    fun getPaintingById(paintingId: String): Flow<Painting?> {
        return paintingDao.getPaintingById(paintingId).map { paintingWithLayers ->
            paintingWithLayers?.toDomain()
        }
    }

    suspend fun upsertPainting(painting: Painting) {
        android.util.Log.d( "PaintingsRepository", "upsertPainting - painting<" +
                "size=${painting.size}, " +
                "#layers=${painting.layers.size}, " +
                "timeTaken=${painting.timeTaken}, " +
                "theme=${painting.theme}, " +
                "mode=${painting.mode}>" )
        val paintingWithLayers = painting.toEntityWithRelations()
        paintingDao.upsertPaintingWithLayers(paintingWithLayers)
    }

    suspend fun deletePainting(painting: Painting) {
        val paintingWithLayers = painting.toEntityWithRelations()
        paintingDao.deletePaintingWithLayers(paintingWithLayers)
    }

    suspend fun deleteAllPaintings() {
        paintingDao.deleteAllPaintings()
    }
}