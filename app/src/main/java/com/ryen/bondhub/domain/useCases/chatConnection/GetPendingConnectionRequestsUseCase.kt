package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPendingConnectionRequestsUseCase @Inject constructor(
    private val chatConnectionRepository: ChatConnectionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<List<ChatConnection>> {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return flowOf(emptyList())

        return chatConnectionRepository.getPendingConnectionRequestsForUser(
            userId = currentUserId,
            asRecipient = true // Important: we only want requests where the current user is the recipient
        )
    }
}