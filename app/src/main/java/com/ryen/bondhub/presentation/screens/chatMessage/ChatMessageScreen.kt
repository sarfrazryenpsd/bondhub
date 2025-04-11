package com.ryen.bondhub.presentation.screens.chatMessage

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.contents.ChatMessageScreenContent
import com.ryen.bondhub.presentation.components.ChatMessageTopBar
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.event.ChatMessageEvent
import com.ryen.bondhub.presentation.event.ChatMessageUiEvent
import com.ryen.bondhub.presentation.screens.ErrorScreen
import com.ryen.bondhub.presentation.screens.LoadingScreen
import com.ryen.bondhub.presentation.state.ChatMessageScreenState

@Composable
fun ChatMessageScreen(
    viewModel: ChatMessageViewModel = hiltViewModel(),
    chatId: String,
    otherUserId: String,
    // Navigation handlers will be implemented later
    onNavigateBack: () -> Unit = {},
) {
    val chatMessageState by viewModel.chatMessageScreenState.collectAsState()
    val friendProfile by viewModel.friendProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.SUCCESS) }

    // Initialize the ViewModel with chat parameters
    LaunchedEffect(chatId, otherUserId) {
        viewModel.initialize(chatId, otherUserId)
    }

    // Collect UI events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatMessageUiEvent.ShowSnackbarError -> {
                    snackbarState.value = SnackBarState.ERROR
                    snackbarHostState.showSnackbar(event.message)
                }
                is ChatMessageUiEvent.ShowSnackbarSuccess -> {
                    snackbarState.value = SnackBarState.SUCCESS
                    snackbarHostState.showSnackbar(event.message)
                }
                is ChatMessageUiEvent.ShowSnackbarInfo -> {
                    //snackbarState.value = SnackBarState.INFO
                    snackbarHostState.showSnackbar(event.message)
                }
                is ChatMessageUiEvent.Navigate -> {
                    // Navigation will be handled later
                    // onNavigateTo(event.route)
                }
                is ChatMessageUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            ChatMessageTopBar(
                onBackClick = { viewModel.onEvent(ChatMessageEvent.NavigateBack) },
                userProfile = friendProfile
            )
        },
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
    ) { paddingValues ->
        when (val state = chatMessageState) {
            is ChatMessageScreenState.Initial,
            is ChatMessageScreenState.Loading -> {
                LoadingScreen()
            }
            is ChatMessageScreenState.Success -> {
                ChatMessageScreenContent(
                    messages = state.messages,
                    isLoading = state.isLoading,
                    canSendMessage = state.canSendMessage,
                    onSendMessage = { content ->
                        viewModel.onEvent(ChatMessageEvent.SendMessage(content))
                    },
                    onAttachImage = { uri ->
                        viewModel.onEvent(ChatMessageEvent.AttachImage(uri))
                    },
                    paddingValues = paddingValues
                )
            }
            is ChatMessageScreenState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.onEvent(ChatMessageEvent.LoadMessages) }
                )
            }
        }
    }
}
