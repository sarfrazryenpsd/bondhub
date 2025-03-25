package com.ryen.bondhub.presentation.screens.chat

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.contents.ChatScreenContent
import com.ryen.bondhub.presentation.event.ChatEvent
import com.ryen.bondhub.presentation.event.UiEvent

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigate: () -> Unit
) {
    val chatScreenState by viewModel.chatScreenState.collectAsState()
    val friendsState by viewModel.friendsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.SUCCESS) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbarError -> {
                    snackbarState.value = SnackBarState.ERROR
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShowSnackbarSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.Navigate -> {
                    // Handle navigation later
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        content = { paddingValues ->
            ChatScreenContent(
                displayName = "User Name",
                lastMessage = "",
                profilePictureUrl = "",
                searchQuery = "",
                searchMode = false,
                context = LocalContext.current,
                onProfileClick = { },
                friendsState = friendsState,
                chatState = chatScreenState,
                onSearchValueChange = { },
                onSearchClick = { },
                onFriendsDismiss = { viewModel.onEvent(ChatEvent.CloseFriendsBottomSheet) },
                onMessageFABClick = {
                    viewModel.onEvent(ChatEvent.ToggleFriendsBottomSheet)
                },
                onFriendClick = { connection ->
                    viewModel.onEvent(ChatEvent.StartChatWithFriend(connection))
                },
                paddingValues = paddingValues
            )
        }
    )
}