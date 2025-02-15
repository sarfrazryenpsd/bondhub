package com.ryen.bondhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.domain.model.UserAuth
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                displayName = displayName,
                email = email
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(defaultProfile)
                .await()

            Result.success(firebaseUser.toUserAuth())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): UserAuth? =
        auth.currentUser?.toUserAuth()

    override fun isUserAuthenticated(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun FirebaseUser.toUserAuth() = UserAuth(
        uid = uid,
        email = email ?: "",
        displayName = displayName
    )
}