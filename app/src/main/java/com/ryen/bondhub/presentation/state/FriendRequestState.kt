package com.ryen.bondhub.presentation.state

sealed class FriendRequestsState {
    data object Initial : FriendRequestsState()
    data object Loading : FriendRequestsState()
    data object Empty : FriendRequestsState()
    data class Success(val requests: List<FriendRequest>) : FriendRequestsState()
    data class Error(val message: String) : FriendRequestsState()
}