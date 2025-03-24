package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.Chat

sealed class ChatScreenState {
    data object Initial : ChatScreenState()
    data object Loading : ChatScreenState()
    data class Success(
        val chats: List<Chat> = emptyList(),
        val showFriendsBottomSheet: Boolean = false
    ) : ChatScreenState()
    data class Error(val message: String) : ChatScreenState()
}