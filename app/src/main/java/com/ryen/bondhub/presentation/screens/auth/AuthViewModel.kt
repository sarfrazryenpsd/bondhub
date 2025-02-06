package com.ryen.bondhub.presentation.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.model.UserStatus
import com.ryen.bondhub.domain.useCases.auth.SignInUseCase
import com.ryen.bondhub.domain.useCases.auth.SignUpUseCase
import com.ryen.bondhub.domain.useCases.userProfile.CreateUserProfileUseCase
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
    private val signUpUseCase: SignUpUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase
) : ViewModel() {

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
                    _uiEvent.emit(UiEvent.Navigate(Screen.ChatScreen.route))
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed")
                    _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Authentication failed"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Unknown error"))
            }
        }
    }

    private fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            try {
                val signUpResult = signUpUseCase(email, password, displayName)
                signUpResult.onSuccess { user ->
                    Log.d("Auth", "User created: ${user.uid}, Email: ${user.email}")
                    _authState.value = AuthState.Authenticated(user)

                    // Create default user profile
                    val defaultProfile = UserProfile(
                        uid = user.uid,
                        displayName = user.displayName ?: email.substringBefore('@'),
                        email = user.email,
                        bio = null,
                        status = UserStatus.ONLINE,
                        createdAt = System.currentTimeMillis(),
                        lastSeen = System.currentTimeMillis()
                    )

                    createUserProfileUseCase(defaultProfile).onSuccess {
                        Log.d("Firestore", "User profile successfully created in Firestore!")
                        // Navigate to profile setup screen after creating default profile
                        _uiEvent.emit(UiEvent.Navigate(Screen.UserProfileSetupScreen.route))
                    }.onFailure { exception ->
                        Log.e("Firestore", "Error writing profile: ${exception.message}", exception)
                        _uiEvent.emit(UiEvent.ShowSnackbar("Profile creation failed: ${exception.message}"))
                    }
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                    _uiEvent.emit(UiEvent.ShowSnackbar(exception.message ?: "Registration failed"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(e.message ?: "Unknown error"))
            }
        }
    }

    private fun signOut(){

    }
}