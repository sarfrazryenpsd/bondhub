package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.UserAuth

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<UserAuth>
    suspend fun signUpWithDefaultProfile(
        email: String,
        password: String,
        displayName: String
    ): Result<UserAuth>
    suspend fun isProfileSetupComplete(): Boolean

    suspend fun getCurrentUser(): UserAuth?
}