package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun getConnectionBetweenUsers(user1Id: String, user2Id: String): Flow<ChatConnection?> = callbackFlow {
        // We need to check for connection in both directions since user1 or user2 could be the initiator
        val query1 = connectionsCollection
            .whereEqualTo("user1Id", user1Id)
            .whereEqualTo("user2Id", user2Id)

        val query2 = connectionsCollection
            .whereEqualTo("user1Id", user2Id)
            .whereEqualTo("user2Id", user1Id)

        val registration1 = query1.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }

            val connection = snapshot?.documents?.firstOrNull()?.toObject(ChatConnection::class.java)
            if (connection != null) {
                trySend(connection)
            } else {
                // Only check the second query if the first returned no results
                query2.get().addOnSuccessListener { snapshot2 ->
                    val connection2 = snapshot2.documents.firstOrNull()?.toObject(ChatConnection::class.java)
                    trySend(connection2)
                }.addOnFailureListener {
                    trySend(null)
                }
            }
        }

        awaitClose { registration1.remove() }
    }
}