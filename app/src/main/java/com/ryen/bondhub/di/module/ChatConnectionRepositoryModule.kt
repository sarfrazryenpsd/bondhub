package com.ryen.bondhub.di.module

import com.ryen.bondhub.data.repository.ChatConnectionRepositoryImpl
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatConnectionRepositoryModule {

    @Binds
    abstract fun bindChatConnectionRepository(
        chatConnectionRepositoryImpl: ChatConnectionRepositoryImpl
    ): ChatConnectionRepository

}