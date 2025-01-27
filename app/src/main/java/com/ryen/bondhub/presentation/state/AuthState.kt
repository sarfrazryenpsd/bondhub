package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.User

sealed class AuthState{
    data object Initial: AuthState()
    data object Loading: AuthState()
    data class Error(val message: String): AuthState()
    data class Authenticated(val user: User): AuthState()
}