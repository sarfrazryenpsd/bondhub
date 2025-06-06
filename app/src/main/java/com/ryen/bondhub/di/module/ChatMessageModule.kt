package com.ryen.bondhub.di.module

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.mappers.ChatMessageMapper
import com.ryen.bondhub.data.remote.dataSource.ChatMessageRemoteDataSource
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import com.ryen.bondhub.domain.useCases.chatMessage.GetUnreadMessagesCountUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.ListenForNewMessagesUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.MarkMessagesAsReadUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.SendMessageUseCase
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
    fun provideGetUnreadMessagesCountUseCase(repository: ChatMessageRepository): GetUnreadMessagesCountUseCase {
        return GetUnreadMessagesCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideListenForNewMessagesUseCase(repository: ChatMessageRepository): ListenForNewMessagesUseCase {
        return ListenForNewMessagesUseCase(repository)
    }

    @Provides
    fun provideMarkMessagesAsReadUseCase(repository: ChatMessageRepository): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideChatMessageMapper(): ChatMessageMapper {
        return ChatMessageMapper()
    }
}