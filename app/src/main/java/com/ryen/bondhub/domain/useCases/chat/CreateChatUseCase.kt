package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.repository.ChatRepository
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId1: String, userId2: String): Result<Chat> {
        return chatRepository.createChat(userId1, userId2)
    }
}