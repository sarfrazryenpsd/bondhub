package com.ryen.bondhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val connectionId: String,
    val participants: String, // Stored as JSON array
    val profilePictureUrlThumbnail: String,
    val displayName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadMessageCount: Int,
)