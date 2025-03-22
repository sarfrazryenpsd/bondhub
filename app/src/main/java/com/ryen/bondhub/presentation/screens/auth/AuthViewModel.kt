package com.ryen.bondhub.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.auth.SignInUseCase
import com.ryen.bondhub.domain.useCases.auth.SignUpUseCase
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.screens.Screen
import com.ryen.bondhub.presentation.state.AuthScreenState
import com.ryen.bondhub.presentation.state.AuthUiState
import com.ryen.bondhub.util.AuthValidation
import com.ryen.bondhub.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    private val _authScreenState = MutableStateFlow<AuthScreenState>(AuthScreenState.Initial)
    val authState = _authScreenState.asStateFlow()

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState = _authUiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SignIn -> signIn(event.email, event.password)
            is AuthEvent.SignUp -> signUp(event.email, event.password, event.displayName)

        }
    }

    fun onEmailChange(email: String) {
        _authUiState.update { it.copy(email = email.trim()) }
    }

    fun onFullNameChange(fullName: String) {
        _authUiState.update { it.copy(fullName = fullName.trim()) }
    }

    fun onPasswordChange(password: String) {
        _authUiState.update { it.copy(password = password.trim()) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _authUiState.update { it.copy(confirmPassword = confirmPassword.trim()) }
    }

    fun onVisibilityChange(visibility: Boolean) {
        _authUiState.update { it.copy(passwordVisibility = visibility) }
    }

    fun onSignInStateChange(signInState: Boolean) {
        _authUiState.update { it.copy(
            signInState = signInState,
            passwordVisibility = false,
            fullName = "",
            email = "",
            password = "",
            confirmPassword = ""
        ) }
    }



    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            // First validate the input fields
            when (val validationResult = AuthValidation.validateSignInFields(email, password)) {
                is ValidationResult.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbarError(validationResult.message))
                    return@launch
                }
                is ValidationResult.Success -> {
                    // Proceed with sign in
                    _authScreenState.value = AuthScreenState.Loading
                    try {
                        signInUseCase(email, password)
                            .onSuccess { user ->
                                _authScreenState.value = AuthScreenState.Success(user, isNewUser = false)
                                _uiEvent.emit(UiEvent.ShowSnackbarSuccess("Sign in successful"))
                                _uiEvent.emit(UiEvent.Navigate(Screen.ChatScreen.route))
                            }
                            .onFailure { exception ->
                                val errorMessage = AuthValidation.handleFirebaseAuthError(exception)
                                _authScreenState.value = AuthScreenState.Error(errorMessage)
                                _uiEvent.emit(UiEvent.ShowSnackbarError(errorMessage))
                            }
                    } catch (e: Exception) {
                        val errorMessage = "Unable to connect. Please check your internet connection."
                        _authScreenState.value = AuthScreenState.Error(errorMessage)
                        _uiEvent.emit(UiEvent.ShowSnackbarError(errorMessage))
                    }
                }
            }
        }
    }

    private fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            // First validate all fields
            when (val validationResult = AuthValidation.validateSignUpFields(
                fullName = displayName,
                email = email,
                password = password,
            )) {
                is ValidationResult.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbarError(validationResult.message))
                    return@launch
                }
                is ValidationResult.Success -> {
                    _authScreenState.value = AuthScreenState.Loading
                    try {
                        signUpUseCase(email, password, displayName)
                            .onSuccess { user ->
                                _authScreenState.value = AuthScreenState.Success(user, isNewUser = true)
                                _uiEvent.emit(UiEvent.ShowSnackbarSuccess("Sign up successful"))
                                _uiEvent.emit(UiEvent.Navigate(Screen.UserProfileSetupScreen.route))
                            }
                            .onFailure { exception ->
                                val errorMessage = AuthValidation.handleFirebaseAuthError(exception)
                                _authScreenState.value = AuthScreenState.Error(errorMessage)
                                _uiEvent.emit(UiEvent.ShowSnackbarError(errorMessage))
                            }
                    } catch (e: Exception) {
                        val errorMessage = "Unable to connect. Please check your internet connection."
                        _authScreenState.value = AuthScreenState.Error(errorMessage)
                        _uiEvent.emit(UiEvent.ShowSnackbarError(errorMessage))
                    }
                }
            }
        }
    }


}

