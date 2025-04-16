package com.ryen.bondhub.domain.model

import java.util.UUID

data class ChatMessage(
    val messageId: String = UUID.randomUUID().toString(),
    val chatId: String = "",  // Individual user's chatId
    val baseChatId: String = "",  // Shared identifier for both chat documents
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENDING,
    val attachmentUrl: String? = null
)

enum class MessageType {
    TEXT, IMAGE, LOCATION, VOICE
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}