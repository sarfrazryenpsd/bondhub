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

    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val messageMap = mapOf(
                "messageId" to message.messageId,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "messageType" to message.messageType.name,
                "status" to message.status.name
            )

            messagesCollection.document(message.messageId).set(messageMap).await()
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(connectionId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = messagesCollection
            .whereEqualTo("connectionId", connectionId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { document ->
                    try {
                        ChatMessage(
                            messageId = document.getString("messageId") ?: "",
                            chatId = document.getString("chatId") ?: "",
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

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            messagesCollection.document(messageId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            messagesCollection.document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}