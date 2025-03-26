package com.ryen.bondhub.presentation.screens.friendRequest

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.contents.FriendRequestScreenContent
import com.ryen.bondhub.presentation.event.UiEvent

@Composable
fun FriendRequestsScreen(
    viewModel: FriendRequestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.SUCCESS) }

    // Collect one-time events
    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbarSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is UiEvent.ShowSnackbarError -> {
                    snackbarState.value = SnackBarState.ERROR
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }

                is UiEvent.Navigate -> {}
                UiEvent.Logout -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        content = { paddingValues ->
            FriendRequestScreenContent(
                paddingValues = paddingValues,
                state = state,
                onAccept = { connectionId -> viewModel.acceptRequest(connectionId) },
                onReject = { connectionId -> viewModel.rejectRequest(connectionId) }
            )
        }
    )
}