package com.ryen.bondhub.presentation.state

data class AuthUiState(
    val email: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisibility: Boolean = false,
    val signInState: Boolean = true
)