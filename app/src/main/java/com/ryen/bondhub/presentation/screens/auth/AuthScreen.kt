package com.ryen.bondhub.presentation.screens.auth

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.AuthScreenContent
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.AuthState


@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()

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

    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var signInState by remember { mutableStateOf(true) }

    AuthScreenContent(
        email = email,
        fullName = fullName,
        password = password,
        confirmPassword = confirmPassword,
        visibility = visibility,
        signInState = signInState,
        onEmailChange = { email = it },
        onFullNameChange = { fullName = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onVisibilityChange = { visibility = it },
        onSignInStateChange = { signInState = it },
        onSignInClick = { viewModel.onEvent(AuthEvent.SignIn(email, password)) },
        onSignUpClick = { viewModel.onEvent(AuthEvent.SignUp(email, password, fullName)) },
    )
    when (authState) {
        is AuthState.Loading -> CircularProgressIndicator()
        is AuthState.Error -> Text(text = (authState as AuthState.Error).message)
        is AuthState.Success -> {
            // Navigation is handled by UiEvents
        }
        AuthState.Initial -> Unit
    }

}

