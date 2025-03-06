package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class UpdateMessageStatusUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(messageId: String, status: MessageStatus): Result<Unit> {
        return repository.updateMessageStatus(messageId, status)
    }
}