package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import jakarta.inject.Inject

class FindUserByEmailUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(email: String): Result<UserProfile?> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }

        return userProfileRepository.findUserByEmail(email)
    }
}