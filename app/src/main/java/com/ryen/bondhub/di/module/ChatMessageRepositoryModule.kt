@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ryen.bondhub.di.module

import com.ryen.bondhub.data.repository.ChatMessageRepositoryImpl
import com.ryen.bondhub.domain.repository.ChatMessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatMessageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatMessageRepository(
        chatMessageRepositoryImpl: ChatMessageRepositoryImpl
    ): ChatMessageRepository
}