package com.ryen.bondhub.presentation.screens.auth

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.contents.AuthScreenContent
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.event.AuthEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.util.AuthValidation


@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
) {
    val authState by viewModel.authState.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.INITIAL) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> onNavigate(event.route)
                is UiEvent.ShowSnackbarSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                    snackbarState.value = SnackBarState.SUCCESS
                }
                is UiEvent.ShowSnackbarError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                    snackbarState.value = SnackBarState.ERROR
                }
            }
        }
    }


    Scaffold (
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        content = { paddingValue ->
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
                onSignInClick = {
                    viewModel.onEvent(
                        AuthEvent.SignIn(
                            authUiState.email,
                            authUiState.password
                        )
                    )
                },
                onSignUpClick = {
                    viewModel.onEvent(
                        AuthEvent.SignUp(
                            authUiState.email,
                            authUiState.password,
                            authUiState.fullName
                        )
                    )
                },
                onEmailCheck = { AuthValidation.validateEmail(it) },
                onFullNameCheck = { AuthValidation.validateFullName(it) },
                onPasswordCheck = { AuthValidation.validatePassword(it) },
                onConfirmPasswordCheck = { password, confirmPassword ->
                    AuthValidation.validatePasswordMatch(password, confirmPassword)
                },
                authState = authState,
                paddingValues = paddingValue
            )
        }
    )


}

