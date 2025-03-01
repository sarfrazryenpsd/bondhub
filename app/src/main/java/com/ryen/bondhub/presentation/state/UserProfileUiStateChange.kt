package com.ryen.bondhub.presentation.state

data class UserProfileUiStateChange(
    val initialDisplayName: String = "",
    val initialBio: String = "",
    val initialProfilePictureUrl: String? = null,
    val hasChanges: Boolean = false,
    val isUpdateCompleted: Boolean = false,
    val isInitialSetup: Boolean = true
)