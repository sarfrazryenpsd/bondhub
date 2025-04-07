package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatMessage


sealed class ChatMessageScreenState {
    data object Initial : ChatMessageScreenState()
    data object Loading : ChatMessageScreenState()
    data class Success(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false,
        val canSendMessage: Boolean = true,
        val error: String? = null
    ) : ChatMessageScreenState()
    data class Error(val message: String) : ChatMessageScreenState()
}