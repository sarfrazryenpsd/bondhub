package com.ryen.bondhub.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.SignInUseCase
import com.ryen.bondhub.domain.useCases.SignUpUseCase
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
): ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignIn -> signIn(event.email, event.password)
            is AuthEvent.SignUp -> signUp(event.email, event.password, event.displayName)
            is AuthEvent.SignOut -> signOut()
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = signInUseCase(email, password)
                result.onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                val result = signUpUseCase(email, password, displayName)
                result.onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun signOut(){

    }
}