package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.UserAuth

sealed class AuthScreenState{
    data object Initial : AuthScreenState()
    data object Loading : AuthScreenState()
    data class Error(val message: String) : AuthScreenState()
    data class Success(
        val user: UserAuth,
        val isNewUser: Boolean
    ) : AuthScreenState()
}