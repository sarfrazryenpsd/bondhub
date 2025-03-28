package com.ryen.bondhub.domain.repository

import android.net.Uri
import com.ryen.bondhub.domain.model.ProfileImageUrls
import com.ryen.bondhub.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun updateProfileImage(userId: String, imageUri: Uri): Result<ProfileImageUrls>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun completeProfile(userProfile: UserProfile): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun getUserProfileRealTime(userId: String): Flow<Result<UserProfile>>
    suspend fun refreshUserProfile(userId: String): Result<UserProfile>
    suspend fun findUserByEmail(email: String): Result<UserProfile?>
}