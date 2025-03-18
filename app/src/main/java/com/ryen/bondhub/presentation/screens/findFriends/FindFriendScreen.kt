package com.ryen.bondhub.presentation.screens.findFriends

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
import com.ryen.bondhub.presentation.event.UiEvent

@Composable
fun FindFriendsScreen(
    viewModel: FindFriendsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.INITIAL) }

    // Handle UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbarSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                    snackbarState.value = SnackBarState.SUCCESS
                }
                is UiEvent.ShowSnackbarError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                    snackbarState.value = SnackBarState.ERROR
                }

                is UiEvent.Navigate -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        content = {

        }
    )
}