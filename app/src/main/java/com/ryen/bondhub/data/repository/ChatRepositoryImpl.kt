package com.ryen.bondhub.data.repository

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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
                participants = listOf(userId1, userId2),
                profilePictureUrlThumbnail = userProfile.profilePictureThumbnailUrl ?: "",
                displayName = userProfile.displayName,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                unreadMessageCount = 0
            )

            // Save to remote
            val chatMap = mapper.mapDomainToMap(chat)
            val remoteResult = remoteDataSource.createChat(chatMap)

            if (remoteResult.isFailure) {
                return Result.failure(remoteResult.exceptionOrNull() ?: Exception("Failed to create chat"))
            }

            // Save to local
            localDataSource.insertChat(mapper.mapDomainToEntity(chat))

            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserChats(userId: String): Flow<List<Chat>> {
        return try {
            val remoteChats = remoteDataSource.getUserChats(userId)
                .map { documents ->
                    documents.map { mapper.mapDocumentToDomain(it) }
                }
                .onEach { chats ->
                    // Update local cache
                    localDataSource.insertChats(chats.map { mapper.mapDomainToEntity(it) })
                }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    // On error, emit empty list and continue with local data
                    emit(emptyList())
                }

            // Combine remote and local data, preferring remote when available
            remoteChats.flatMapLatest { remoteResult ->
                if (remoteResult.isEmpty()) {
                    // If remote is empty or failed, use local data
                    localDataSource.getUserChats(userId)
                        .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
                } else {
                    // Otherwise use remote data
                    flow { emit(remoteResult) }
                }
            }
        } catch (e: Exception) {
            flow { emit(emptyList()) }
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