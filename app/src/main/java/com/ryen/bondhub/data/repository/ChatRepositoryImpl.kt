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
            // First check if a chat already exists between these users
            val existingChatResult = remoteDataSource.getUserChat(userId1, userId2)

            if (existingChatResult.isSuccess && existingChatResult.getOrNull() != null) {
                // Chat exists, map it to domain model and return
                val chatDoc = existingChatResult.getOrNull()!!
                val chatData = chatDoc.data ?: return Result.failure(Exception("Invalid chat data"))
                val chat = mapper.mapRemoteToDomain(chatDoc.id, chatData)

                // Make sure it's in the local database
                localDataSource.insertChat(mapper.mapDomainToEntity(chat))

                return Result.success(chat)
            }

            // First check if connection exists
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

            // Get other user's profile for display info
            val otherUserId = if (userId1 == auth.currentUser?.uid) userId2 else userId1
            val userProfileResult = userProfileRepository.getUserProfile(otherUserId)

            if (userProfileResult.isFailure) {
                return Result.failure(userProfileResult.exceptionOrNull() ?: Exception("Failed to get user profile"))
            }

            val userProfile = userProfileResult.getOrNull()
                ?: return Result.failure(Exception("User profile not found"))

            // Create chat object
            val chatId = UUID.randomUUID().toString()
            val chat = Chat(
                chatId = chatId,
                connectionId = connectionId,
                participants = listOf(userId1, userId2).sorted(),  // Sort for consistency
                profilePictureUrlThumbnail = userProfile.profilePictureThumbnailUrl ?: "",
                displayName = userProfile.displayName,
                lastMessage = "",  // Empty initially
                lastMessageTime = System.currentTimeMillis(),
                unreadMessageCount = 0
            )

            // Save to local database only (will save to remote when first message is sent)
            localDataSource.insertChat(mapper.mapDomainToEntity(chat))

            Result.success(chat)
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

    override suspend fun createChatInFirestore(chatId: String, userId1: String, userId2: String): Result<Unit> {
        return try {
            // Get the chat from local database
            val chatEntity = localDataSource.getChatById(chatId)
                ?: return Result.failure(Exception("Chat not found in local database"))

            // Map to domain model
            val chat = mapper.mapEntityToDomain(chatEntity)

            // Save to remote
            val chatMap = mapper.mapDomainToMap(chat)
            val result = remoteDataSource.createChat(chatMap)

            // Transform the DocumentSnapshot result to Unit result
            result.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastChatMessage(chatId: String): Flow<Result<ChatMessage>> {
        return remoteDataSource.getLastChatMessage(chatId)
            .map { result ->
                result.map { messageEntity ->
                    messageMapper.mapEntityToDomain(messageEntity)
                }
            }
    }

    override suspend fun getUserChats(userId: String): Flow<List<Chat>> = flow {
        try {
            // First emit from local database to show something immediately
            val localChats = localDataSource.getUserChats(userId)
                .first() // Get first emission from the flow
                .map { mapper.mapEntityToDomain(it) }

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
                    val remoteChats = documents.map { mapper.mapDocumentToDomain(it) }

                    // Only update and emit if we got actual data
                    if (remoteChats.isNotEmpty()) {
                        // Update local cache
                        withContext(Dispatchers.IO) {
                            localDataSource.insertChats(remoteChats.map { mapper.mapDomainToEntity(it) })
                        }

                        emit(remoteChats) // Emit remote data
                    }
                }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error in getUserChats", e)

            // On outer exception, try to get data from local source
            try {
                val fallbackLocalChats = localDataSource.getUserChats(userId)
                    .first()
                    .map { mapper.mapEntityToDomain(it) }

                emit(fallbackLocalChats) // Emit local data as fallback
            } catch (fallbackException: Exception) {
                Log.e("ChatRepository", "Failed to get local chats as fallback", fallbackException)
                emit(emptyList()) // As a last resort, emit empty list
            }
        }
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            // Delete from remote first
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