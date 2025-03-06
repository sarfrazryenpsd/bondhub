package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(
        connectionId: String,
        senderId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ): Result<ChatMessage> {
        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            connectionId = connectionId,
            senderId = senderId,
            content = content,
            timestamp = System.currentTimeMillis(),
            messageType = messageType,
            status = MessageStatus.SENT
        )

        return repository.sendMessage(message)
    }
}