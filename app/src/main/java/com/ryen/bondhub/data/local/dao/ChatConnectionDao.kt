package com.ryen.bondhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ryen.bondhub.data.local.entity.ChatConnectionEntity
import com.ryen.bondhub.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ChatConnectionEntity)

    @Query("SELECT * FROM chat_connections WHERE user1Id = :userId OR user2Id = :userId")
    fun getConnectionsForUser(userId: String): Flow<List<ChatConnectionEntity>>

    @Query("SELECT * FROM chat_connections WHERE connectionId = :connectionId")
    suspend fun getConnectionById(connectionId: String): ChatConnectionEntity?

    @Update
    suspend fun updateConnection(connection: ChatConnectionEntity)

    @Query("SELECT * FROM chat_connections WHERE (user1Id = :user1Id AND user2Id = :user2Id) OR (user1Id = :user2Id AND user2Id = :user1Id)")
    suspend fun findExistingConnection(user1Id: String, user2Id: String): ChatConnectionEntity?

    @Query("DELETE FROM chat_connections WHERE connectionId = :connectionId")
    suspend fun deleteConnection(connectionId: String)

    @Query("SELECT * FROM chat_connections WHERE status = :status AND (user1Id = :userId OR user2Id = :userId)")
    fun getConnectionsByStatus(userId: String, status: ConnectionStatus): Flow<List<ChatConnectionEntity>>
}