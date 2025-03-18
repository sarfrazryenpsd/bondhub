package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.UserProfile

sealed class FindFriendsState {
    data object Initial : FindFriendsState()
    data object Loading : FindFriendsState()
    data class UserFound(val userProfile: UserProfile) : FindFriendsState()
    data object UserNotFound : FindFriendsState()
    data class Error(val message: String) : FindFriendsState()
}