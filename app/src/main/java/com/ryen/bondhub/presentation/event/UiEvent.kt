package com.ryen.bondhub.presentation.event

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data object ProfileUpdateCompleted : UiEvent()
    data class ShowSnackbar(val message: String) : UiEvent()
}