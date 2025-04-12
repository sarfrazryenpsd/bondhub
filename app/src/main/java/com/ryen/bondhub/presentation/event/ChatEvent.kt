package com.ryen.bondhub.presentation.event

import com.ryen.bondhub.domain.model.ChatConnection

sealed class ChatEvent {

    data class NavigateToUserProfile(val route: String) : ChatEvent()
    data object ToggleFriendsBottomSheet : ChatEvent()
    data object CloseFriendsBottomSheet : ChatEvent()
    data class StartChatWithFriend(val connection: ChatConnection) : ChatEvent()
    data class NavigateToChat(val chatId: String, val friendConnectionId: String, val friendUserId: String) : ChatEvent()
    data class DeleteChat(val chatId: String) : ChatEvent()

}