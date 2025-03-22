package com.ryen.bondhub.presentation.screens.findFriends

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
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
import com.ryen.bondhub.presentation.contents.FindFriendsScreenContent
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.FindFriendsState

@Composable
fun FindFriendsScreen(
    viewModel: FindFriendsViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.SUCCESS) }

    // Handle UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
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
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        contentWindowInsets = WindowInsets.safeContent,
        content = {
            FindFriendsScreenContent(
                query = searchQuery,
                onSendRequest = { viewModel.sendConnectionRequest((state as FindFriendsState.UserFound).userProfile) },
                onQueryChanged = viewModel::onSearchQueryChanged,
                onSearch = viewModel::searchUserByEmail,
                uiState = state,
                paddingValues = it,
            )
        }
    )
}