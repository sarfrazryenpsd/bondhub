package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val chatsCollection = firestore.collection("chats")
    private val connectionsCollection = firestore.collection("chat_connections")

    suspend fun createChat(chat: Map<String, Any?>): Result<DocumentSnapshot> = withContext(Dispatchers.IO) {
        try {
            val chatId = chat["chatId"] as String
            chatsCollection.document(chatId).set(chat).await()
            val chatDoc = chatsCollection.document(chatId).get().await()
            Result.success(chatDoc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserChats(userId: String): Flow<List<DocumentSnapshot>> = callbackFlow {
        val subscription = chatsCollection
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents ?: emptyList()
                trySend(chats)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatsCollection.document(chatId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatConnection(userId1: String, userId2: String): Result<DocumentSnapshot?> = withContext(Dispatchers.IO) {
        try {
            val connections = connectionsCollection
                .whereEqualTo("user1Id", userId1)
                .whereEqualTo("user2Id", userId2)
                .limit(1)
                .get()
                .await()

            if (!connections.isEmpty) {
                Result.success(connections.documents.first())
            } else {
                // Try the reverse order
                val reverseConnections = connectionsCollection
                    .whereEqualTo("user1Id", userId2)
                    .whereEqualTo("user2Id", userId1)
                    .limit(1)
                    .get()
                    .await()

                if (!reverseConnections.isEmpty) {
                    Result.success(reverseConnections.documents.first())
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}