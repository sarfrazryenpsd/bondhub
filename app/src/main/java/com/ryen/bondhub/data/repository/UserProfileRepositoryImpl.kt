package com.ryen.bondhub.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ryen.bondhub.data.local.dao.UserProfileDao
import com.ryen.bondhub.data.local.entity.toDomain
import com.ryen.bondhub.data.local.entity.toEntity
import com.ryen.bondhub.domain.model.ProfileImageUrls
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.UserProfileRepository
import com.ryen.bondhub.util.ImageProcessingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao
): UserProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val profilePicsRef = storage.reference.child("profile_pictures")
    private val thumbnailsRef = storage.reference.child("profile_pictures_thumbnails")

    private val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L

    override suspend fun updateProfileImage(userId: String, imageUri: Uri): Result<ProfileImageUrls> =
        withContext(Dispatchers.IO) {
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


    override suspend fun findUserByEmail(email: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            // Search for user with the given email
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            val userProfile = querySnapshot.documents.firstOrNull()?.toObject(UserProfile::class.java)

            // Cache the profile if found
            if (userProfile != null) {
                userProfileDao.insertUserProfile(userProfile.toEntity())
            }

            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> = try {
        val update = mapOf(
            "displayName" to userProfile.displayName,
            "bio" to userProfile.bio,
            "profilePictureUrl" to userProfile.profilePictureUrl,
            "profilePictureThumbnailUrl" to userProfile.profilePictureThumbnailUrl,
        )
        usersCollection.document(userProfile.uid)
            .update(update)
            .await()
        val updatedUserProfile = userProfile.copy(
            displayName = userProfile.displayName,
            bio = userProfile.bio,
            profilePictureUrl = userProfile.profilePictureUrl,
            profilePictureThumbnailUrl = userProfile.profilePictureThumbnailUrl,
        )
        userProfileDao.insertUserProfile(updatedUserProfile.toEntity())
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

    override suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            // Check for fresh cached data first
            val timestamp = System.currentTimeMillis() - CACHE_EXPIRY_MS
            val cachedUserProfile = userProfileDao.getUserProfileIfFresh(userId, timestamp)

            if (cachedUserProfile != null) {
                // Return cached data if fresh
                return@withContext Result.success(cachedUserProfile.toDomain())
            }

            // If no fresh cache, fetch from network
            val documentSnapshot = usersCollection.document(userId).get().await()
            val userProfile = documentSnapshot.toObject(UserProfile::class.java)
                ?: return@withContext Result.failure(Exception("User profile not found"))

            // Cache the fetched profile
            userProfileDao.insertUserProfile(userProfile.toEntity())

            Result.success(userProfile)
        } catch (e: Exception) {
            // If network fetch fails, try to get any cached data regardless of freshness
            val cachedUserProfile = userProfileDao.getUserProfileById(userId)
            if (cachedUserProfile != null) {
                // Return cached data with stale warning
                val profile = cachedUserProfile.toDomain()
                Result.success(profile)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserProfileRealTime(userId: String): Flow<Result<UserProfile>> = callbackFlow {
        val listenerRegistration = usersCollection
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val userProfile = snapshot.toObject(UserProfile::class.java)
                    userProfile?.let {
                        trySend(Result.success(it))
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun refreshUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val userProfile = documentSnapshot.toObject(UserProfile::class.java)
                ?: return@withContext Result.failure(Exception("User profile not found"))

            // Update cache
            userProfileDao.insertUserProfile(userProfile.toEntity())

            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}