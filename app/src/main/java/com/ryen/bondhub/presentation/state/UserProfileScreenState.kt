package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.UserAuth

sealed class UserProfileScreenState{
    data object Initial : UserProfileScreenState()
    data object Loading : UserProfileScreenState()
    data class Error(val message: String) : UserProfileScreenState()
    data object Success : UserProfileScreenState()
}