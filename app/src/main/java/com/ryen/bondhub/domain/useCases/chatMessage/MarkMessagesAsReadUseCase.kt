package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    suspend operator fun invoke(chatId: String, receiverId: String): Result<Unit> {
        return chatMessageRepository.markAllMessagesAsRead(chatId, receiverId)
    }
}