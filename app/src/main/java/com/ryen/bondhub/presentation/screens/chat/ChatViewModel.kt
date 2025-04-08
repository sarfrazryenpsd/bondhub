package com.ryen.bondhub.presentation.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.chat.CreateChatUseCase
import com.ryen.bondhub.domain.useCases.chat.DeleteChatUseCase
import com.ryen.bondhub.domain.useCases.chat.GetUserChatsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetAcceptedConnectionsUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.GetUnreadMessagesCountUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.presentation.event.ChatEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.ChatScreenState
import com.ryen.bondhub.presentation.state.ChatsState
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getAcceptedConnectionsUseCase: GetAcceptedConnectionsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserChatsUseCase: GetUserChatsUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val deleteChatsUseCase: DeleteChatUseCase,
    private val getUnreadMessagesCountUseCase: GetUnreadMessagesCountUseCase
) : ViewModel() {

    private val _chatScreenState = MutableStateFlow<ChatScreenState>(ChatScreenState.Initial)
    val chatScreenState: StateFlow<ChatScreenState> = _chatScreenState.asStateFlow()

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private val _chatsState = MutableStateFlow<ChatsState>(ChatsState.Loading)
    val chatsState: StateFlow<ChatsState> = _chatsState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadChats()
        loadCurrentUserProfile()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.ToggleFriendsBottomSheet -> toggleFriendsBottomSheet()
            is ChatEvent.CloseFriendsBottomSheet -> closeFriendsBottomSheet()
            is ChatEvent.StartChatWithFriend -> startChatWithFriend(event.connection)
            is ChatEvent.NavigateToUserProfile -> navigateToRoute(event.route)
            is ChatEvent.NavigateToChat -> navigateToChat(event.chatId, event.otherUserId)
            is ChatEvent.DeleteChat -> deleteChat(event.chatId)
        }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileState.Loading

            try {
                val currentUser = authRepository.getCurrentUser()

                if (currentUser != null) {
                    getUserProfileUseCase.getUserProfileRealTime(currentUser.uid)
                        .collect { result ->
                            result.onSuccess { userProfile ->
                                _userProfileState.value = UserProfileState.Success(userProfile)
                            }.onFailure { exception ->
                                _userProfileState.value = UserProfileState.Error(
                                    exception.message ?: "Failed to load user profile"
                                )
                                _events.emit(
                                    UiEvent.ShowSnackbarError(
                                        exception.message ?: "Failed to load user profile"
                                    )
                                )
                            }
                        }
                } else {
                    _userProfileState.value = UserProfileState.Error("No current user found")
                }
            } catch (e: Exception) {
                _userProfileState.value = UserProfileState.Error(
                    e.message ?: "Unknown error loading profile"
                )
                _events.emit(
                    UiEvent.ShowSnackbarError(
                        e.message ?: "Unknown error loading profile"
                    )
                )
            }
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _chatsState.value = ChatsState.Loading

            try {
                getUserChatsUseCase().collect { chats ->
                    if (chats.isEmpty()) {
                        _chatsState.value = ChatsState.Empty
                    } else {
                        // For each chat, get the unread message count
                        val enhancedChats = chats.map { chat ->
                            val currentUser = authRepository.getCurrentUser()
                            if (currentUser != null) {
                                // Set up a flow that will update the chat with the unread count
                                getUnreadMessagesCountUseCase(chat.chatId, currentUser.uid)
                                    .map { count ->
                                        chat.copy(unreadMessageCount = count)
                                    }
                            } else {
                                flowOf(chat)
                            }
                        }

                        // Combine all the flows
                        enhancedChats.combine().collect { updatedChats ->
                            _chatsState.value = ChatsState.Success(updatedChats)
                        }
                    }
                }
            } catch (e: Exception) {
                _chatsState.value = ChatsState.Error(e.message ?: "Failed to load chats")
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to load chats"))
            }
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

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _events.emit(UiEvent.ShowSnackbarError("User not authenticated"))
                    return@launch
                }

                // Create or get existing chat
                createChatUseCase(currentUser.uid, connection.user2Id).fold(
                    onSuccess = { chat ->
                        // Navigate to the chat screen with the chat ID
                        navigateToChat(chat.chatId, connection.user2Id)
                    },
                    onFailure = { exception ->
                        _events.emit(UiEvent.ShowSnackbarError(exception.message ?: "Failed to create chat"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to start chat"))
            }
        }
    }

    private fun deleteChat(chatId: String) {
        viewModelScope.launch {
            try {
                deleteChatsUseCase(chatId).fold(
                    onSuccess = {
                        _events.emit(UiEvent.ShowSnackbarSuccess("Chat deleted successfully"))
                        // Refresh chats list
                        loadChats()
                    },
                    onFailure = { exception ->
                        _events.emit(UiEvent.ShowSnackbarError(exception.message ?: "Failed to delete chat"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to delete chat"))
            }
        }
    }

    fun refreshUserProfile() {
        loadCurrentUserProfile()
    }

    fun refreshChats() {
        loadChats()
    }

    private fun navigateToRoute(route : String) {
        viewModelScope.launch {
            _events.emit(UiEvent.Navigate(route))
        }
    }

    private fun navigateToChat(chatId: String, otherUserId: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.Navigate("chat_message_screen/$chatId?otherUserId=$otherUserId"))
        }
    }

    private inline fun <reified T> List<Flow<T>>.combine(): Flow<List<T>> {
        return if (isEmpty()) {
            flowOf(emptyList())
        } else {
            // Combine all flows into a single flow of lists
            combine(this) { it.toList() }
        }
    }

}

sealed class UserProfileState {
    data object Loading : UserProfileState()
    data class Success(val userProfile: UserProfile) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}