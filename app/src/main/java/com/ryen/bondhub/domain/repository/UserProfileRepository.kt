package com.ryen.bondhub.domain.repository

import android.net.Uri
import com.ryen.bondhub.domain.model.ProfileImageUrls
import com.ryen.bondhub.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun updateProfileImage(userId: String, imageUri: Uri): Result<ProfileImageUrls>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
}