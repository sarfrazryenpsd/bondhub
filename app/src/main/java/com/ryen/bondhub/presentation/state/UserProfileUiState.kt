package com.ryen.bondhub.presentation.state

data class UserProfileUiState(
    val email: String = "",
    val displayName: String = "",
    val profilePictureUrl: String? = null,
    val bio: String? = null,
)
