package com.ryen.bondhub.presentation.state

import com.google.firebase.auth.FirebaseUser
import com.ryen.bondhub.domain.model.UserAuth

sealed class AuthState{
    data object Initial: AuthState()
    data object Loading: AuthState()
    data class Error(val message: String): AuthState()
    data class SignInSuccess(val user: UserAuth) : AuthState()
    data class SignUpSuccess(val user: UserAuth) : AuthState()
}