package com.ryen.bondhub.presentation.state

sealed class FriendsState {
    data object Loading : FriendsState()
    data object Empty : FriendsState()
    data class Success(val friends: List<FriendRequest>) : FriendsState()
    data class Error(val message: String) : FriendsState()
}