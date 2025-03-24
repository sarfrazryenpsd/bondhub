package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetAcceptedConnectionsUseCase @Inject constructor(
    private val repository: ChatConnectionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<List<ChatConnection>> {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return flowOf(emptyList())
        return repository.getAcceptedConnectionsFlow(currentUserId)
    }
}