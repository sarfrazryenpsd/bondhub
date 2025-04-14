package com.ryen.bondhub.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.mappers.ChatMapper
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatMessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatMessageRemoteDataSource,
    private val localDataSource: ChatMessageDao,
    private val chatDao: ChatDao,
    private val mapper: ChatMessageMapper,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatMessageRepository {
    override suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            // Create a new message with unique ID if not provided
            val finalMessage = if (message.messageId.isEmpty()) {
                message.copy(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    senderId = auth.currentUser?.uid ?: message.senderId,
                    status = MessageStatus.SENDING,
                )
            } else {
                message
            }

            // Save to local DB immediately for UI responsiveness
            localDataSource.insertMessage(mapper.mapDomainToEntity(finalMessage))

            // Update local chat with last message info
            chatDao.updateChatWithNewMessage(
                finalMessage.chatId,
                finalMessage.content,
                finalMessage.timestamp
            )

            // Send message to remote
            val remoteMessage = mapper.mapDomainToRemote(finalMessage)

            remoteDataSource.sendMessage(remoteMessage).fold(
                onSuccess = {
                    // Update status to SENT
                    val sentMessage = finalMessage.copy(status = MessageStatus.SENT)
                    remoteDataSource.updateMessageStatus(messageId = finalMessage.messageId,status = MessageStatus.SENT)
                    localDataSource.updateMessageStatus(sentMessage.messageId, MessageStatus.SENT.name)

                    // Update Firestore chat document with last message info
                    updateChatLastMessage(sentMessage.chatId, sentMessage.content, sentMessage.timestamp)

                    Result.success(sentMessage)
                },
                onFailure = {
                    // Update status to FAILED
                    val failedMessage = finalMessage.copy(status = MessageStatus.FAILED)
                    localDataSource.updateMessageStatus(failedMessage.messageId, MessageStatus.FAILED.name)
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatMessages(chatId: String): Flow<List<ChatMessage>> {
        return try {
            val remoteMessages = remoteDataSource.getMessages(chatId)

            remoteMessages
                .onEach { messageList ->
                    // Store the remote messages in local database
                    val entities = messageList.map { mapper.mapDomainToEntity(it) }
                    localDataSource.insertMessages(entities)
                }
                .flowOn(Dispatchers.IO)
                .catch {
                    // Fallback to local data source on error
                    emit(emptyList())
                }
                .flatMapConcat { remoteResult ->
                    if (remoteResult.isEmpty()) {
                        // If remote returns empty, use local data
                        localDataSource.getChatMessages(chatId)
                            .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
                    } else {
                        // Otherwise use the remote result
                        flow { emit(remoteResult) }
                    }
                }
        } catch (e: Exception) {
            // In case of any exception, return empty list
            flow { emit(emptyList()) }
        }
    }

    override suspend fun listenForNewMessages(chatId: String): Flow<ChatMessage> {
        return callbackFlow {
            val listenerRegistration = firestore.collection("messages")
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val messageDoc = snapshot.documents[0]
                        val messageData = messageDoc.data
                        if (messageData != null) {
                            try {
                                val message = mapper.mapRemoteToDomain(messageData)

                                // Update the chat's last message in both local and remote
                                launch(Dispatchers.IO){
                                    chatDao.updateChatWithNewMessage(
                                        chatId,
                                        message.content,
                                        message.timestamp
                                    )

                                    // Only update Firestore for messages we received (not sent)
                                    if (message.senderId != auth.currentUser?.uid) {
                                        updateChatLastMessage(
                                            chatId,
                                            message.content,
                                            message.timestamp
                                        )
                                    }
                                }

                                trySend(message)
                            } catch (e: Exception) {
                                Log.e("ChatMessageRepository", "Error processing new message: ${e.message}")
                            }
                        }
                    }
                }

            awaitClose { listenerRegistration.remove() }
        }
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus
    ): Result<Unit> {
        return try {
            remoteDataSource.updateMessageStatus(messageId, status).fold(
                onSuccess = {
                    // Update local database to reflect the status change
                    localDataSource.updateMessageStatus(messageId, status.name)
                    Result.success(Unit)
                },
                onFailure = {
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int> {
        return try {
            // First try to get from remote
            remoteDataSource.getUnreadMessagesCount(connectionId, userId)
                .catch {
                    // On error, fall back to local data
                    localDataSource.getUnreadMessagesCount(connectionId, userId)
                }
                .onEach { count ->
                    // Update chat unread count in local DB
                    chatDao.updateUnreadMessageCount(connectionId, count)
                }
        } catch (e: Exception) {
            // If everything fails, return zero
            flow { emit(0) }
        }
    }

    override suspend fun markAllMessagesAsRead(chatId: String, receiverId: String): Result<Unit> {
        return try {
            remoteDataSource.updateAllMessageStatus(chatId, receiverId, MessageStatus.READ.name).fold(
                onSuccess = {
                    // Also update in local database
                    localDataSource.updateAllMessageStatus(chatId, receiverId, MessageStatus.READ.name)
                    chatDao.markChatAsRead(chatId)
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateChatLastMessage(chatId: String, lastMessage: String, lastMessageTime: Long) {
        try {
            val chatRef = firestore.collection("chats").document(chatId)

            // First check if the chat exists in Firestore
            val chatDoc = chatRef.get().await()

            if (chatDoc.exists()) {
                // Update existing chat
                chatRef.update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTime" to lastMessageTime
                    )
                ).await()
            } else {
                // This is the first message - need to create the chat document in Firestore
                // Get the chat from local DB
                val chatEntity = chatDao.getChatById(chatId)
                if (chatEntity != null) {
                    val chatMapper = ChatMapper()
                    val chat = chatMapper.mapEntityToDomain(chatEntity)

                    // Create a new chat document with updated last message
                    val chatWithLastMessage = chat.copy(
                        lastMessage = lastMessage,
                        lastMessageTime = lastMessageTime
                    )

                    // Save to Firestore
                    val chatData = chatMapper.mapDomainToMap(chatWithLastMessage)
                    chatRef.set(chatData).await()
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to update chat last message: ${e.message}")
            // Don't fail the overall operation if this update fails
        }
    }

}