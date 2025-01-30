package com.ryen.bondhub.domain.model

data class UserProfile(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String? = null
)
