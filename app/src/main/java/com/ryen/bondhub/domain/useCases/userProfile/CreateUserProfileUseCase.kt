package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {

    suspend operator fun invoke(userProfile: UserProfile): Result<Unit> {
        return repository.createUserProfile(userProfile)
    }

}