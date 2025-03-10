package com.ryen.bondhub.presentation.screens.findFriends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FindFriendsScreen() {
    // Your friend requests screen content
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Find Friend Screen", modifier = Modifier.align(Alignment.Center))
    }
}