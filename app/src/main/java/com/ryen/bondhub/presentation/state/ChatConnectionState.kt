package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatConnection

sealed class ChatConnectionState {
    data object Initial : ChatConnectionState()
    data object Loading : ChatConnectionState()
    data class Success(val connections: List<ChatConnection>) : ChatConnectionState()
    data class Error(val message: String) : ChatConnectionState()
}