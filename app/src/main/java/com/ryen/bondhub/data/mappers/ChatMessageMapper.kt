package com.ryen.bondhub.data.mappers

import com.ryen.bondhub.data.local.entity.ChatMessageEntity
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType

class ChatMessageMapper {
    fun mapEntityToDomain(entity: ChatMessageEntity): ChatMessage {
        return ChatMessage(
            messageId = entity.messageId,
            chatId = entity.chatId,
            senderId = entity.senderId,
            receiverId = entity.receiverId,
            content = entity.content,
            timestamp = entity.timestamp,
            messageType = MessageType.valueOf(entity.messageType),
            status = MessageStatus.valueOf(entity.status),
            attachmentUrl = entity.attachmentUrl
        )
    }

    fun mapDomainToEntity(domain: ChatMessage): ChatMessageEntity {
        return ChatMessageEntity(
            messageId = domain.messageId,
            chatId = domain.chatId,
            senderId = domain.senderId,
            receiverId = domain.receiverId,
            content = domain.content,
            timestamp = domain.timestamp,
            messageType = domain.messageType.name,
            status = domain.status.name,
            attachmentUrl = domain.attachmentUrl
        )
    }

    fun mapRemoteToDomain(remote: Map<String, Any>): ChatMessage {
        return ChatMessage(
            messageId = remote["messageId"] as String,
            chatId = remote["chatId"] as String,
            senderId = remote["senderId"] as String,
            receiverId = remote["receiverId"] as String,
            content = remote["content"] as String,
            timestamp = remote["timestamp"] as Long,
            messageType = MessageType.valueOf(remote["messageType"] as String),
            status = MessageStatus.valueOf(remote["status"] as String),
            attachmentUrl = remote["attachmentUrl"] as? String
        )
    }

    fun mapDomainToRemote(domain: ChatMessage): Map<String, Any?> {
        return mapOf(
            "messageId" to domain.messageId,
            "chatId" to domain.chatId,
            "senderId" to domain.senderId,
            "receiverId" to domain.receiverId,
            "content" to domain.content,
            "timestamp" to domain.timestamp,
            "messageType" to domain.messageType.name,
            "status" to domain.status.name,
            "attachmentUrl" to domain.attachmentUrl
        )
    }
}
