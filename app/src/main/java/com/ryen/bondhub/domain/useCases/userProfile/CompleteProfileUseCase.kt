package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import javax.inject.Inject

class CompleteProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(userProfile: UserProfile) = repository.completeProfile(userProfile)
}