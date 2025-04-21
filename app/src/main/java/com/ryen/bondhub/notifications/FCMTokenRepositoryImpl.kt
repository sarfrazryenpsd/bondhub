package com.ryen.bondhub.notifications

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FCMTokenRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
): FCMTokenRepository {


    override suspend fun updateUserFCMToken(token: String): Result<Unit> {
        return try {
            val currentUser = authRepository.getCurrentUser() ?: return Result.failure(Exception("User not authenticated"))

            firestore.collection("users")
                .document(currentUser.uid)
                .update("fcmToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FCMTokenRepository", "Error updating FCM token", e)
            Result.failure(e)
        }
    }


    override suspend fun getUserFCMToken(userId: String): Result<String?> = try{
        val snapshot = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        val token = snapshot.getString("fcmToken")
        Result.success(token)
    } catch (e: Exception) {
        Log.e("FCMTokenRepository", "Error getting FCM token", e)
        Result.failure(e)
    }

}