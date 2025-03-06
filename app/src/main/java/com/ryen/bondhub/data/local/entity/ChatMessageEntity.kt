package com.ryen.bondhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val messageId: String,
    val connectionId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageType: String,
    val status: String
)

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        messageId = messageId,
        connectionId = connectionId,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        messageType = MessageType.valueOf(messageType),
        status = MessageStatus.valueOf(status)
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = messageId,
        connectionId = connectionId,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        messageType = messageType.name,
        status = status.name
    )
}