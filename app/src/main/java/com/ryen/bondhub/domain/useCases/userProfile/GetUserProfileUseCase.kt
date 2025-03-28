package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.data.repository.UserProfileRepositoryImpl
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(userId: String, forceRefresh: Boolean = false): Result<UserProfile> {
        return if (forceRefresh) {
            (userProfileRepository as UserProfileRepositoryImpl).refreshUserProfile(userId)
        } else {
            userProfileRepository.getUserProfile(userId)
        }
    }

    suspend fun getUserProfileRealTime(userId: String): Flow<Result<UserProfile>> {
        return userProfileRepository.getUserProfileRealTime(userId)
    }

}