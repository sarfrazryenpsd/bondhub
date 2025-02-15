package com.ryen.bondhub.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.auth.SignInUseCase
import com.ryen.bondhub.domain.useCases.auth.SignUpUseCase
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.screens.Screen
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
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignIn -> signIn(event.email, event.password)
            is AuthEvent.SignUp -> signUp(event.email, event.password, event.displayName)
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                signInUseCase(email, password).onSuccess { user ->
                    _authState.value = AuthState.Success(user, isNewUser = false)
                    _uiEvent.emit(UiEvent.Navigate(Screen.ChatScreen.route))
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                    _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Sign in failed"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Unknown error"))
            }
        }
    }

    private fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                signUpUseCase(email, password, displayName).onSuccess { user ->
                    _authState.value = AuthState.Success(user, isNewUser = true)
                    _uiEvent.emit(UiEvent.Navigate(Screen.UserProfileSetupScreen.route))
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                    _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Sign up failed"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Unknown error"))
            }
        }
    }
}
