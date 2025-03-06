package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatMessage

sealed class ChatMessageState {
    data object Initial : ChatMessageState()
    data object Loading : ChatMessageState()
    data class Success(val messages: List<ChatMessage>) : ChatMessageState()
    data class Error(val message: String) : ChatMessageState()
}