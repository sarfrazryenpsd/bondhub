package com.ryen.bondhub.di.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.repository.AuthRepositoryImpl
import com.ryen.bondhub.data.repository.UserProfileRepositoryImpl
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.repository.UserProfileRepository
import com.ryen.bondhub.domain.useCases.auth.GetAuthStateUseCase
import com.ryen.bondhub.domain.useCases.auth.SignInUseCase
import com.ryen.bondhub.domain.useCases.auth.SignUpUseCase
import com.ryen.bondhub.domain.useCases.userProfile.CreateUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository = AuthRepositoryImpl(auth)

    @Provides
    @Singleton
    fun provideSignInUseCase(repository: AuthRepository): SignInUseCase = SignInUseCase(repository)

    @Provides
    @Singleton
    fun provideSignUpUseCase(repository: AuthRepository): SignUpUseCase = SignUpUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAuthStateUseCase(repository: AuthRepository): GetAuthStateUseCase = GetAuthStateUseCase(repository)

    @Provides
    @Singleton
    fun provideUserProfileRepository(firestore: FirebaseFirestore): UserProfileRepository = UserProfileRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideCreateUserProfileUseCase(repository: UserProfileRepository): CreateUserProfileUseCase = CreateUserProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(repository: UserProfileRepository): GetUserProfileUseCase = GetUserProfileUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(repository: UserProfileRepository): UpdateUserProfileUseCase = UpdateUserProfileUseCase(repository)


}