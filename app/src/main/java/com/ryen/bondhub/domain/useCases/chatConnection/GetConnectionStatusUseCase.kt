package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import javax.inject.Inject

class GetConnectionStatusUseCase @Inject constructor(
    private val connectionRepository: ChatConnectionRepository
) {
    suspend operator fun invoke(currentUserId: String, otherUserId: String): Result<ConnectionStatus?> {
        return connectionRepository.getConnectionStatus(currentUserId, otherUserId)
    }
}