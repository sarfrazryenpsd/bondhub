package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.repository.ChatRepository

class CheckChatExistRemotelyUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(chatId: String): Result<Boolean> {
        return chatRepository.checkChatExistsRemotely(chatId)
    }

}