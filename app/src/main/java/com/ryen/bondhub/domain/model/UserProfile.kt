package com.ryen.bondhub.domain.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val profilePictureThumbnailUrl: String? = null,
    val bio: String = "",
    val status: UserStatus = UserStatus.OFFLINE,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val isProfileSetupComplete: Boolean = false,
    val fcmToken: String? = null
)


