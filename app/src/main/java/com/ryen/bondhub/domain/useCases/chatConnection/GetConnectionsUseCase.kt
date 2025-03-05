package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import javax.inject.Inject

class GetConnectionsUseCase @Inject constructor(
    private val repository: ChatConnectionRepository
) {
    operator fun invoke(userId: String) = repository.getConnectionsForUser(userId)
}