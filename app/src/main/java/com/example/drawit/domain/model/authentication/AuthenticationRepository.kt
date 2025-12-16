package com.example.drawit.domain.model.authentication

import com.example.drawit.data.remote.model.NetworkResult
import com.google.firebase.auth.FirebaseUser

interface AuthenticationRepository {
    fun isLoggedIn(): Boolean

    suspend fun login(email: String, password: String): NetworkResult<Unit>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun register(email: String, password: String): NetworkResult<Unit>

    fun getCurrentUser(): FirebaseUser?
}