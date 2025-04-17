package com.ryen.bondhub.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.local.entity.ChatEntity
import com.ryen.bondhub.data.mappers.ChatMapper
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.data.remote.dataSource.ChatRemoteDataSource
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import com.ryen.bondhub.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatMessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatMessageRemoteDataSource,
    private val chatRemoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatMessageDao,
    private val chatDao: ChatDao,
    private val chatMessageMapper: ChatMessageMapper,
    private val chatMapper: ChatMapper,
    private val userProfileRepo: UserProfileRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChatMessageRepository {

    override suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            // Get current user ID
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            // Create a new message with unique ID if not provided
            val finalMessage = if (message.messageId.isEmpty()) {
                message.copy(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    senderId = currentUserId,
                    status = MessageStatus.SENDING,
                )
            } else {
                message
            }

            // Extract baseChatId from the chatId (format: baseChatId_userId)
            val baseChatId = finalMessage.chatId.split("_").first()
            val messageWithBaseChatId = finalMessage.copy(baseChatId = baseChatId)

            // Save to local DB immediately for UI responsiveness
            localDataSource.insertMessage(chatMessageMapper.mapDomainToEntity(messageWithBaseChatId))

            // Update local chat with last message info
            chatDao.updateChatWithNewMessage(
                messageWithBaseChatId.chatId,
                messageWithBaseChatId.content,
                messageWithBaseChatId.timestamp
            )

            // Check if chat exists in Firestore
            val existsResult = chatRemoteDataSource.getChatsByBaseChatId(baseChatId)
            val chatExists = existsResult.isSuccess && (existsResult.getOrNull()?.isNotEmpty() == true)

            // If chat doesn't exist in Firestore, create it first
            if (!chatExists) {
                // Extract user IDs from the chat
                val chatEntity = chatDao.getChatById(messageWithBaseChatId.chatId)
                    ?: return Result.failure(Exception("Chat not found in local database"))

                val participants = chatEntity.participants
                if (participants.size != 2) {
                    return Result.failure(Exception("Invalid participants list"))
                }

                val userId1 = participants[0]
                val userId2 = participants[1]

                // Create both chat documents in Firestore
                val createChatResult = chatRemoteDataSource.createChat(chatMapper.mapEntityToRemoteMap(chatEntity))
                if (createChatResult.isFailure) {
                    return Result.failure(createChatResult.exceptionOrNull() ?: Exception("Failed to create chat"))
                }

                // Also create chat for other user
                val otherUserId = if (userId1 == currentUserId) userId2 else userId1
                val otherUserProfile = userProfileRepo.getUserProfile(otherUserId).getOrNull()
                    ?: return Result.failure(Exception("Other user profile not found"))

                val otherUserChatId = "${baseChatId}_${otherUserId}"

                // We need to implement logic to create other user's chat document
                val otherUserChatEntity = ChatEntity(
                    chatId = otherUserChatId,
                    baseChatId = baseChatId,
                    connectionId = chatEntity.connectionId,
                    participants = listOf(otherUserId, currentUserId),
                    profilePictureUrlThumbnail = otherUserProfile.profilePictureThumbnailUrl!!,
                    displayName = otherUserProfile.displayName,
                    lastMessage = messageWithBaseChatId.content,
                    lastMessageTime = messageWithBaseChatId.timestamp,
                    unreadMessageCount = 1
                )

                val createOtherResult = chatRemoteDataSource.createChat(chatMapper.mapEntityToRemoteMap(otherUserChatEntity))
                if (createOtherResult.isFailure) {
                    return Result.failure(createOtherResult.exceptionOrNull() ?: Exception("Failed to create other user's chat"))
                }
            }

            // Send message to remote
            val remoteMessage = chatMessageMapper.mapDomainToRemote(messageWithBaseChatId)
            remoteDataSource.sendMessage(remoteMessage).fold(
                onSuccess = {
                    // Update status to SENT
                    val updatedMessage = messageWithBaseChatId.copy(status = MessageStatus.SENT)
                    remoteDataSource.updateMessageStatus(baseChatId, messageWithBaseChatId.messageId, MessageStatus.SENT)
                    localDataSource.updateMessageStatus(messageWithBaseChatId.messageId, MessageStatus.SENT.name)

                    // Update both chat documents with last message info
                    chatRemoteDataSource.updateChatLastMessage(baseChatId, updatedMessage)

                    Result.success(updatedMessage)
                },
                onFailure = {
                    // Update status to FAILED
                    val failedMessage = messageWithBaseChatId.copy(status = MessageStatus.FAILED)
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
            // Extract baseChatId from chatId
            val baseChatId = chatId.split("_").first()

            // Get remote messages
            val remoteMessages = remoteDataSource.getMessages(baseChatId)

            remoteMessages
                .onEach { messageList ->
                    // Store the remote messages in local database
                    val entities = messageList.map { chatMessageMapper.mapDomainToEntity(it) }
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
                        localDataSource.getMessagesByBaseChatId(baseChatId)
                            .map { entities -> entities.map { chatMessageMapper.mapEntityToDomain(it) } }
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

    // Improved listenForNewMessages implementation
    override suspend fun listenForNewMessages(baseChatId: String): Flow<List<ChatMessage>> = callbackFlow {
        // First emit cached messages immediately
        try {
            val cachedMessages = localDataSource.getMessagesByBaseChatId(baseChatId)
                .first()
                .map { chatMessageMapper.mapEntityToDomain(it) }

            if (cachedMessages.isNotEmpty()) {
                send(cachedMessages)
            }
        } catch (e: Exception) {
            Log.e("ChatMessageRepository", "Error loading cached messages", e)
            // Don't close the flow on cache error, just continue to the listener
        }

        // Set up real-time listener
        val listener = try {
            firestore.collection("messages")
                .document(baseChatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatMessageRepository", "Error in message listener", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        try {
                            val messages = snapshot.documents.mapNotNull { doc ->
                                try {
                                    chatMessageMapper.mapDocumentToDomain(doc)
                                } catch (e: Exception) {
                                    Log.e("ChatMessageRepository", "Error mapping message", e)
                                    null
                                }
                            }

                            // Update local cache
                            // Use a scope that won't cause the flow to be cancelled if it fails
                            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                                try {
                                    val entities = messages.map { chatMessageMapper.mapDomainToEntity(it) }
                                    localDataSource.insertMessages(entities)
                                } catch (e: Exception) {
                                    Log.e("ChatMessageRepository", "Failed to update local cache", e)
                                }
                            }

                            // Send to UI immediately
                            trySend(messages)
                        } catch (e: Exception) {
                            Log.e("ChatMessageRepository", "Error processing messages", e)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatMessageRepository", "Failed to set up message listener", e)
            close(e)
            return@callbackFlow
        }

        // IMPORTANT: awaitClose must be the last thing in the callbackFlow block
        awaitClose {
            listener.remove()
            Log.d("ChatMessageRepository", "Message listener removed for baseChatId: $baseChatId")
        }
    }


    override suspend fun getUnreadMessagesCount(baseChatId: String, userId: String): Flow<Int> {
        return remoteDataSource.getUnreadMessagesCount(baseChatId, userId).catch {
            emit(0) // Default to 0 on error
        }
    }

    override suspend fun markAllMessagesAsRead(chatId: String, receiverId: String): Result<Unit> {
        return try {
            // Extract baseChatId from chatId
            val baseChatId = chatId.split("_").firstOrNull()
                ?: return Result.failure(Exception("Invalid chatId format"))

            // Update message status in Firestore
            val remoteResult = remoteDataSource.updateAllMessageStatus(
                baseChatId,
                receiverId,
                MessageStatus.READ.name
            )

            if (remoteResult.isSuccess) {
                // Update local database
                localDataSource.updateAllMessageStatus(chatId, receiverId, MessageStatus.READ.name)

                // Reset unread message count in chat
                chatDao.markChatAsRead(chatId)

                // Also update the remote chat document
                chatRemoteDataSource.markChatAsRead(chatId)

                Result.success(Unit)
            } else {
                remoteResult
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}