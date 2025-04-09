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
import com.ryen.bondhub.presentation.screens.ErrorScreen
import com.ryen.bondhub.presentation.screens.LoadingScreen
import com.ryen.bondhub.presentation.screens.Screen

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateTo: (String) -> Unit,
) {
    val chatScreenState by viewModel.chatScreenState.collectAsState()
    val userProfileState by viewModel.userProfileState.collectAsState()
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
                    onNavigateTo(event.route)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        content = { paddingValues ->
            when (val profileState = userProfileState) {
                is UserProfileState.Loading -> {
                    LoadingScreen()
                }
                is UserProfileState.Success -> {
                    ChatScreenContent(
                        displayName = profileState.userProfile.displayName,
                        profilePictureUrl = profileState.userProfile.profilePictureUrl ?: "",
                        searchQuery = "",
                        searchMode = false,
                        context = LocalContext.current,
                        onProfileClick = {
                            viewModel.onEvent(ChatEvent.NavigateToUserProfile(Screen.UserProfileEditScreen.route))
                        },
                        friendsState = friendsState,
                        chatState = chatScreenState,
                        onSearchValueChange = { },
                        onSearchClick = { },
                        onChatClick = { chatId, otherUserId ->
                            viewModel.onEvent(ChatEvent.NavigateToChat(chatId, otherUserId))
                        },
                        onDeleteChat = { chatId ->
                            viewModel.onEvent(ChatEvent.DeleteChat(chatId))
                        },
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
                is UserProfileState.Error -> {
                    ErrorScreen(
                        message = profileState.message,
                        onRetry = { viewModel.refreshUserProfile() }
                    )
                }
            }
        }
    )
}