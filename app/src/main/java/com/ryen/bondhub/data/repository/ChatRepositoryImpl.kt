package com.ryen.bondhub.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.mappers.ChatMapper
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatRemoteDataSource
import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatRepository
import com.ryen.bondhub.domain.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatDao,
    private val userProfileRepository: UserProfileRepository,
    private val mapper: ChatMapper,
    private val messageMapper: ChatMessageMapper,
    private val auth: FirebaseAuth
) : ChatRepository {

    override suspend fun createChat(userId1: String, userId2: String): Result<Chat> {
        return try {
            // Get current user ID
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val otherUserId = if (userId1 == currentUserId) userId2 else userId1

            // Generate a unique baseChatId
            val baseChatId = UUID.randomUUID().toString()
            val chatId = "${baseChatId}_${currentUserId}"

            // Check if a chat with this baseChatId already exists locally
            val existingChats = localDataSource.getChatsByBaseChatId(baseChatId)
            if (existingChats.isNotEmpty()) {
                val existingChat = mapper.mapEntityToDomain(existingChats.first())
                return Result.success(existingChat)
            }

            // Check if connection exists between users
            val connectionResult = remoteDataSource.getChatConnection(userId1, userId2)
            if (connectionResult.isFailure) {
                return Result.failure(connectionResult.exceptionOrNull() ?: Exception("Failed to get connection"))
            }

            val connectionDoc = connectionResult.getOrNull()
                ?: return Result.failure(Exception("No connection exists between these users"))

            val connectionId = connectionDoc.id
            val connectionStatus = connectionDoc.getString("status")

            if (connectionStatus != ConnectionStatus.ACCEPTED.name) {
                return Result.failure(Exception("Connection is not active"))
            }

            // Get other user's profile
            val otherUserProfileResult = userProfileRepository.getUserProfile(otherUserId)
            if (otherUserProfileResult.isFailure) {
                return Result.failure(otherUserProfileResult.exceptionOrNull() ?: Exception("Failed to get other user profile"))
            }

            val otherUserProfile = otherUserProfileResult.getOrNull()
                ?: return Result.failure(Exception("Other user profile not found"))

            // Create chat object for current user with other user's profile info
            val currentUserChat = Chat(
                chatId = chatId,
                baseChatId = baseChatId,
                connectionId = connectionId,
                participants = listOf(currentUserId, otherUserId),
                profilePictureUrlThumbnail = otherUserProfile.profilePictureThumbnailUrl ?: "",
                displayName = otherUserProfile.displayName,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                unreadMessageCount = 0
            )

            // Save to local database only (will save to remote when first message is sent)
            localDataSource.insertChat(mapper.mapDomainToEntity(currentUserChat))

            return Result.success(currentUserChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkChatExistsRemotely(chatId: String): Result<Boolean> {
        return try {
            val result = remoteDataSource.getChatById(chatId)
            Result.success(result.isSuccess && result.getOrNull() != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkChatExistsByBaseChatId(baseChatId: String): Result<Boolean> {
        return try {
            val result = remoteDataSource.getChatsByBaseChatId(baseChatId)
            Result.success(result.isSuccess && (result.getOrNull()?.isNotEmpty() == true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createChatInFirestore(chatId: String, userId1: String, userId2: String): Result<Unit> {
        return try {
            // Get the chat from local database
            val chatEntity = localDataSource.getChatById(chatId)
                ?: return Result.failure(Exception("Chat not found in local database"))

            // Map to domain model
            val chat = mapper.mapEntityToDomain(chatEntity)

            // Get base chatId from the chat
            val baseChatId = chat.baseChatId

            // Create both user chat documents

            // 1. Current user's chat document
            val currentUserChatMap = mapper.mapDomainToMap(chat)
            val currentUserResult = remoteDataSource.createChat(currentUserChatMap)

            if (currentUserResult.isFailure) {
                return Result.failure(currentUserResult.exceptionOrNull() ?: Exception("Failed to create current user chat"))
            }

            // 2. Get current user's profile for the other user's chat document
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val otherUserId = if (userId1 == currentUserId) userId2 else userId1

            val currentUserProfileResult = userProfileRepository.getUserProfile(currentUserId)
            if (currentUserProfileResult.isFailure) {
                return Result.failure(currentUserProfileResult.exceptionOrNull() ?: Exception("Failed to get current user profile"))
            }

            val currentUserProfile = currentUserProfileResult.getOrNull()
                ?: return Result.failure(Exception("Current user profile not found"))

            // 3. Create other user's chat document
            val otherUserChatId = "${baseChatId}_${otherUserId}"
            val otherUserChat = Chat(
                chatId = otherUserChatId,
                baseChatId = baseChatId,
                connectionId = chat.connectionId,
                participants = listOf(otherUserId, currentUserId),
                profilePictureUrlThumbnail = currentUserProfile.profilePictureThumbnailUrl ?: "",
                displayName = currentUserProfile.displayName,
                lastMessage = chat.lastMessage,
                lastMessageTime = chat.lastMessageTime,
                unreadMessageCount = 1  // New message notification for other user
            )

            val otherUserChatMap = mapper.mapDomainToMap(otherUserChat)
            val otherUserResult = remoteDataSource.createChat(otherUserChatMap)

            if (otherUserResult.isFailure) {
                return Result.failure(otherUserResult.exceptionOrNull() ?: Exception("Failed to create other user chat"))
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserChats(userId: String): Flow<List<Chat>> = flow {
        try {
            // First emit from local database to show something immediately
            val localChats = localDataSource.getUserChats(userId)
                .first() // Get first emission from the flow
                .map { mapper.mapEntityToDomain(it) }
                .sortedByDescending { it.lastMessageTime } // Sort by most recent

            if (localChats.isNotEmpty()) {
                emit(localChats) // Emit local data first if available
            }

            // Then collect from remote source
            remoteDataSource.getUserChats(userId)
                .catch { exception ->
                    Log.e("ChatRepository", "Error fetching remote chats", exception)
                    // Don't emit anything here, just log the error
                }
                .collect { documents ->
                    val remoteChats = documents.mapNotNull {
                        try {
                            mapper.mapDocumentToDomain(it)
                        } catch (e: Exception) {
                            Log.e("ChatRepository", "Error mapping document to chat", e)
                            null
                        }
                    }

                    // Only update and emit if we got actual data
                    if (remoteChats.isNotEmpty()) {
                        // Update local cache
                        withContext(Dispatchers.IO) {
                            localDataSource.insertChats(remoteChats.map { mapper.mapDomainToEntity(it) })
                        }

                        emit(remoteChats.sortedByDescending { it.lastMessageTime }) // Sort by most recent
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error in getUserChats", e)

            // On outer exception, try to get data from local source
            try {
                val fallbackLocalChats = localDataSource.getUserChats(userId)
                    .first()
                    .map { mapper.mapEntityToDomain(it) }
                    .sortedByDescending { it.lastMessageTime } // Sort by most recent

                emit(fallbackLocalChats) // Emit local data as fallback
            } catch (fallbackException: Exception) {
                Log.e("ChatRepository", "Failed to get local chats as fallback", fallbackException)
                emit(emptyList()) // As a last resort, emit empty list
            }
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            // Delete from remote first (only the current user's chat document)
            val remoteResult = remoteDataSource.deleteChat(chatId)

            if (remoteResult.isSuccess) {
                // If remote delete succeeded, also delete locally
                localDataSource.deleteChat(chatId)
                Result.success(Unit)
            } else {
                remoteResult // Return the remote failure
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastChatMessage(chatId: String): Flow<Result<ChatMessage>> {
        // Extract baseChatId from chatId (format: baseChatId_userId)
        val baseChatId = chatId.split("_").first()

        return flow {
            try {
                // Get the latest message from the message repository
                // This would typically be implemented differently, but for now we'll simulate it
                val messagesResult = remoteDataSource.getChatsByBaseChatId(baseChatId)

                if (messagesResult.isSuccess) {
                    val chatDocs = messagesResult.getOrNull() ?: emptyList()
                    if (chatDocs.isNotEmpty()) {
                        val latestChat = chatDocs.maxByOrNull {
                            it.getLong("lastMessageTime") ?: 0L
                        }

                        if (latestChat != null) {
                            val lastMessageContent = latestChat.getString("lastMessage") ?: ""
                            val lastMessageTime = latestChat.getLong("lastMessageTime") ?: 0L

                            // Create a dummy message with available info
                            val message = ChatMessage(
                                messageId = UUID.randomUUID().toString(),
                                chatId = chatId,
                                baseChatId = baseChatId,
                                content = lastMessageContent,
                                timestamp = lastMessageTime,
                                status = MessageStatus.SENT
                            )

                            emit(Result.success(message))
                        } else {
                            emit(Result.failure(Exception("No messages found")))
                        }
                    } else {
                        emit(Result.failure(Exception("No chat documents found")))
                    }
                } else {
                    emit(Result.failure(messagesResult.exceptionOrNull() ?: Exception("Failed to get chat")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }
}