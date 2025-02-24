package com.ryen.bondhub.domain.useCases.userProfile

import android.net.Uri
import com.ryen.bondhub.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateProfileImageUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(userId: String, imageUri: Uri) = repository.updateProfileImage(userId, imageUri)
}