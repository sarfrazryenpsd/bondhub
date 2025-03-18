package com.ryen.bondhub.presentation.event

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data class ShowSnackbarError(val message: String) : UiEvent()
    data class ShowSnackbarSuccess(val message: String) : UiEvent()
}