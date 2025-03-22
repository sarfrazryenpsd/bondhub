package com.ryen.bondhub.di.module

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.remote.dataSource.ChatConnectionRemoteDataSource
import com.ryen.bondhub.data.repository.ChatConnectionRepositoryImpl
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import com.ryen.bondhub.domain.useCases.chatConnection.AcceptConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.FindExistingConnectionUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionBetweenUsersUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionStatusUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetPendingConnectionRequestsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.RejectConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.SendConnectionRequestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
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
        chatConnectionDao: ChatConnectionDao,
        firestore: FirebaseFirestore
    ): ChatConnectionRepository {
        return ChatConnectionRepositoryImpl(
            remoteDataSource,
            firestore,
            chatConnectionDao,
            dispatcher = Dispatchers.IO
        )
    }

    @Provides
    @Singleton
    fun provideAcceptConnectionRequestUseCase(
        repository: ChatConnectionRepository
    ): AcceptConnectionRequestUseCase {
        return AcceptConnectionRequestUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRejectConnectionRequestUseCase(
        repository: ChatConnectionRepository
    ): RejectConnectionRequestUseCase {
        return RejectConnectionRequestUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetConnectionsUseCase(
        repository: ChatConnectionRepository
    ): GetConnectionsUseCase {
        return GetConnectionsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFindExistingConnectionUseCase(
        repository: ChatConnectionRepository
    ): FindExistingConnectionUseCase {
        return FindExistingConnectionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetConnectionBetweenUsersUseCase(
        repository: ChatConnectionRepository
    ): GetConnectionBetweenUsersUseCase {
        return GetConnectionBetweenUsersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetConnectionStatusUseCase(
        repository: ChatConnectionRepository
    ): GetConnectionStatusUseCase {
        return GetConnectionStatusUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPendingConnectionRequestsUseCase(
        chatConnectionRepository: ChatConnectionRepository,
        authRepository: AuthRepository
    ): GetPendingConnectionRequestsUseCase {
        return GetPendingConnectionRequestsUseCase(chatConnectionRepository, authRepository)
    }

    @Provides
    fun provideSendConnectionRequestUseCase(
        chatConnectionRepository: ChatConnectionRepository,
    ): SendConnectionRequestUseCase {
        return SendConnectionRequestUseCase(chatConnectionRepository)
    }

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
