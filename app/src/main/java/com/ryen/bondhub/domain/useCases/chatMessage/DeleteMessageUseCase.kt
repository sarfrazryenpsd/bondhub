package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return repository.deleteMessage(messageId)
    }
}