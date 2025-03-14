package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConnectionBetweenUsersUseCase @Inject constructor(
    private val repository: ChatConnectionRepository
) {
    suspend operator fun invoke(user1Id: String, user2Id: String): Flow<ChatConnection?> {
        return repository.getConnectionBetweenUsers(user1Id, user2Id)
    }
}