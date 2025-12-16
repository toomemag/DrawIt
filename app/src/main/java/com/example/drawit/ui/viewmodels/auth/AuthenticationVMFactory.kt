package com.example.drawit.ui.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drawit.NavCoordinator
import com.example.drawit.domain.model.authentication.AuthenticationRepository

class AuthenticationVMFactory (
    private val navCoordinator: NavCoordinator,
    private val authenticationRepository: AuthenticationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthenticationViewmodel(
            navCoordinator = navCoordinator,
            authenticationRepository = authenticationRepository
        ) as T
    }
}