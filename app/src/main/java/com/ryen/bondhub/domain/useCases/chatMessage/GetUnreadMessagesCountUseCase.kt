package com.ryen.bondhub.domain.useCases.chatMessage

import com.ryen.bondhub.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadMessagesCountUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend operator fun invoke(connectionId: String, userId: String): Flow<Int> {
        return repository.getUnreadMessagesCount(connectionId, userId)
    }
}