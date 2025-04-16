package com.ryen.bondhub.data.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.ryen.bondhub.data.local.entity.ChatEntity
import com.ryen.bondhub.domain.model.Chat

@Suppress("UNCHECKED_CAST")
class ChatMapper {
    fun mapEntityToDomain(entity: ChatEntity): Chat {
        return Chat(
            chatId = entity.chatId,
            baseChatId = entity.baseChatId,
            connectionId = entity.connectionId,
            participants = entity.participants,
            profilePictureUrlThumbnail = entity.profilePictureUrlThumbnail,
            displayName = entity.displayName,
            lastMessage = entity.lastMessage,
            lastMessageTime = entity.lastMessageTime,
            unreadMessageCount = entity.unreadMessageCount
        )
    }

    fun mapDomainToEntity(domain: Chat): ChatEntity {
        return ChatEntity(
            chatId = domain.chatId,
            baseChatId = domain.baseChatId,
            connectionId = domain.connectionId,
            participants = domain.participants,
            profilePictureUrlThumbnail = domain.profilePictureUrlThumbnail,
            displayName = domain.displayName,
            lastMessage = domain.lastMessage?: "",
            lastMessageTime = domain.lastMessageTime,
            unreadMessageCount = domain.unreadMessageCount
        )
    }

    fun mapEntityToRemoteMap(entity: ChatEntity): Map<String, Any?> {
        return mapOf(
            "chatId" to entity.chatId,
            "baseChatId" to entity.baseChatId,
            "connectionId" to entity.connectionId,
            "participants" to entity.participants,
            "profilePictureUrlThumbnail" to entity.profilePictureUrlThumbnail,
            "displayName" to entity.displayName,
            "lastMessage" to entity.lastMessage,
            "lastMessageTime" to entity.lastMessageTime,
            "unreadMessageCount" to entity.unreadMessageCount,
        )
    }

    fun mapDocumentToDomain(document: DocumentSnapshot): Chat {
        return Chat(
            chatId = document.id,
            baseChatId = document.getString("baseChatId") ?: "",
            connectionId = document.getString("connectionId") ?: "",
            participants = document.get("participants") as? List<String> ?: emptyList(),
            profilePictureUrlThumbnail = document.getString("profilePictureUrlThumbnail") ?: "",
            displayName = document.getString("displayName") ?: "",
            lastMessage = document.getString("lastMessage") ?: "",
            lastMessageTime = document.getLong("lastMessageTime") ?: 0L,
            unreadMessageCount = document.getLong("unreadMessageCount")?.toInt() ?: 0
        )
    }

    fun mapDomainToMap(domain: Chat): Map<String, Any?> {
        return mapOf(
            "chatId" to domain.chatId,
            "baseChatId" to domain.baseChatId,
            "connectionId" to domain.connectionId,
            "participants" to domain.participants,
            "profilePictureUrlThumbnail" to domain.profilePictureUrlThumbnail,
            "displayName" to domain.displayName,
            "lastMessage" to domain.lastMessage,
            "lastMessageTime" to domain.lastMessageTime,
            "unreadMessageCount" to domain.unreadMessageCount
        )
    }

    fun mapRemoteToDomain(id: String, chatData: Map<String, Any>): Chat {
        return Chat(
            chatId = id,
            baseChatId = chatData["baseChatId"] as? String ?: "",
            connectionId = chatData["connectionId"] as? String ?: "",
            participants = chatData["participants"] as? List<String> ?: emptyList(),
            profilePictureUrlThumbnail = chatData["profilePictureUrlThumbnail"] as? String ?: "",
            displayName = chatData["displayName"] as? String ?: "",
            lastMessage = chatData["lastMessage"] as? String ?: "",
            lastMessageTime = chatData["lastMessageTime"] as? Long ?: 0L,
            unreadMessageCount = (chatData["unreadMessageCount"] as? Number)?.toInt() ?: 0
        )
    }
}