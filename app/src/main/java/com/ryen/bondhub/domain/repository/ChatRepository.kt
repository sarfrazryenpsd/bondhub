package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createChat(userId1: String, userId2: String): Result<Chat>
    suspend fun getUserChats(userId: String): Flow<List<Chat>>
    suspend fun deleteChat(chatId: String): Result<Unit>
    suspend fun checkChatExistsRemotely(chatId: String): Result<Boolean>
    suspend fun getLastChatMessage(chatId: String): Flow<Result<ChatMessage>>
    suspend fun createChatInFirestore(chatId: String, userId1: String, userId2: String): Result<Unit>
}