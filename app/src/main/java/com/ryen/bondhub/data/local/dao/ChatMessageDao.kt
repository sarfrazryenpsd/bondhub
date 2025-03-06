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

    @Query("SELECT * FROM chat_messages WHERE connectionId = :connectionId ORDER BY timestamp ASC")
    fun getMessagesByConnectionId(connectionId: String): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET status = :status WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE connectionId = :connectionId AND senderId != :userId AND status != 'READ'")
    fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int>

    @Query("UPDATE chat_messages SET status = 'READ' WHERE connectionId = :connectionId AND senderId != :receiverId AND status != 'READ'")
    suspend fun markMessagesAsRead(connectionId: String, receiverId: String)
}