package com.ryen.bondhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val baseChatId: String,
    val connectionId: String,
    @TypeConverters(StringListConverter::class)
    val participants: List<String>,
    val profilePictureUrlThumbnail: String,
    val displayName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadMessageCount: Int
)

/**
 * Type converter for storing List<String> in Room database
 */
class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",")
        }
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }
}