package com.example.drawit.ui.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawit.NavCoordinator
import com.example.drawit.data.remote.model.NetworkResult
import com.example.drawit.domain.model.authentication.AuthenticationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class Validators {
    companion object {
        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidPassword(password: String): Boolean {
            return password.length >= 6
        }

        fun validateInput(email: String, password: String): String? {
            if (!isValidEmail(email)) {
                return "Invalid email format."
            }
            if (!isValidPassword(password)) {
                return "Password must be at least 6 characters long."
            }

            return null
        }
    }
}

class AuthenticationViewmodel(
    private val navCoordinator: NavCoordinator,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data object Loading : UiEvent()
        data object Success : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0)
    open val events = _events.asSharedFlow()

    fun onLoginClicked(email: String, password: String) {
        val error = Validators.validateInput(email, password)

        if (error != null) {
            showError(error)
            return
        }

        startLoading()

        viewModelScope.launch {
            val res = authenticationRepository.login(email, password)
            when (res) {
                is NetworkResult.Success -> {
                    _events.emit(UiEvent.Success)
                    navCoordinator.toMainScreen()
                }

                is NetworkResult.Error -> {
                    showError(res.message ?: "An unknown error occurred")
                }

                else -> {
                    showError("An unknown error occurred")
                }
            }
        }

        // Proceed with login using authenticationRepository
    }

    fun onRegisterClicked(email: String, password: String) {
        val error = Validators.validateInput(email, password)

        if (error != null) {
            showError(error)
            return
        }

        startLoading()

        viewModelScope.launch {
            val res = authenticationRepository.register(email, password)
            when (res) {
                is NetworkResult.Success -> {
                    // login aswell
                    loginAfterRegister(email, password)
                }

                is NetworkResult.Error -> {
                    showError(res.message)
                }

                else -> {
                    showError("An unknown error occurred")
                }
            }
        }
    }

    private fun loginAfterRegister(email: String, password: String) {
        viewModelScope.launch {
            val res = authenticationRepository.login(email, password)
            when (res) {
                is NetworkResult.Success -> {
                    _events.emit(UiEvent.Success)
                    navCoordinator.toMainScreen()
                }

                is NetworkResult.Error -> {
                    showError(res.message)
                }

                else -> {
                    showError("An unknown error occurred")
                }
            }
        }
    }

    private fun startLoading() {
        viewModelScope.launch {
            _events.emit(UiEvent.Loading)
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowError(message))
        }
    }
}