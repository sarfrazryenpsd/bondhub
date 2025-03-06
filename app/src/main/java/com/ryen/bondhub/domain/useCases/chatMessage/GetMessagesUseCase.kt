package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(connectionId: String): Flow<List<ChatMessage>> {
        return repository.getMessages(connectionId)
    }
}