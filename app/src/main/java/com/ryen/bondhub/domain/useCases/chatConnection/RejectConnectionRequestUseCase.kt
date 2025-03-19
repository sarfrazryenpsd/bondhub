package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import javax.inject.Inject

class RejectConnectionRequestUseCase @Inject constructor(
    private val repository: ChatConnectionRepository
) {
    suspend operator fun invoke(connectionId: String): Result<Unit> {
        return repository.rejectConnectionRequest(connectionId)
    }
}