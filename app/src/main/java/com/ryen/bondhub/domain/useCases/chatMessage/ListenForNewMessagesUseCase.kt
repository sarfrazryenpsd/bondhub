package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenForNewMessagesUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    suspend operator fun invoke(chatIdOrBaseChatId: String): Flow<List<ChatMessage>> {
        // Extract baseChatId if this is a user-specific chatId
        val components = chatIdOrBaseChatId.split("_")
        val baseChatId = if (components.size > 1) components.first() else chatIdOrBaseChatId

        return chatMessageRepository.listenForNewMessages(baseChatId)
    }
}