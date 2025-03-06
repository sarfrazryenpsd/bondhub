package com.ryen.bondhub.domain.model

data class ChatMessage(
    val messageId: String = "",
    val connectionId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val messageType: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageType {
    TEXT, IMAGE, LOCATION, VOICE
}

enum class MessageStatus {
    SENT, DELIVERED, READ
}