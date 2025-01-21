package com.ryen.bondhub.presentation.state

sealed class SignInState{
    data object Initial: SignInState()
    data object Loading: SignInState()
    data class Error(val message: String): SignInState()
    data object Success: SignInState()
}