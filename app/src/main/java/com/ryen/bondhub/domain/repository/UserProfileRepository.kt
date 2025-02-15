package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile>
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit>
    fun observeUserProfile(uid: String): Flow<Result<UserProfile>>
}