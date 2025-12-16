package com.example.drawit.data.remote.repository

import com.example.drawit.data.local.room.dao.PaintingDao
import com.example.drawit.data.local.room.mapper.toEntityWithRelations
import com.example.drawit.data.remote.firestore.mapper.toDomain
import com.example.drawit.data.remote.firestore.mapper.toEntity
import com.example.drawit.data.remote.firestore.mapper.toFirestoreDto
import com.example.drawit.data.remote.firestore.model.PaintingFirestoreDto
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.domain.model.Painting
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

data class User(
    val createdAt: Timestamp,
    val userId: String,
    val username: String
)

class FirestoreDrawItRepository(
    private val db: FirebaseFirestore,
    private val dao: PaintingDao
) {
    private val USERS_COLLECTION = "users"
    private val PAINTINGS_COLLECTION = "paintings"
    private val FRIEND_REQUESTS_COLLECTION = "friendrequests"
    private val FRIENDS_COLLECTION = "friends"

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

    suspend fun getUserPaintings( userId: String ): NetworkResult< List< Painting > > {
        return try {
            val snapshot = db.collection(PAINTINGS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val paintings = snapshot.documents.mapNotNull {
                it.toObject(PaintingFirestoreDto::class.java)
            }.map { it.toDomain() }

            NetworkResult.Success(paintings)
        } catch ( e: Exception ) {
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun searchForUser(username: String): NetworkResult<List<User>> {
        return try {
            val end = "$username\uf8ff" // upper bound for prefix search

            val snapshot = db.collection(USERS_COLLECTION)
                .where(Filter.greaterThanOrEqualTo("username", username))
                .where(Filter.lessThanOrEqualTo("username", end))
                .get()
                .await()

            android.util.Log.d("FirestoreDrawItRepository", "searchForUser - found ${snapshot.size()} users for query '$username'")

            val users = snapshot.documents.mapNotNull {
                val uname = it.getString("username")
                if (uname != null) {
                    User(
                        userId = it.getString("userId")!!,
                        username = uname,
                        createdAt = it.getTimestamp("createdAt")!!
                    )
                } else {
                    null
                }
            }

            NetworkResult.Success(users)
        } catch ( e: Exception ) {
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    /**
     * Validated on server side
     * @param fromUserId The ID of the user sending the friend request.
     * @param fromUsername The ID of the friend request sender.
     * @param toUserId The ID of the user receiving the friend request.
     * @param toUsername The username of the user receiving the friend request.
     */
    suspend fun sendFriendRequest(fromUserId: String, fromUsername: String, toUserId: String, toUsername: String): NetworkResult<String?> {
        return try {
            val docName = "${fromUserId}_to_${toUserId}"
            val friendRequestData = mapOf(
                "fromId" to fromUserId,
                "fromUsername" to fromUsername,
                "toId" to toUserId,
                "toUsername" to toUsername,
                "timestamp" to Timestamp.now()
            )

            db.collection(FRIEND_REQUESTS_COLLECTION)
                .document(docName)
                .set(friendRequestData)
                .await()

            NetworkResult.Success(null)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "sendFriendRequest - error sending friend request: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun declineFriendRequest(fromUserId: String, toUserId: String): NetworkResult<String?> {
        return try {
            val docName = "${fromUserId}_to_${toUserId}"

            db.collection(FRIEND_REQUESTS_COLLECTION)
                .document(docName)
                .delete()
                .await()

            NetworkResult.Success(null)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "declineFriendRequest - error declining friend request: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun acceptFriendRequest(requestId: String): NetworkResult<String?> {
        return try {
            val functions = FirebaseFunctions.getInstance()

            val data = hashMapOf(
                "requestId" to requestId
            )

            functions
                .getHttpsCallable("acceptFriendRequest")
                .call(data)
                .await()

            NetworkResult.Success(null)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "acceptFriendRequest - error accepting friend request: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    /**
     * Validated on server side.
     * @return List of friend requests
     */
    suspend fun getFriendRequestsForUser(userId: String): NetworkResult<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toId", userId)
                .get()
                .await()

            val requests = snapshot.documents.mapNotNull {
                it.data
            }

            NetworkResult.Success(requests)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "getFriendRequestsForUser - error fetching friend requests: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun getSentFriendRequestsFromUser(userId: String): NetworkResult<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("fromId", userId)
                .get()
                .await()

            val requests = snapshot.documents.mapNotNull {
                it.data
            }

            NetworkResult.Success(requests)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "getSentFriendRequestsFromUser - error fetching sent friend requests: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun getFriendsForUser(userId: String): NetworkResult<List<Map<String, Any>>> {
        return try {
            val snapshot = db.collection(FRIENDS_COLLECTION)
                .whereArrayContains("ids", userId)
                .get()
                .await()

            val friends = snapshot.documents.mapNotNull {
                it.data
            }

            NetworkResult.Success(friends)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "getFriendsForUser - error fetching friends: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun removeFriend(userId: String, friendId: String): NetworkResult<String?> {
        return try {
            db.collection(FRIENDS_COLLECTION)
                .whereArrayContains("ids", userId)
                .get()
                .await()
                .documents
                .forEach { document ->
                    val ids = document.get("ids") as? List<*>
                    if (ids != null && ids.contains(friendId)) {
                        document.reference.delete().await()
                    }
                }

            NetworkResult.Success(null)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "removeFriend - error removing friend: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }

    suspend fun getUsernameById(userId: String): NetworkResult<String?> {
        return try {
            val doc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val username = doc.getString("username")
            NetworkResult.Success(username)
        } catch ( e: Exception ) {
            android.util.Log.e("FirestoreDrawItRepository", "getUsernameById - error fetching username: ${e.message}")
            NetworkResult.Error( e.message ?: "Unknown error" )
        }
    }
}