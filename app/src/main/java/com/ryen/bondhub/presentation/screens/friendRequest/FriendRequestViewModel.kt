package com.ryen.bondhub.presentation.screens.friendRequest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.chatConnection.AcceptConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetPendingConnectionRequestsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.RejectConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.FriendRequest
import com.ryen.bondhub.presentation.state.FriendRequestsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val getPendingConnectionRequestsUseCase: GetPendingConnectionRequestsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val acceptConnectionRequestUseCase: AcceptConnectionRequestUseCase,
    private val rejectConnectionRequestUseCase: RejectConnectionRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FriendRequestsState>(FriendRequestsState.Initial)
    val state: StateFlow<FriendRequestsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadPendingRequests()
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            _state.value = FriendRequestsState.Loading

            try {
                getPendingConnectionRequestsUseCase().collect { connections ->
                    // For each connection, we need the sender's profile
                    val requestsWithProfiles = connections.mapNotNull { connection ->
                        val senderProfile = getUserProfileUseCase(connection.user2Id)
                        senderProfile.fold(
                            onSuccess = { profile ->
                                FriendRequest(connection, profile)
                            },
                            onFailure = {
                                // Log the error but don't include this request
                                Log.e("FriendRequestsVM", "Failed to get profile for ${connection.user1Id}", it)
                                null
                            }
                        )
                    }

                    _state.value = if (requestsWithProfiles.isEmpty()) {
                        FriendRequestsState.Empty
                    } else {
                        FriendRequestsState.Success(requestsWithProfiles)
                    }
                }
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Unknown error"))
                _state.value = FriendRequestsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun acceptRequest(connectionId: String) {
        viewModelScope.launch {
            try {
                val result = acceptConnectionRequestUseCase(connectionId)
                result.fold(
                    onSuccess = {
                        _events.emit(UiEvent.ShowSnackbarSuccess("Request accepted"))
                        // No need to manually update the list - it will update through Flow collection
                    },
                    onFailure = { error ->
                        _events.emit(UiEvent.ShowSnackbarError(error.message ?: "Failed to accept request"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to accept request"))
            }
        }
    }

    fun rejectRequest(connectionId: String) {
        viewModelScope.launch {
            try {
                val result = rejectConnectionRequestUseCase(connectionId)
                result.fold(
                    onSuccess = {
                        _events.emit(UiEvent.ShowSnackbarSuccess("Request rejected"))
                        // No need to manually update the list - it will update through Flow collection
                    },
                    onFailure = { error ->
                        _events.emit(UiEvent.ShowSnackbarError(error.message ?: "Failed to reject request"))
                    }
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowSnackbarError(e.message ?: "Failed to reject request"))
            }
        }
    }

}