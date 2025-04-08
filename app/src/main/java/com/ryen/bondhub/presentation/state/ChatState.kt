package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.Chat

sealed class ChatsState {
    data object Loading : ChatsState()
    data object Empty : ChatsState()
    data class Success(val chats: List<Chat>) : ChatsState()
    data class Error(val message: String) : ChatsState()
}