package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPendingConnectionRequestsUseCase @Inject constructor(
    private val chatConnectionRepository: ChatConnectionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<List<ChatConnection>> {
        val currentUser = authRepository.getCurrentUser() ?: throw Exception("User not authenticated")

        return chatConnectionRepository.getConnectionsForUser(currentUser.uid)
            .map { connections ->
                // Filter for connections where the current user is the receiver (user2Id)
                // and the status is still PENDING
                connections.filter { connection ->
                    connection.initiatorId != currentUser.uid &&
                    connection.user2Id == currentUser.uid &&
                    connection.status == ConnectionStatus.PENDING
                }
            }
    }
}