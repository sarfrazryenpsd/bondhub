package com.ryen.bondhub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.local.entity.ChatConnectionEntity
import com.ryen.bondhub.data.remote.dataSource.ChatConnectionRemoteDataSource
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import com.ryen.bondhub.util.networkBoundResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatConnectionRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatConnectionRemoteDataSource,
    private val firestore: FirebaseFirestore,
    private val localDao: ChatConnectionDao,
    private val dispatcher: CoroutineDispatcher
) : ChatConnectionRepository {

    private val connectionsCollection = firestore.collection("chat_connections")

    // Existing methods...

    override suspend fun createConnection(connection: ChatConnection): Result<ChatConnection> = withContext(
        Dispatchers.IO) {
        try {
            // Create a new document with auto-generated ID
            val docRef = connectionsCollection.document()

            // Add the connectionId to the connection object
            val newConnection = connection.copy(connectionId = docRef.id)

            // Set the data
            docRef.set(newConnection).await()

            // Cache the connection
            localDao.insertConnection(newConnection.toEntity())

            Result.success(newConnection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendConnectionRequest(
        currentUserId: String,
        targetUserId: String
    ): Result<ChatConnection> = runCatching {
        // Check if connection already exists
        val existingConnection = findExistingConnection(currentUserId, targetUserId)

        if (currentUserId == targetUserId) {
            return Result.failure(IllegalArgumentException("Cannot send connection request to yourself"))
        }

        existingConnection?.let {
            throw IllegalStateException("Connection already exists")
        }

        // Create two connection documents in a transaction to ensure atomicity
        val result = firestore.runTransaction { transaction ->
            // Create first connection (from sender to receiver)
            val senderConnectionRef = connectionsCollection.document()
            val senderConnection = ChatConnection(
                connectionId = senderConnectionRef.id,
                user1Id = currentUserId,
                user2Id = targetUserId,
                status = ConnectionStatus.PENDING,
                initiatorId = currentUserId
            )

            // Create second connection (from receiver to sender)
            val receiverConnectionRef = connectionsCollection.document()
            val receiverConnection = ChatConnection(
                connectionId = receiverConnectionRef.id,
                user1Id = targetUserId,
                user2Id = currentUserId,
                status = ConnectionStatus.PENDING,
                initiatorId = currentUserId,
                // Store reference to the counterpart connection
                counterpartConnectionId = senderConnectionRef.id
            )

            // Update first connection with reference to its counterpart
            val updatedSenderConnection = senderConnection.copy(
                counterpartConnectionId = receiverConnectionRef.id
            )

            // Set both documents in transaction
            transaction.set(senderConnectionRef, updatedSenderConnection)
            transaction.set(receiverConnectionRef, receiverConnection)

            updatedSenderConnection
        }.await()

        // Cache locally
        localDao.insertConnection(result.toEntity())

        result
    }

    override suspend fun acceptConnectionRequest(connectionId: String): Result<Unit> = runCatching {
        val connection = remoteDataSource.getConnectionById(connectionId).getOrThrow()

        // Use transaction to update both connection documents
        firestore.runTransaction { transaction ->
            // Update this connection
            val connectionRef = connectionsCollection.document(connectionId)
            transaction.update(connectionRef, "status", ConnectionStatus.ACCEPTED)

            // Update the counterpart connection
            if (connection.counterpartConnectionId.isNotEmpty()) {
                val counterpartRef = connectionsCollection.document(connection.counterpartConnectionId)
                transaction.update(counterpartRef, "status", ConnectionStatus.ACCEPTED)
            }
        }.await()

        // Update local cache
        localDao.updateConnectionStatus(connectionId, ConnectionStatus.ACCEPTED)
        if (connection.counterpartConnectionId.isNotEmpty()) {
            localDao.updateConnectionStatus(connection.counterpartConnectionId, ConnectionStatus.ACCEPTED)
        }
    }

    override suspend fun rejectConnectionRequest(connectionId: String): Result<Unit> = runCatching {
        val connection = remoteDataSource.getConnectionById(connectionId).getOrThrow()

        firestore.runTransaction { transaction ->
            // Update this connection
            transaction.update(connectionsCollection.document(connectionId), "status", ConnectionStatus.INITIAL)

            // Update counterpart
            if (connection.counterpartConnectionId.isNotEmpty()) {
                transaction.update(
                    connectionsCollection.document(connection.counterpartConnectionId),
                    "status",
                    ConnectionStatus.INITIAL
                )
            }
        }.await()

        // Update local cache for both documents
        localDao.updateConnectionStatus(connectionId, ConnectionStatus.INITIAL)
        if (connection.counterpartConnectionId.isNotEmpty()) {
            localDao.updateConnectionStatus(connection.counterpartConnectionId, ConnectionStatus.INITIAL)
        }
    }

    override suspend fun getPendingConnectionRequestsForUser(userId: String, asRecipient: Boolean): Flow<List<ChatConnection>> = withContext(Dispatchers.IO) {
        connectionsCollection
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", ConnectionStatus.PENDING)
            .apply {
                if (asRecipient) {
                    // Only show requests where the user is NOT the initiator
                    whereNotEqualTo("initiatorId", userId)
                } else {
                    // Only show requests where the user IS the initiator
                    whereEqualTo("initiatorId", userId)
                }
            }
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(ChatConnection::class.java) }
            }
    }

    // This should already be in your repository implementation
    override suspend fun getConnectionsForUser(userId: String): Flow<List<ChatConnection>> {
        return networkBoundResource(
            query = {
                // Query from local database
                localDao.getConnectionsForUser(userId)
                    .map { entities -> entities.map { it.toDomain() } }
            },
            fetch = {
                // Fetch from remote
                remoteDataSource.getConnectionsForUser(userId).first()
            },
            saveFetchResult = { connections ->
                // Save to local database
                connections.forEach { connection ->
                    localDao.insertConnection(connection.toEntity())
                }
            },
            shouldFetch = { cachedConnections ->
                // Decide based on your app's cache policy
                cachedConnections.isEmpty() ||
                        cachedConnections.any { shouldRefreshCache(it.lastInteractedAt) }
            },
            dispatcher = dispatcher
        )
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

    override suspend fun getConnectionStatus(userId1: String, userId2: String): Result<ConnectionStatus> {
        return try {
            val connection = findExistingConnection(userId1, userId2)
            if (connection != null) {
                Result.success(connection.status)
            } else {
                Result.failure(NoSuchElementException("Connection not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            initiatorId = initiatorId,
            counterpartConnectionId = counterpartConnectionId
        )
    }

    private fun ChatConnectionEntity.toDomain(): ChatConnection {
        return ChatConnection(
            connectionId = connectionId,
            user1Id = user1Id,
            user2Id = user2Id,
            status = ConnectionStatus.fromString(status.name),
            initiatedAt = initiatedAt,
            lastInteractedAt = lastInteractedAt,
            initiatorId = initiatorId,
            counterpartConnectionId = counterpartConnectionId
        )
    }
}