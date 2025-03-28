package com.ryen.bondhub.presentation.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.useCases.chatConnection.GetAcceptedConnectionsUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.presentation.event.ChatEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.ChatScreenState
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getAcceptedConnectionsUseCase: GetAcceptedConnectionsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
) : ViewModel() {

    private val _chatScreenState = MutableStateFlow<ChatScreenState>(ChatScreenState.Initial)
    val chatScreenState: StateFlow<ChatScreenState> = _chatScreenState.asStateFlow()

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        // Will load chats later
        _chatScreenState.value = ChatScreenState.Success()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.ToggleFriendsBottomSheet -> toggleFriendsBottomSheet()
            is ChatEvent.CloseFriendsBottomSheet -> closeFriendsBottomSheet()
            is ChatEvent.StartChatWithFriend -> startChatWithFriend(event.connection)
            is ChatEvent.NavigateToUserProfile -> navigateToRoute(event.route)
        }
    }

    private fun toggleFriendsBottomSheet() {
        val currentState = _chatScreenState.value
        if (currentState is ChatScreenState.Success) {
            val newShowBottomSheet = !currentState.showFriendsBottomSheet
            _chatScreenState.value = currentState.copy(showFriendsBottomSheet = newShowBottomSheet)

            // Load friends when opening bottom sheet
            if (newShowBottomSheet) {
                loadFriends()
            }
        }
    }

    private fun closeFriendsBottomSheet() {
        val currentState = _chatScreenState.value
        if (currentState is ChatScreenState.Success) {
            _chatScreenState.value = currentState.copy(showFriendsBottomSheet = false)
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading

            try {
                getAcceptedConnectionsUseCase().collect { connections ->
                    // For each connection, we need the user profile
                    val friendsWithProfiles = connections.mapNotNull { connection ->
                        val userProfile = getUserProfileUseCase(connection.user2Id)
                        userProfile.fold(
                            onSuccess = { profile ->
                                FriendRequest(connection, profile)
                            },
                            onFailure = {
                                // Log the error but don't include this friend
                                Log.e("ChatViewModel", "Failed to get profile for ${connection.user2Id}", it)
                                null
                            }
                        )
                    }

                    _friendsState.value = if (friendsWithProfiles.isEmpty()) {
                        FriendsState.Empty
                    } else {
                        FriendsState.Success(friendsWithProfiles)
                    }
                }
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Unknown error"))
                _friendsState.value = FriendsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun startChatWithFriend(connection: ChatConnection) {
        viewModelScope.launch {
            // Close the bottom sheet first
            closeFriendsBottomSheet()

            // For now, just emit a success message
            // Later we'll implement actual chat creation and navigation

            // Future implementation:
            // val chatId = createChatUseCase(connection.connectionId).getOrNull()
            // if (chatId != null) {
            //     _events.emit(UiEvent.Navigate("chat_screen/chat_message/$chatId"))
            // } else {
            //     _events.emit(UiEvent.ShowSnackbarError("Failed to create chat"))
            // }
        }
    }

    private fun navigateToRoute(route : String) {
        viewModelScope.launch {
            _events.emit(UiEvent.Navigate(route))
        }
    }

}