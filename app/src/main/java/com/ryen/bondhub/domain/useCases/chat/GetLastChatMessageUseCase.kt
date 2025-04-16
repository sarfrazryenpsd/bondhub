package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatIdOrBaseChatId: String): Flow<Result<ChatMessage>> {
        // Extract baseChatId if this is a user-specific chatId
        val components = chatIdOrBaseChatId.split("_")
        val baseChatId = if (components.size > 1) components.first() else chatIdOrBaseChatId

        return chatRepository.getLastChatMessage(baseChatId)
    }
}