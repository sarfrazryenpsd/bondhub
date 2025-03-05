package com.ryen.bondhub.domain.useCases.chatConnection

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import javax.inject.Inject

class FindExistingConnectionUseCase @Inject constructor(
    private val repository: ChatConnectionRepository
) {
    suspend operator fun invoke(
        user1Id: String,
        user2Id: String
    ): ChatConnection? {
        return repository.findExistingConnection(user1Id, user2Id)
    }
}