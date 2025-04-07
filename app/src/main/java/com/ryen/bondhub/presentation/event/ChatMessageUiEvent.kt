package com.ryen.bondhub.presentation.event

sealed class ChatMessageUiEvent {
    data class ShowSnackbarError(val message: String) : ChatMessageUiEvent()
    data class ShowSnackbarSuccess(val message: String) : ChatMessageUiEvent()
    data class ShowSnackbarInfo(val message: String) : ChatMessageUiEvent()
    data class Navigate(val route: String) : ChatMessageUiEvent()
    data object NavigateBack : ChatMessageUiEvent()
    data object ClearInput : ChatMessageUiEvent()
}