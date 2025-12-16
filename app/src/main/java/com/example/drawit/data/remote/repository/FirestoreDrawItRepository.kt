package com.example.drawit.data.remote.repository

import com.example.drawit.data.local.room.dao.PaintingDao
import com.example.drawit.data.local.room.mapper.toEntityWithRelations
import com.example.drawit.data.remote.firestore.mapper.toEntity
import com.example.drawit.data.remote.firestore.mapper.toFirestoreDto
import com.example.drawit.data.remote.firestore.model.PaintingFirestoreDto
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.domain.model.Painting
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreDrawItRepository(
    private val db: FirebaseFirestore,
    private val dao: PaintingDao
) {
    private val PAINTINGS_COLLECTION = "paintings"

    suspend fun upsert( item: Painting ): NetworkResult< Unit > {
        return try {
            val doc = db.collection(PAINTINGS_COLLECTION).document(item.id)

            // creates painting + layers objects
            val localEntity = item.toEntityWithRelations()
            // layer, bindings object
            val layers = localEntity.layers.map { it.layer }
            // toFirestoreDto expects map<layerid, bindingent>
            val bindings = localEntity.layers.associate { layerWithBindings ->
                layerWithBindings.layer.id to layerWithBindings.bindings
            }

            doc.set(localEntity.painting.toFirestoreDto(layers, bindings)).await()
            refreshFromRemote()
        } catch ( e: Exception ) {
            return NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun delete( id: String ): NetworkResult< Unit > {
        return try {
            val doc = db.collection(PAINTINGS_COLLECTION).document(id)
            doc.delete().await()
            refreshFromRemote()
        } catch ( e: Exception ) {
            return NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun refreshFromRemote(): NetworkResult< Unit > {
        return try {
            val snapshot = db.collection(PAINTINGS_COLLECTION).get().await()

            val paintings = snapshot.documents.mapNotNull {
                it.toObject(PaintingFirestoreDto::class.java)
            }.map { it.toEntity() }

            // update/push local db
            paintings.forEach { p ->
                dao.upsertPaintingEntity( p )
            }

            return NetworkResult.Success(Unit)
        } catch ( e: Exception ) {
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }
}