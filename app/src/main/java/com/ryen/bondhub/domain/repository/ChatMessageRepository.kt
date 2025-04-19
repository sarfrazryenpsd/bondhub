package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage>
    suspend fun getUnreadMessagesCount(baseChatId: String, userId: String): Flow<Int>
    suspend fun listenForNewMessages(baseChatId: String): Flow<List<ChatMessage>>
    suspend fun markAllMessagesAsRead(chatId: String, receiverId: String): Result<Unit>
}