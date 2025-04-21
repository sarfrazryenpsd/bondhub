package com.ryen.bondhub.notifications

interface FCMTokenRepository {
    suspend fun updateUserFCMToken(token: String): Result<Unit>
    suspend fun getUserFCMToken(userId: String): Result<String?>
}