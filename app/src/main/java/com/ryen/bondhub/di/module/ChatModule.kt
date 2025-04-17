package com.ryen.bondhub.di.module

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.mappers.ChatMapper
import com.ryen.bondhub.data.remote.dataSource.ChatRemoteDataSource
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatRepository
import com.ryen.bondhub.domain.useCases.chat.CheckChatExistsByBaseChatIdUseCase
import com.ryen.bondhub.domain.useCases.chat.CreateChatInFirestore
import com.ryen.bondhub.domain.useCases.chat.CreateChatUseCase
import com.ryen.bondhub.domain.useCases.chat.DeleteChatUseCase
import com.ryen.bondhub.domain.useCases.chat.GetUserChatsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule{
    @Provides
    @Singleton
    fun provideChatRemoteDataSource(
        firestore: FirebaseFirestore
    ): ChatRemoteDataSource {
        return ChatRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideChatMapper(): ChatMapper {
        return ChatMapper()
    }

    @Provides
    @Singleton
    fun provideCreateChatUseCase(chatRepository: ChatRepository): CreateChatUseCase{
        return CreateChatUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideGetChatUseCase(chatRepository: ChatRepository, authRepository: AuthRepository): GetUserChatsUseCase{
        return GetUserChatsUseCase(chatRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteChatUseCase(chatRepository: ChatRepository): DeleteChatUseCase{
        return DeleteChatUseCase(chatRepository)
    }


    @Provides
    @Singleton
    fun provideCheckChatExistsByBaseChatIdUseCase(chatRepository: ChatRepository): CheckChatExistsByBaseChatIdUseCase {
        return CheckChatExistsByBaseChatIdUseCase(chatRepository)
    }

    @Provides
    @Singleton
    fun provideCreateChatInFirestore(chatRepository: ChatRepository): CreateChatInFirestore {
        return CreateChatInFirestore(chatRepository)
    }


}