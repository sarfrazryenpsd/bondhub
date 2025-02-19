package com.ryen.bondhub.presentation.screens.auth

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.AuthScreenContent
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.AuthScreenState


@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event.route)
                is UiEvent.ShowSnackbar -> {
                    // Handle snackbar
                }
            }
        }
    }


    AuthScreenContent(
        email = authUiState.email,
        fullName = authUiState.fullName,
        password = authUiState.password,
        confirmPassword = authUiState.confirmPassword,
        passwordVisibility = authUiState.passwordVisibility,
        signInState = authUiState.signInState,
        onEmailChange = { viewModel.onEmailChange(it) },
        onFullNameChange = { viewModel.onFullNameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onConfirmPasswordChange = { viewModel.onConfirmPasswordChange(it) },
        onVisibilityChange = { viewModel.onVisibilityChange(it) },
        onSignInStateChange = { viewModel.onSignInStateChange(it) },
        onSignInClick = { viewModel.onEvent(AuthEvent.SignIn(authUiState.email, authUiState.password)) },
        onSignUpClick = { viewModel.onEvent(AuthEvent.SignUp(authUiState.email, authUiState.password, authUiState.fullName)) },
    )
    when (authState) {
        is AuthScreenState.Loading -> CircularProgressIndicator()
        is AuthScreenState.Error -> Text(text = (authState as AuthScreenState.Error).message)
        is AuthScreenState.Success -> {
            // Navigation is handled by UiEvents
        }
        AuthScreenState.Initial -> Unit
    }

}

