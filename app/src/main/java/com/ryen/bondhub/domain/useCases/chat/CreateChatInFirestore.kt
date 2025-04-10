package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.repository.ChatRepository

class CreateChatInFirestore(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(chatId: String, userId1: String, userId2: String): Result<Unit> {
        return chatRepository.createChatInFirestore(chatId, userId1, userId2)
    }
}