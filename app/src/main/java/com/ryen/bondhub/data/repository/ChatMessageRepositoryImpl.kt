package com.ryen.bondhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.data.local.dao.ChatDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.local.entity.toDomain
import com.ryen.bondhub.data.local.entity.toEntity
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
                .map { messageList ->
                    val messages = messageList.map { mapper.mapRemoteToDomain(it) }

                    // Update local cache
                    localDataSource.insertMessages(messages.map { mapper.mapDomainToEntity(it) })

                    messages
                }
                .flowOn(Dispatchers.IO)
                .catch {
                    // Fallback to local data source on error
                    emit(emptyList())
                    localDataSource.getChatMessages(chatId)
                        .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
                }
                .flatMapConcat { remoteResult ->
                    if (remoteResult.isEmpty()) {
                        localDataSource.getChatMessages(chatId)
                            .map { entities -> entities.map { mapper.mapEntityToDomain(it) } }
                    } else {
                        flow { emit(remoteResult) }
                    }
                }
        } catch (e: Exception) {
            flow { emit(emptyList()) }
        }
    }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: MessageStatus
    ): Result<Unit> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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