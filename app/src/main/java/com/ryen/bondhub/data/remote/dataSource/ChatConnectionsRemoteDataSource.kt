package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatConnectionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val connectionsCollection = firestore.collection("chat_connections")

    suspend fun createConnection(connection: ChatConnection): ChatConnection {
        val docRef = connectionsCollection.add(connection).await()
        return connection.copy(connectionId = docRef.id)
    }

    suspend fun updateConnectionStatus(
        connectionId: String,
        status: ConnectionStatus
    ): Result<Unit> = runCatching {
        connectionsCollection
            .document(connectionId)
            .update("status", status)
            .await()
    }

    suspend fun getConnectionsForUser(userId: String): List<ChatConnection> {
        return connectionsCollection
            .whereIn("user1Id", listOf(userId))
            .whereIn("user2Id", listOf(userId))
            .orderBy("lastInteractedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(ChatConnection::class.java)
    }

    suspend fun findExistingConnection(
        user1Id: String,
        user2Id: String
    ): ChatConnection? {
        return connectionsCollection
            .whereIn("user1Id", listOf(user1Id, user2Id))
            .whereIn("user2Id", listOf(user1Id, user2Id))
            .get()
            .await()
            .toObjects(ChatConnection::class.java)
            .firstOrNull()
    }
}