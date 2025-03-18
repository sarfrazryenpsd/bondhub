package com.ryen.bondhub.domain.useCases.userProfile

import android.util.Patterns
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

        // Basic email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        return userProfileRepository.findUserByEmail(email)
    }
}