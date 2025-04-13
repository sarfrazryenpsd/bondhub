package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListenForNewMessagesUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    suspend operator fun invoke(chatId: String): Flow<ChatMessage> {
        return chatMessageRepository.listenForNewMessages(chatId)
    }
}