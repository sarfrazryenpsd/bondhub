package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    suspend operator fun invoke(message: ChatMessage): Result<ChatMessage> {
        return chatMessageRepository.sendMessage(message)
    }
}