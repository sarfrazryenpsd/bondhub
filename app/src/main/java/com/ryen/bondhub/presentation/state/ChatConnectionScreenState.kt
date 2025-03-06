package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatConnection

sealed class ChatConnectionScreenState {
    data object Initial : ChatConnectionScreenState()
    data object Loading : ChatConnectionScreenState()
    data class Success(val connections: List<ChatConnection>) : ChatConnectionScreenState()
    data class Error(val message: String) : ChatConnectionScreenState()
}