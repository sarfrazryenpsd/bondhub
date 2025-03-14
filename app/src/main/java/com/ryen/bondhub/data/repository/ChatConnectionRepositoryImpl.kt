package com.ryen.bondhub.data.repository

import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.local.entity.ChatConnectionEntity
import com.ryen.bondhub.data.remote.dataSource.ChatConnectionRemoteDataSource
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import com.ryen.bondhub.util.networkBoundResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatConnectionRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatConnectionRemoteDataSource,
    private val localDao: ChatConnectionDao,
    private val dispatcher: CoroutineDispatcher
) : ChatConnectionRepository {

    override suspend fun sendConnectionRequest(
        currentUserId: String,
        targetUserId: String
    ): Result<ChatConnection> = runCatching {
        // Check if connection already exists
        val existingConnection = findExistingConnection(currentUserId, targetUserId)

        existingConnection?.let {
            throw IllegalStateException("Connection already exists")
        }

        val newConnection = ChatConnection(
            user1Id = currentUserId,
            user2Id = targetUserId,
            status = ConnectionStatus.PENDING,
            initiatorId = currentUserId
        )

        // Create in remote and get ID
        val createdConnection = remoteDataSource.createConnection(newConnection)

        // Cache locally
        localDao.insertConnection(createdConnection.toEntity())

        createdConnection
    }

    override suspend fun acceptConnectionRequest(connectionId: String): Result<Unit> = runCatching {
        // Update remote status
        remoteDataSource.updateConnectionStatus(
            connectionId,
            ConnectionStatus.ACCEPTED
        ).getOrThrow()

        // Update local cache
        val connection = localDao.getConnectionById(connectionId)
        connection?.let {
            localDao.updateConnection(
                it.copy(status = ConnectionStatus.ACCEPTED)
            )
        }
    }

    override suspend fun getConnectionsForUser(userId: String): Flow<List<ChatConnection>> {
        return localDao.getConnectionsForUser(userId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun findExistingConnection(
        user1Id: String,
        user2Id: String
    ): ChatConnection? {
        // First check local cache
        val localConnection = localDao.findExistingConnection(user1Id, user2Id)

        return localConnection?.toDomain()
            ?: remoteDataSource.findExistingConnection(user1Id, user2Id)
    }

    override suspend fun getConnectionBetweenUsers(user1Id: String, user2Id: String): Flow<ChatConnection?> {
        return networkBoundResource(
            query = {
                localDao.getConnectionBetweenUsers(user1Id, user2Id)
                    .map { entity -> entity?.toDomain() }
            },
            fetch = {
                remoteDataSource.getConnectionBetweenUsers(user1Id, user2Id).first()
            },
            saveFetchResult = { connection ->
                if (connection != null) {
                    localDao.insertConnection(connection.toEntity())
                }
            },
            shouldFetch = { cachedConnection ->
                // Decide based on your app's cache policy
                cachedConnection == null || shouldRefreshCache(cachedConnection.lastInteractedAt)
            },
            dispatcher = dispatcher
        )
    }

    private fun shouldRefreshCache(lastUpdatedTimestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheLifetime = TimeUnit.MINUTES.toMillis(15) // Cache for 15 minutes
        return currentTime - lastUpdatedTimestamp > cacheLifetime
    }

    // Extension functions for mapping
    private fun ChatConnection.toEntity(): ChatConnectionEntity {
        return ChatConnectionEntity(
            connectionId = connectionId,
            user1Id = user1Id,
            user2Id = user2Id,
            status = status,
            initiatedAt = initiatedAt,
            lastInteractedAt = lastInteractedAt,
            initiatorId = initiatorId
        )
    }

    private fun ChatConnectionEntity.toDomain(): ChatConnection {
        return ChatConnection(
            connectionId = connectionId,
            user1Id = user1Id,
            user2Id = user2Id,
            status = status,
            initiatedAt = initiatedAt,
            lastInteractedAt = lastInteractedAt,
            initiatorId = initiatorId
        )
    }
}