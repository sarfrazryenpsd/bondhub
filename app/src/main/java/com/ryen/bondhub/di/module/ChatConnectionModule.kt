package com.ryen.bondhub.di.module

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.AppDatabase
import com.ryen.bondhub.data.dao.ChatConnectionDao
import com.ryen.bondhub.data.remote.dataSource.ChatConnectionRemoteDataSource
import com.ryen.bondhub.data.repository.ChatConnectionRepositoryImpl
import com.ryen.bondhub.domain.repository.ChatConnectionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideChatConnectionDao(appDatabase: AppDatabase): ChatConnectionDao {
        return appDatabase.chatConnectionDao()
    }

    @Provides
    @Singleton
    fun provideChatConnectionRepository(
        remoteDataSource: ChatConnectionRemoteDataSource,
        chatConnectionDao: ChatConnectionDao
    ): ChatConnectionRepository {
        return ChatConnectionRepositoryImpl(
            remoteDataSource,
            chatConnectionDao
        )
    }

    // Extend the existing AppDatabase to include ChatConnection DAO
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "chat_app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}