package com.ryen.bondhub.domain.useCases.userProfile

import com.ryen.bondhub.domain.repository.UserProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(uid: String) = repository.getUserProfile(uid)
}