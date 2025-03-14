package com.ryen.bondhub.domain.useCases.chat

import com.ryen.bondhub.domain.model.Chat
import com.ryen.bondhub.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(userId: String): Flow<List<Chat>> {
        return chatRepository.getUserChats(userId)
    }
}