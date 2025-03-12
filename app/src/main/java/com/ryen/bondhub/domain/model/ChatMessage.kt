package com.ryen.bondhub.domain.model

data class ChatMessage(
    val messageId: String = "",
    val chatId: String = "",
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