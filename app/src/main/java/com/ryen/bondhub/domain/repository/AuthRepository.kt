package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    fun getCurrentUser(): User?
    fun isUserAuthenticated(): Flow<Boolean>
}