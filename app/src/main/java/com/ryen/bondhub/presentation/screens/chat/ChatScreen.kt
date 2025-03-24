package com.ryen.bondhub.presentation.screens.chat

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.event.UiEvent

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigate: () -> Unit
) {
    val chatScreenState by viewModel.chatScreenState.collectAsState()
    val friendsState by viewModel.friendsState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbarError -> {
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
}