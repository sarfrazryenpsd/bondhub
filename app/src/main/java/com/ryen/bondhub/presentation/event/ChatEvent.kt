package com.ryen.bondhub.presentation.event

import com.ryen.bondhub.domain.model.ChatConnection

sealed class ChatEvent {
    // Existing events...

    data object ToggleFriendsBottomSheet : ChatEvent()
    data object CloseFriendsBottomSheet : ChatEvent()
    data class StartChatWithFriend(val connection: ChatConnection) : ChatEvent()
}