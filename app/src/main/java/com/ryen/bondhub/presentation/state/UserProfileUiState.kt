package com.ryen.bondhub.presentation.state

data class UserProfileUiState(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profilePictureUrl: String? = null,
    val bio: String = "",
)
