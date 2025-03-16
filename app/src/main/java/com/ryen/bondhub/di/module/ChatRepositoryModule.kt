@file:OptIn(ExperimentalCoroutinesApi::class)

package com.ryen.bondhub.di.module

import com.ryen.bondhub.data.repository.ChatRepositoryImpl
import com.ryen.bondhub.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatRepositoryModule {

    @Binds
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

}