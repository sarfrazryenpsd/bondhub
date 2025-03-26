package com.ryen.bondhub.data.repository

import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.data.AppDatabase
import com.ryen.bondhub.data.local.dao.ChatConnectionDao
import com.ryen.bondhub.data.local.dao.UserProfileDao
import com.ryen.bondhub.domain.model.UserAuth
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProfileDao: UserProfileDao,
    private val chatConnectionDao: ChatConnectionDao,
    private val database: AppDatabase
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<UserAuth> =
        withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    Result.success(firebaseUser.toUserAuth())
                } ?: Result.failure(Exception("Authentication failed"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signUpWithDefaultProfile(
        email: String,
        password: String,
        displayName: String
    ): Result<UserAuth> = withContext(Dispatchers.IO) {
        try {
            // 1. Create auth user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User creation failed")

            // 2. Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Create default profile in Firestore
            val defaultProfile = UserProfile(
                uid = firebaseUser.uid,
                displayName = displayName.ifEmpty { email.substringBefore('@') },
                email = email
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(defaultProfile)
                .await()

            Result.success(firebaseUser.toUserAuth())
        } catch (e: Exception) {
            // If anything fails, attempt to delete the user and return failure
            auth.currentUser?.delete()
            Result.failure(e)
        }
    }

    override suspend fun isProfileSetupComplete(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: return@withContext false
            val snapshot = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            return@withContext snapshot.exists() &&
                    snapshot.getBoolean("isProfileSetupComplete") == true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Start a database transaction
            database.withTransaction {
                try {
                    // Attempt Firebase sign-out first
                    auth.signOut()

                    // Clear local database caches
                    userProfileDao.clearAllProfiles()
                    chatConnectionDao.clearAllConnections()
                } catch (e: Exception) {
                    // If any step fails, rollback the transaction
                    throw e
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Return failure with the specific exception
            Result.failure(e)
        }
    }


    override suspend fun getCurrentUser(): UserAuth? {
        return auth.currentUser?.toUserAuth()
    }


    private fun FirebaseUser.toUserAuth() = UserAuth(
        uid = uid,
        email = email ?: "",
        displayName = displayName
    )
}