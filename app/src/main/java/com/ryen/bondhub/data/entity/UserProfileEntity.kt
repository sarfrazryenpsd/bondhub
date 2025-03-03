package com.ryen.bondhub.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ryen.bondhub.domain.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String?,
    val profilePictureThumbnailUrl: String?,
    val bio: String,
    val isProfileSetupComplete: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)


fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        uid = uid,
        email = email,
        displayName = displayName,
        profilePictureUrl = profilePictureUrl,
        profilePictureThumbnailUrl = profilePictureThumbnailUrl,
        bio = bio,
        isProfileSetupComplete = isProfileSetupComplete,
        lastUpdated = System.currentTimeMillis()
    )
}

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        email = email,
        displayName = displayName,
        profilePictureUrl = profilePictureUrl,
        profilePictureThumbnailUrl = profilePictureThumbnailUrl,
        bio = bio,
        isProfileSetupComplete = isProfileSetupComplete
    )
}