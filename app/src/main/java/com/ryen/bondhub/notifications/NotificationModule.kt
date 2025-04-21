package com.ryen.bondhub.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideFCMTokenRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): FCMTokenRepository {
        return FCMTokenRepositoryImpl(firestore, authRepository)
    }
}