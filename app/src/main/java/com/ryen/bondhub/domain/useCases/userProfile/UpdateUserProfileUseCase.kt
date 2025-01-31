package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(updates: Map<String, Any>) = repository.updateUserProfile(updates)
}