package com.ryen.bondhub.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ryen.bondhub.domain.model.ProfileImageUrls
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import com.ryen.bondhub.util.ImageProcessingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    @ApplicationContext private val context: Context

): UserProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val profilePicsRef = storage.reference.child("profile_pictures")
    private val thumbnailsRef = storage.reference.child("profile_pictures_thumbnails")

    override suspend fun updateProfileImage(userId: String, imageUri: Uri): Result<ProfileImageUrls> = withContext(
        Dispatchers.IO) {
        try {
            val processedImages = ImageProcessingUtils.processProfileImage(context, imageUri)
                .getOrThrow()

            // Upload main image and thumbnail concurrently
            val uploads = coroutineScope {
                val mainUpload = async {
                    profilePicsRef.child("$userId.jpg")
                        .putBytes(processedImages.mainImage)
                        .await()
                }
                val thumbUpload = async {
                    thumbnailsRef.child("$userId.jpg")
                        .putBytes(processedImages.thumbnail)
                        .await()
                }
                mainUpload.await() to thumbUpload.await()
            }

            // Get download URLs
            val mainUrl = uploads.first.storage.downloadUrl.await().toString()
            val thumbUrl = uploads.second.storage.downloadUrl.await().toString()

            Result.success(ProfileImageUrls(mainUrl, thumbUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    override suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = try {
        val update = mapOf(
            "displayName" to userProfile.displayName,
            "bio" to userProfile.bio,
        )
        usersCollection.document(userProfile.uid)
            .update(update)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun completeProfile(userProfile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!userProfile.isProfileSetupComplete) {
                val update = mapOf("isProfileSetupComplete" to true)
                usersCollection.document(userProfile.uid)
                    .update(update)
                    .await()
            }
            Result.success(Unit) // Ensure a Result is always returned
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO){
        try {
            val snapshot = usersCollection.document(userId).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            if (userProfile != null) {
                Result.success(userProfile)
            }
            else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}