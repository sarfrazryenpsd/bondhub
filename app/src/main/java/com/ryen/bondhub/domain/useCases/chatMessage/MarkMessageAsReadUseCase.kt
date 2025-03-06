package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(connectionId: String, receiverId: String): Result<Unit> {
        return repository.markMessagesAsRead(connectionId, receiverId)
    }
}