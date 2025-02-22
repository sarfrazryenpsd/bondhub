package com.ryen.bondhub.domain.repository

import android.net.Uri
import com.ryen.bondhub.domain.model.ProfilePicUrls
import com.ryen.bondhub.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<ProfilePicUrls>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
}