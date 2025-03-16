package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<List<Chat>> {
        val currentUserId = authRepository.getCurrentUser()?.uid
            ?: return flow { emit(emptyList()) }

        return chatRepository.getUserChats(currentUserId)
    }
}