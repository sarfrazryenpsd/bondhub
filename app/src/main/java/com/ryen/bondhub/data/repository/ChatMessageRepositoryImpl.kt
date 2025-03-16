package com.ryen.bondhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatMessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatMessageRemoteDataSource,
    private val localDataSource: ChatMessageDao,
    private val chatDao: ChatDao,
    private val mapper: ChatMessageMapper,
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

            // Update chat with last message info
            chatDao.updateChatWithNewMessage(
                finalMessage.chatId,
                finalMessage.content,
                finalMessage.timestamp
            )

            // Send to remote
            val remoteMessage = mapper.mapDomainToRemote(finalMessage)

            remoteDataSource.sendMessage(remoteMessage).fold(
                onSuccess = {
                    // Update status to SENT
                    val sentMessage = finalMessage.copy(status = MessageStatus.SENT)
                    localDataSource.updateMessageStatus(sentMessage.messageId, MessageStatus.SENT.name)
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

    override suspend fun deleteChatMessage(messageId: String): Result<Unit> {
        return try {
            remoteDataSource.deleteMessage(messageId).fold(
                onSuccess = {
                    // Also delete from local database
                    localDataSource.deleteMessage(messageId)
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
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

}