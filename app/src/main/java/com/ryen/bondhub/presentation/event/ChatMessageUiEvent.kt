package com.ryen.bondhub.presentation.event

sealed class ChatMessageUiEvent {
    data class ShowSnackbar(val message: String) : ChatMessageUiEvent()
    data object NavigateBack : ChatMessageUiEvent()
}