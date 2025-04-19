package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.domain.model.ChatMessage
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

    fun getUserChats(userId: String): Flow<List<DocumentSnapshot>> {
        return callbackFlow {
            val listenerRegistration = chatsCollection
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // Filter documents to only include those owned by this user
                        val userChats = snapshot.documents.filter {
                            it.id.endsWith("_$userId")
                        }
                        trySend(userChats)
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        }
    }

    // Add this method to ChatRemoteDataSource
    suspend fun createMessageCollection(baseChatId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Create an empty document at the messages collection path
            firestore.collection("messages")
                .document(baseChatId)
                .set(hashMapOf("created" to FieldValue.serverTimestamp()))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatsByBaseChatId(baseChatId: String): Result<List<DocumentSnapshot>> {
        return try {
            val querySnapshot = chatsCollection
                .whereEqualTo("baseChatId", baseChatId)
                .get()
                .await()

            Result.success(querySnapshot.documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatsCollection.document(chatId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChatLastMessage(baseChatId: String, message: ChatMessage): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get all chats associated with this baseChatId
            val chatsQuery = chatsCollection
                .whereEqualTo("baseChatId", baseChatId)
                .get()
                .await()

            // Update each chat with the new message info
            val batch = firestore.batch()
            for (chatDoc in chatsQuery.documents) {
                val chatId = chatDoc.id
                val chatOwnerId = chatId.split("_").last()

                // Update unread count if this user is the receiver (not the sender)
                val unreadIncrement = if (chatOwnerId != message.senderId) {
                    FieldValue.increment(1)
                } else {
                    FieldValue.increment(0) // No change for sender
                }

                // Update last message info
                val chatRef = chatsCollection.document(chatId)
                batch.update(chatRef, mapOf(
                    "lastMessage" to message.content,
                    "lastMessageTime" to message.timestamp,
                    "unreadMessageCount" to unreadIncrement
                ))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markChatAsRead(chatId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val chatRef = chatsCollection.document(chatId)
            chatRef.update("unreadMessageCount", 0).await()
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