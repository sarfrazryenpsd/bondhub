package com.ryen.bondhub.di.module

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import com.ryen.bondhub.domain.useCases.chatMessage.DeleteMessageUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.GetMessagesUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.GetUnreadMessagesCountUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.MarkMessagesAsReadUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.SendMessageUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.UpdateMessageStatusUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatMessageModule {

    @Provides
    @Singleton
    fun provideChatMessageRemoteDataSource(
        firestore: FirebaseFirestore
    ): ChatMessageRemoteDataSource {
        return ChatMessageRemoteDataSource(firestore)
    }

    @Provides
    fun provideSendMessageUseCase(repository: ChatMessageRepository): SendMessageUseCase {
        return SendMessageUseCase(repository)
    }

    @Provides
    fun provideGetMessagesUseCase(repository: ChatMessageRepository): GetMessagesUseCase {
        return GetMessagesUseCase(repository)
    }

    @Provides
    fun provideUpdateMessageStatusUseCase(repository: ChatMessageRepository): UpdateMessageStatusUseCase {
        return UpdateMessageStatusUseCase(repository)
    }

    @Provides
    fun provideDeleteMessageUseCase(repository: ChatMessageRepository): DeleteMessageUseCase {
        return DeleteMessageUseCase(repository)
    }

    @Provides
    fun provideGetUnreadMessagesCountUseCase(repository: ChatMessageRepository): GetUnreadMessagesCountUseCase {
        return GetUnreadMessagesCountUseCase(repository)
    }

    @Provides
    fun provideMarkMessagesAsReadUseCase(repository: ChatMessageRepository): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(repository)
    }
}