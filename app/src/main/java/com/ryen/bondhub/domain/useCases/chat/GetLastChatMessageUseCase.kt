package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String): Flow<Result<ChatMessage>> {
        return chatRepository.getLastChatMessage(chatId)
    }
}