package com.ryen.bondhub.presentation.screens.findFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.ConnectionStatus
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionStatusUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.SendConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.userProfile.FindUserByEmailUseCase
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.FindFriendsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindFriendsViewModel @Inject constructor(
    private val findUserByEmailUseCase: FindUserByEmailUseCase,
    private val sendConnectionRequestUseCase: SendConnectionRequestUseCase,
    private val getConnectionStatusUseCase: GetConnectionStatusUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FindFriendsState>(FindFriendsState.Initial)
    val uiState: StateFlow<FindFriendsState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            currentUserId = authRepository.getCurrentUser()?.uid
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun searchUserByEmail() {
        viewModelScope.launch {
            val email = _searchQuery.value.trim()

            if (email.isEmpty()) {
                _uiState.value = FindFriendsState.Error("Please enter an email address")
                return@launch
            }

            _uiState.value = FindFriendsState.Loading

            findUserByEmailUseCase(email).fold(
                onSuccess = { userProfile ->
                    if (userProfile != null) {
                        // Don't show the current user in search results
                        if (userProfile.uid == currentUserId) {
                            _uiState.value = FindFriendsState.Error("This is your email address")
                        } else {
                            // Check connection status before showing the user
                            checkConnectionStatus(userProfile)
                        }
                    } else {
                        _uiState.value = FindFriendsState.UserNotFound
                    }
                },
                onFailure = { e ->
                    _uiState.value = FindFriendsState.Error(e.message ?: "Unknown error occurred")
                }
            )
        }
    }

    private fun checkConnectionStatus(userProfile: UserProfile) {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                getConnectionStatusUseCase(userId, userProfile.uid).fold(
                    onSuccess = { connectionStatus ->
                        _uiState.value = FindFriendsState.UserFound(
                            userProfile = userProfile,
                            connectionStatus = connectionStatus
                        )
                    },
                    onFailure = { e ->
                        // Check if the error is because no connection exists yet
                        if (e is NoSuchElementException || e.message?.contains("not found") == true) {
                            // No connection exists yet, show as INITIAL
                            _uiState.value = FindFriendsState.UserFound(
                                userProfile = userProfile,
                                connectionStatus = ConnectionStatus.INITIAL
                            )
                        } else {
                            // Other error, show error state
                            _uiState.value = FindFriendsState.Error(
                                "Error checking connection: ${e.message ?: "Unknown error"}"
                            )
                        }
                    }
                )
            } ?: run {
                _uiState.value = FindFriendsState.Error("Not logged in")
            }
        }
    }

    fun sendConnectionRequest(userProfile: UserProfile) {
        viewModelScope.launch {
            // Store the current state before showing loading
            val previousState = _uiState.value
            _uiState.value = FindFriendsState.Loading

            currentUserId?.let { userId ->
                // Create a new connection with PENDING status

                sendConnectionRequestUseCase(
                    currentUserId = userId,
                    targetUserId = userProfile.uid
                ).fold(
                    onSuccess = {
                        // Update UI state to show the connection is now pending
                        val currentState = _uiState.value
                        if (currentState is FindFriendsState.UserFound) {
                            _uiState.value = currentState.copy(
                                connectionStatus = ConnectionStatus.PENDING
                            )
                        }
                        _uiEvent.emit(UiEvent.ShowSnackbarSuccess("Connection request sent"))
                        resetState()
                    },
                    onFailure = { e ->
                        _uiEvent.emit(UiEvent.ShowSnackbarError("Failed to send request: ${e.message}"))
                        _uiState.value = previousState
                    }
                )
            } ?: run {
                _uiEvent.emit(UiEvent.ShowSnackbarError("You must be logged in to send a request"))
            }
        }
    }

    private fun resetState() {
        _uiState.value = FindFriendsState.Initial
        _searchQuery.value = ""
    }
}