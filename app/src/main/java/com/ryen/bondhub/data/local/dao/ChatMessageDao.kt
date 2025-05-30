package com.ryen.bondhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ryen.bondhub.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getChatMessages(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE baseChatId = :baseChatId ORDER BY timestamp ASC")
    fun getMessagesByBaseChatId(baseChatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId")
    fun getChatMessagesByMessageId(messageId: String): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET status = :status WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("UPDATE chat_messages SET status = :status WHERE chatId = :chatId AND receiverId = :receiverId AND status != :status")
    suspend fun updateAllMessageStatus(chatId: String, receiverId: String, status: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE chatId = :connectionId AND receiverId = :userId AND status != 'READ'")
    fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int>
}