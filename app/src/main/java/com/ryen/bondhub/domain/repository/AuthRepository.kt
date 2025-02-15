package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.UserAuth
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<UserAuth>
    suspend fun signUpWithDefaultProfile(
        email: String,
        password: String,
        displayName: String
    ): Result<UserAuth>
    fun getCurrentUser(): UserAuth?
    fun isUserAuthenticated(): Flow<Boolean>
}