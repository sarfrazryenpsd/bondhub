package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.UserProfile


sealed class ChatMessageScreenState {
    data object Initial : ChatMessageScreenState()
    data object Loading : ChatMessageScreenState()
    data class Success(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false,
        val canSendMessage: Boolean = true,
        val error: String? = null,
        val userProfile: UserProfile? = null
    ) : ChatMessageScreenState()
    data class Error(val message: String) : ChatMessageScreenState()
}