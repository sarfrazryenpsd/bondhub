package com.ryen.bondhub.presentation.event

import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType

sealed class ChatMessageEvent {
    data class SendMessage(
        val connectionId: String,
        val senderId: String,
        val content: String,
        val messageType: MessageType = MessageType.TEXT
    ) : ChatMessageEvent()

    data class LoadMessages(val connectionId: String) : ChatMessageEvent()
    data class UpdateMessageStatus(val messageId: String, val status: MessageStatus) : ChatMessageEvent()
    data class DeleteMessage(val messageId: String) : ChatMessageEvent()
    data class MarkAsRead(val connectionId: String, val receiverId: String) : ChatMessageEvent()
}