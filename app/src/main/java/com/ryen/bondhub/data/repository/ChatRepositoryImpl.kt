package com.ryen.bondhub.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.mappers.ChatMapper
import com.ryen.bondhub.data.remote.dataSource.ChatRemoteDataSource
import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.ChatRepository
import com.ryen.bondhub.domain.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatDao,
    private val userProfileRepository: UserProfileRepository,
    private val mapper: ChatMapper,
    private val auth: FirebaseAuth
) : ChatRepository {

    override suspend fun createChat(userId1: String, userId2: String): Result<Chat> {
        return try {
            // Get current user ID
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val otherUserId = if (userId1 == currentUserId) userId2 else userId1

            // Generate a unique baseChatId - only needed if chat doesn't exist
            val baseChatId = UUID.randomUUID().toString()
            val chatId = "${baseChatId}_${currentUserId}"

            // Check if a chat already exists locally
            val existingChats = localDataSource.getUserChats(currentUserId)
                .first()
                .filter { entity ->
                    entity.participants.contains(otherUserId) &&
                            entity.chatId.endsWith("_$currentUserId")
                }

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

            // Create chat object for local use only - no remote storage yet
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

            // Create both user chat documents in the chats collection (not messages)
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

            // Create the message collection document in Firestore if it doesn't exist
            // This ensures the path exists before we try to write messages
            val messageCollectionResult = remoteDataSource.createMessageCollection(baseChatId)
            if (messageCollectionResult.isFailure) {
                return Result.failure(messageCollectionResult.exceptionOrNull() ?:
                Exception("Failed to create message collection"))
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserChats(userId: String): Flow<List<Chat>> = flow {
        try {
            // First emit from local database
            val localChats = localDataSource.getAllChats()
                .first() // Get first emission
                .filter { it.chatId.endsWith("_$userId") } // Filter by owner
                .map { mapper.mapEntityToDomain(it) }
                .sortedByDescending { it.lastMessageTime }

            // Always emit something - either local chats or empty list
            emit(localChats)

            // Then collect from remote source
            remoteDataSource.getUserChats(userId)
                .catch { exception ->
                    Log.e("ChatRepository", "Error fetching remote chats", exception)
                    // Important: Emit empty list on error to prevent getting stuck
                    emit(emptyList())
                }
                .collect { documents ->
                    val remoteChats = documents.mapNotNull {
                        try {
                            // Double check the chat belongs to this user
                            if (it.id.endsWith("_$userId")) {
                                mapper.mapDocumentToDomain(it)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("ChatRepository", "Error mapping document to chat", e)
                            null
                        }
                    }

                    // Update local cache even if empty
                    withContext(Dispatchers.IO) {
                        localDataSource.insertChats(remoteChats.map { mapper.mapDomainToEntity(it) })
                    }

                    // Always emit the latest result, even if empty
                    emit(remoteChats.sortedByDescending { it.lastMessageTime })
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error in getUserChats", e)

            // On outer exception, try to get data from local source
            try {
                val fallbackLocalChats = localDataSource.getUserChats(userId)
                    .first()
                    .map { mapper.mapEntityToDomain(it) }
                    .sortedByDescending { it.lastMessageTime }

                emit(fallbackLocalChats)
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

}