package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.UserProfile

sealed class UserProfileScreenState{
    data object Initial : UserProfileScreenState()
    data object Loading : UserProfileScreenState()
    data class Error(val message: String) : UserProfileScreenState()
    data class Success(val userProfile: UserProfile) : UserProfileScreenState()
}