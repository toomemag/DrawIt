package com.example.drawit

import android.app.Application
import com.example.drawit.data.local.AppDatabaseProvider
import com.example.drawit.data.local.room.repository.PaintingsRepository
import com.example.drawit.domain.model.authentication.AuthenticationRepositoryImpl
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class DrawItApplication : Application() {
    val navCoordinator by lazy { NavCoordinator() }
    private val database by lazy { AppDatabaseProvider.getDatabase(this) }

    val authenticationRepository by lazy {
        AuthenticationRepositoryImpl(applicationContext, firebaseAuth)
    }


    val paintingsRepository by lazy {
        PaintingsRepository(
            paintingDao = database.paintingDao(),
            firestoreDatabase = firestoreDatabase
        )
    }


    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestoreDatabase by lazy {
        FirebaseApp.initializeApp(this)
        Firebase.firestore
    }
}
