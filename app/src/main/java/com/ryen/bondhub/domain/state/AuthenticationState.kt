package com.ryen.bondhub.domain.state

import com.ryen.bondhub.domain.model.UserAuth

sealed class AuthenticationState {
    data object Unauthenticated : AuthenticationState()
    data class NewUser(val user: UserAuth) : AuthenticationState()
    data class ExistingUser(val user: UserAuth) : AuthenticationState()
}