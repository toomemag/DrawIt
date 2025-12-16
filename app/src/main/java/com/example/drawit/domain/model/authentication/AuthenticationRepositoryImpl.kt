package com.example.drawit.domain.model.authentication

import android.content.Context
import com.example.drawit.data.remote.model.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthenticationRepositoryImpl(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : AuthenticationRepository {
    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun login(email: String, password: String): NetworkResult<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override suspend fun logout(): NetworkResult<Unit> {
        return try {
            firebaseAuth.signOut()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override suspend fun register(email: String, password: String): NetworkResult<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override suspend fun deleteAccount(): NetworkResult<Unit> {
        if ( !isLoggedIn() ) {
            return NetworkResult.Error("No user is currently logged in.")
        }

        return try {
            firebaseAuth.currentUser?.delete()?.await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }

    override fun getCurrentUser( ) = firebaseAuth.currentUser
}