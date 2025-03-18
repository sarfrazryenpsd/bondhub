package com.ryen.bondhub.di.module

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ryen.bondhub.data.local.dao.UserProfileDao
import com.ryen.bondhub.data.repository.AuthRepositoryImpl
import com.ryen.bondhub.data.repository.UserProfileRepositoryImpl
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.UserProfileRepository
import com.ryen.bondhub.domain.useCases.auth.SignInUseCase
import com.ryen.bondhub.domain.useCases.auth.SignUpUseCase
import com.ryen.bondhub.domain.useCases.userProfile.CompleteProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.FindUserByEmailUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()


    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository = AuthRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun provideSignInUseCase(repository: AuthRepository): SignInUseCase = SignInUseCase(repository)

    @Provides
    @Singleton
    fun provideSignUpUseCase(repository: AuthRepository): SignUpUseCase = SignUpUseCase(repository)


    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(repository: UserProfileRepository): UpdateUserProfileUseCase = UpdateUserProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateProfileImageUseCase(repository: UserProfileRepository): UpdateProfileImageUseCase = UpdateProfileImageUseCase(repository)

    @Provides
    @Singleton
    fun provideGetProfileImageUseCase(repository: UserProfileRepository): GetUserProfileUseCase = GetUserProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideCompleteProfileUseCase(repository: UserProfileRepository): CompleteProfileUseCase = CompleteProfileUseCase(repository)

    @Provides
    fun provideFindUserByEmailUseCase(
        userProfileRepository: UserProfileRepository
    ): FindUserByEmailUseCase {
        return FindUserByEmailUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideUserProfileRepositoryImpl(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        @ApplicationContext context: Context,
        userProfileDao: UserProfileDao
    ): UserProfileRepositoryImpl {
        return UserProfileRepositoryImpl(
            firestore,
            storage,
            context,
            userProfileDao
        )
    }

}