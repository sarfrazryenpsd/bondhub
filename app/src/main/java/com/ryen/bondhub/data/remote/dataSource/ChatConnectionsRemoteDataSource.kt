package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatConnectionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val connectionsCollection = firestore.collection("chat_connections")

    fun getConnectionsForUser(userId: String): Flow<List<ChatConnection>> = callbackFlow {
        val query = connectionsCollection
            .where(
                Filter.or(
                Filter.equalTo("user1Id", userId),
                Filter.equalTo("user2Id", userId)
            ))
            .orderBy("lastInteractedAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val connections = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatConnection::class.java)
            } ?: emptyList()

            trySend(connections)
        }

        awaitClose { registration.remove() }
    }

    suspend fun findExistingConnection(user1Id: String, user2Id: String): ChatConnection? {
        // Check both directions of connection
        val query1 = connectionsCollection
            .whereEqualTo("user1Id", user1Id)
            .whereEqualTo("user2Id", user2Id)
            .get()
            .await()

        val query2 = connectionsCollection
            .whereEqualTo("user1Id", user2Id)
            .whereEqualTo("user2Id", user1Id)
            .get()
            .await()

        val result1 = query1.documents.firstOrNull()?.toObject(ChatConnection::class.java)
        val result2 = query2.documents.firstOrNull()?.toObject(ChatConnection::class.java)

        return result1 ?: result2
    }

    suspend fun getConnectionById(connectionId: String): Result<ChatConnection> = runCatching {
        connectionsCollection
            .document(connectionId)
            .get()
            .await()
            .toObject(ChatConnection::class.java)
            ?: throw NoSuchElementException("Connection not found with ID: $connectionId")
    }

    fun getConnectionBetweenUsers(user1Id: String, user2Id: String): Flow<ChatConnection?> = callbackFlow {
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

    suspend fun getAcceptedConnectionsSnapshot(userId: String): List<ChatConnection> {
        return suspendCoroutine { continuation ->
            firestore.collection("chat_connections")
                .whereEqualTo("user1Id", userId)
                .whereEqualTo("status", ConnectionStatus.ACCEPTED)
                .get()
                .addOnSuccessListener { snapshot ->
                    val connections = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatConnection::class.java)
                    }
                    continuation.resume(connections)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}