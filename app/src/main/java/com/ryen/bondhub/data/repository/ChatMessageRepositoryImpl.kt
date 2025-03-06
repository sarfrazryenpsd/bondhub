package com.ryen.bondhub.data.repository

import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.local.entity.toDomain
import com.ryen.bondhub.data.local.entity.toEntity
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ChatMessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatMessageRemoteDataSource,
    private val localDataSource: ChatMessageDao
) : ChatMessageRepository {

    override suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            // First send to remote
            val remoteResult = remoteDataSource.sendMessage(message)

            // If successful, cache locally
            if (remoteResult.isSuccess) {
                localDataSource.insertMessage(message.toEntity())
            }

            remoteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessages(connectionId: String): Flow<List<ChatMessage>> {
        return localDataSource.getMessagesByConnectionId(connectionId)
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                // Fetch latest messages from remote and cache them
                try {
                    remoteDataSource.getMessages(connectionId)
                        .first()
                        .let { messages ->
                            localDataSource.insertMessages(messages.map { it.toEntity() })
                        }
                } catch (e: Exception) {
                    // If remote fetch fails, we'll still emit cached messages
                }
            }
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit> {
        return try {
            // Update remote first
            val remoteResult = remoteDataSource.updateMessageStatus(messageId, status)

            // If successful, update local cache
            if (remoteResult.isSuccess) {
                localDataSource.updateMessageStatus(messageId, status.name)
            }

            remoteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            // Delete from remote first
            val remoteResult = remoteDataSource.deleteMessage(messageId)

            // If successful, delete from local cache
            if (remoteResult.isSuccess) {
                localDataSource.deleteMessage(messageId)
            }

            remoteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int> {
        return localDataSource.getUnreadMessagesCount(connectionId, userId)
    }

    override suspend fun markMessagesAsRead(connectionId: String, receiverId: String): Result<Unit> {
        return try {
            // We'll need to get all unread messages and update them one by one in Firestore
            // For simplicity, let's assume we have a batch update function in remote data source
            // First update local
            localDataSource.markMessagesAsRead(connectionId, receiverId)

            // Then attempt to update remote (this is simplified - you'd need to implement the remote part)
            // This would require fetching unread messages and updating them one by one or in a batch
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}