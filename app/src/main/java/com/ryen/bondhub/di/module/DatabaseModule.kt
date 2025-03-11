package com.ryen.bondhub.di.module

import android.content.Context
import androidx.room.Room
import com.ryen.bondhub.data.AppDatabase
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.local.dao.ChatMessageDao
import com.ryen.bondhub.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            AppDatabase::class.java,
            "chat_app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserProfileDao(appDatabase: AppDatabase): UserProfileDao {
        return appDatabase.userProfileDao()
    }

    @Provides
    fun provideChatConnectionDao(appDatabase: AppDatabase): ChatConnectionDao {
        return appDatabase.chatConnectionDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: AppDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}