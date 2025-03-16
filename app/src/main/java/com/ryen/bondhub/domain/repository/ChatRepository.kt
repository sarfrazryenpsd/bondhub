package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createChat(userId1: String, userId2: String): Result<Chat>
    suspend fun getUserChats(userId: String): Flow<List<Chat>>
    suspend fun deleteChat(chatId: String): Result<Unit>
}