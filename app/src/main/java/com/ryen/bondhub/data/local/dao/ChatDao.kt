package com.ryen.bondhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ryen.bondhub.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE :userId IN (participants)")
    fun getUserChats(userId: String): Flow<List<ChatEntity>>

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query("UPDATE chats SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime WHERE chatId = :chatId")
    suspend fun updateChatWithNewMessage(chatId: String, lastMessage: String, lastMessageTime: Long)

    @Query("UPDATE chats SET unreadMessageCount = 0 WHERE chatId = :chatId")
    suspend fun markChatAsRead(chatId: String)

    @Query("UPDATE chats SET unreadMessageCount = :count WHERE connectionId = :connectionId")
    suspend fun updateUnreadMessageCount(connectionId: String, count: Int)

    /*@Query("UPDATE chats SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime WHERE chatId = :chatId")
    suspend fun updateChatWithNewMessage(
        chatId: String,
        lastMessage: String,
        lastMessageTime: Long,
        *//*lastMessageType: String,
        lastMessageSenderId: String*//*
    )*/
}