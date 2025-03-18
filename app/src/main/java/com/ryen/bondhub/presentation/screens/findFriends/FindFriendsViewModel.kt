package com.ryen.bondhub.presentation.screens.findFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
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
                            _uiState.value = FindFriendsState.UserFound(userProfile)
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

    fun sendConnectionRequest(userProfile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = FindFriendsState.Loading

            sendConnectionRequestUseCase(currentUserId!!, userProfile.uid).fold(
                onSuccess = {
                    _uiEvent.emit(UiEvent.ShowSnackbarSuccess("Connection request sent to ${userProfile.displayName}"))
                    _uiState.value = FindFriendsState.UserFound(userProfile)
                },
                onFailure = { e ->
                    _uiState.value = FindFriendsState.UserFound(userProfile)
                    _uiEvent.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to send connection request"))
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = FindFriendsState.Initial
        _searchQuery.value = ""
    }
}