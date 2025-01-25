package com.ryen.bondhub.domain.repository

import com.ryen.bondhub.domain.model.User

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
}