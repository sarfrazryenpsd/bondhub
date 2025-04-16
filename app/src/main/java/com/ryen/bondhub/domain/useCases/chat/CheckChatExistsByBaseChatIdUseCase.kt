package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.repository.ChatRepository

class CheckChatExistsByBaseChatIdUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(baseChatId: String): Result<Boolean> {
        return chatRepository.checkChatExistsByBaseChatId(baseChatId)
    }

}