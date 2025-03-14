package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import com.ryen.bondhub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatConnectionRepository: ChatConnectionRepository
) {
    suspend operator fun invoke(userId1: String, userId2: String): Result<Chat> {
        try {
            // Collect the first value from the Flow
            val connection = chatConnectionRepository.getConnectionBetweenUsers(userId1, userId2)
                .first() // This will give us the ChatConnection? object (nullable)

            // Check if connection exists and is accepted
            return if (connection != null && connection.status == ConnectionStatus.ACCEPTED) {
                chatRepository.createChat(userId1, userId2)
            } else {
                Result.failure(Exception("Cannot create chat: No accepted connection exists between users"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}