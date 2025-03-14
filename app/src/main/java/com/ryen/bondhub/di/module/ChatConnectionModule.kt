package com.ryen.bondhub.di.module

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.remote.dataSource.ChatConnectionRemoteDataSource
import com.ryen.bondhub.data.repository.ChatConnectionRepositoryImpl
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionBetweenUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatConnectionModule {

    @Provides
    @Singleton
    fun provideChatConnectionRemoteDataSource(
        firestore: FirebaseFirestore
    ): ChatConnectionRemoteDataSource {
        return ChatConnectionRemoteDataSource(firestore)
    }


    @Provides
    @Singleton
    fun provideChatConnectionRepository(
        remoteDataSource: ChatConnectionRemoteDataSource,
        chatConnectionDao: ChatConnectionDao
    ): ChatConnectionRepository {
        return ChatConnectionRepositoryImpl(
            remoteDataSource,
            chatConnectionDao,
            dispatcher = Dispatchers.IO
        )
    }

    @Provides
    @Singleton
    fun provideGetConnectionBetweenUsersUseCase(
        repository: ChatConnectionRepository
    ): GetConnectionBetweenUsersUseCase {
        return GetConnectionBetweenUsersUseCase(repository)
    }
}