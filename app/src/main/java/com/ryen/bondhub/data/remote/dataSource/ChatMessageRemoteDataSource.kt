package com.ryen.bondhub.data.remote.dataSource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatMessageRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val messagesCollection = firestore.collection("messages")

    suspend fun sendMessage(messageMap: Map<String, Any?>): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val messageId = messageMap["messageId"] as String
            val baseChatId = messageMap["baseChatId"] as String

            // Store messages in a sub-collection under the baseChatId
            messagesCollection.document(baseChatId)
                .collection("messages")
                .document(messageId)
                .set(messageMap)
                .await()

            // Convert back to ChatMessage for return
            val message = ChatMessage(
                messageId = messageId,
                chatId = messageMap["chatId"] as String,
                baseChatId = baseChatId,
                senderId = messageMap["senderId"] as String,
                receiverId = messageMap["receiverId"] as String,
                content = messageMap["content"] as String,
                timestamp = messageMap["timestamp"] as Long,
                messageType = MessageType.valueOf(messageMap["messageType"] as String),
                status = MessageStatus.valueOf(messageMap["status"] as String)
            )

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(baseChatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = messagesCollection
            .document(baseChatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { document ->
                    try {
                        ChatMessage(
                            messageId = document.id,
                            chatId = document.getString("chatId") ?: "",
                            baseChatId = document.getString("baseChatId") ?: "",
                            senderId = document.getString("senderId") ?: "",
                            receiverId = document.getString("receiverId") ?: "",
                            content = document.getString("content") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0L,
                            messageType = MessageType.valueOf(document.getString("messageType") ?: MessageType.TEXT.name),
                            status = MessageStatus.valueOf(document.getString("status") ?: MessageStatus.SENT.name)
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun updateMessageStatus(baseChatId: String, messageId: String, status: MessageStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            messagesCollection.document(baseChatId)
                .collection("messages")
                .document(messageId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAllMessageStatus(baseChatId: String, receiverId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Create a batch to update multiple messages efficiently
            val batch = firestore.batch()

            // Query for all messages in the chat that are for the receiver and not yet read
            val messagesToUpdate = messagesCollection
                .document(baseChatId)
                .collection("messages")
                .whereEqualTo("receiverId", receiverId)
                .whereNotEqualTo("status", status)
                .get()
                .await()

            // Add each document to the batch update
            for (document in messagesToUpdate.documents) {
                val messageRef = messagesCollection
                    .document(baseChatId)
                    .collection("messages")
                    .document(document.id)
                batch.update(messageRef, "status", status)
            }

            // Commit the batch
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUnreadMessagesCount(baseChatId: String, userId: String): Flow<Int> = callbackFlow {
        val subscription = messagesCollection
            .document(baseChatId)
            .collection("messages")
            .whereEqualTo("receiverId", userId)
            .whereNotEqualTo("status", MessageStatus.READ.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val count = snapshot?.documents?.size ?: 0
                trySend(count)
            }

        awaitClose { subscription.remove() }
    }
}