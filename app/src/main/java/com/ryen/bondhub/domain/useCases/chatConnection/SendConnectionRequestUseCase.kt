package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import javax.inject.Inject

class SendConnectionRequestUseCase @Inject constructor(
    private val repository: ChatConnectionRepository
) {
    suspend operator fun invoke(
        currentUserId: String,
        targetUserId: String
    ): Result<ChatConnection> {
        return repository.sendConnectionRequest(currentUserId, targetUserId)
    }
}