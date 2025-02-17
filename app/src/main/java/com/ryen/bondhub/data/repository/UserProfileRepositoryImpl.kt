package com.ryen.bondhub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
): UserProfileRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getUserProfile(uid: String): Result<UserProfile> =
        try {
            val snapshot = usersCollection.document(uid).get().await()
            if(snapshot.exists()){
                Result.success(snapshot.toObject(UserProfile::class.java)!!)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception){
            Result.failure(e)
        }

    override suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> =
        try {
            usersCollection.document(updates["uid"] as String)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun observeUserProfile(uid: String): Flow<Result<UserProfile>> = callbackFlow {
        val subscription = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    close(error)
                    return@addSnapshotListener
                }

                if(snapshot != null && snapshot.exists()){
                    snapshot.toObject(UserProfile::class.java)?.let {
                        trySend(Result.success(it))
                    }
                }
            }
        awaitClose { subscription.remove()  }
    }

}