package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class DeleteChatMessageUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return chatMessageRepository.deleteChatMessage(messageId)
    }
}