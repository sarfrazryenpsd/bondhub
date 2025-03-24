package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface ChatConnectionRepository {

    suspend fun sendConnectionRequest(currentUserId: String, targetUserId: String): Result<ChatConnection>
    suspend fun acceptConnectionRequest(connectionId: String): Result<Unit>
    suspend fun rejectConnectionRequest(connectionId: String): Result<Unit>
    suspend fun getConnectionsForUser(userId: String): Flow<List<ChatConnection>>
    suspend fun findExistingConnection(user1Id: String, user2Id: String): ChatConnection?
    suspend fun getConnectionBetweenUsers(user1Id: String, user2Id: String): Flow<ChatConnection?>
    suspend fun createConnection(connection: ChatConnection): Result<ChatConnection>
    suspend fun getConnectionStatus(userId1: String, userId2: String): Result<ConnectionStatus?>
    suspend fun getPendingConnectionRequestsForUser(userId: String, asRecipient: Boolean): Flow<List<ChatConnection>>
    suspend fun getAcceptedConnectionsFlow(userId: String): Flow<List<ChatConnection>>
}