package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage>
    suspend fun getChatMessages(chatId: String): Flow<List<ChatMessage>>
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit>
    suspend fun deleteChatMessage(messageId: String): Result<Unit>
    suspend fun getUnreadMessagesCount(connectionId: String, userId: String): Flow<Int>
    suspend fun markAllMessagesAsRead(chatId: String, receiverId: String): Result<Unit>
}