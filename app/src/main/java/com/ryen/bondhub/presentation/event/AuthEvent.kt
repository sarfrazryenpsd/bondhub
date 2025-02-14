package com.ryen.bondhub.presentation.event

sealed class AuthEvent {
    data class SignUp(val email: String, val password: String, val displayName: String) : AuthEvent()
    data class SignIn(val email: String, val password: String) : AuthEvent()
}