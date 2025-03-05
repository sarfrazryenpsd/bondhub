package com.ryen.bondhub.presentation.event

sealed class ChatEvent {
    data class SendConnectionRequest(
        val currentUserId: String,
        val targetUserId: String
    ) : ChatEvent()

    data class AcceptConnectionRequest(val connectionId: String) : ChatEvent()

    data class FindExistingConnection(
        val user1Id: String,
        val user2Id: String
    ) : ChatEvent()
}