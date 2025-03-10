package com.ryen.bondhub.presentation.screens.navBar

import androidx.annotation.DrawableRes
import com.ryen.bondhub.R

sealed class BottomNavItems(val route: String, @DrawableRes val icon: Int, val title: String) {
    // ChatScreen is now the same as MessagesScreen
    data object Messages : BottomNavItems("chat_screen", R.drawable.message, "Messages")
    data object FriendRequests : BottomNavItems("friend_requests", R.drawable.friends, "Requests")
    data object FindFriends : BottomNavItems("find_friends", R.drawable.friends_search, "Find Friends")

    companion object {
        fun getAllItems() = listOf(Messages, FriendRequests, FindFriends)
    }
}