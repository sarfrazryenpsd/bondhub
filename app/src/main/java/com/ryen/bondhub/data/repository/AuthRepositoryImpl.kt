package com.ryen.bondhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ryen.bondhub.domain.model.User
import com.ryen.bondhub.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
): AuthRepository {

    override fun isUserAuthenticated(): Flow<Boolean> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener{ auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO){
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    Result.success(firebaseUser.toUser())
                } ?: Result.failure(Exception("Authentication failed"))
            } catch (e: Exception){
                Result.failure(e)
            }
        }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User> = withContext(Dispatchers.IO){
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()
                Result.success(firebaseUser.toUser())
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception){
            Result.failure(e)
        }
    }


    override fun getCurrentUser(): User? =
        auth.currentUser?.toUser()



    private fun FirebaseUser.toUser() = User(
        uid = uid,
        email = email ?: "",
        displayName = displayName
    )

}