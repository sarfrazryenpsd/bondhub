package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage>
    suspend fun getMessages(connectionId: String): Flow<List<ChatMessage>>
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int>
    suspend fun markMessagesAsRead(connectionId: String, receiverId: String): Result<Unit>
}