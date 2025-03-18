package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.ChatConnection
import kotlinx.coroutines.flow.Flow

interface ChatConnectionRepository {
    suspend fun sendConnectionRequest(currentUserId: String, targetUserId: String): Result<ChatConnection>
    suspend fun acceptConnectionRequest(connectionId: String): Result<Unit>
    suspend fun getConnectionsForUser(userId: String): Flow<List<ChatConnection>>
    suspend fun findExistingConnection(user1Id: String, user2Id: String): ChatConnection?
    suspend fun getConnectionBetweenUsers(user1Id: String, user2Id: String): Flow<ChatConnection?>
    suspend fun createConnection(connection: ChatConnection): Result<ChatConnection>
}