package com.ryen.bondhub.presentation.screens.chatConnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.chatConnection.AcceptConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.FindExistingConnectionUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.SendConnectionRequestUseCase
import com.ryen.bondhub.presentation.event.ChatEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.ChatConnectionScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatConnectionViewModel @Inject constructor(
    private val sendConnectionRequestUseCase: SendConnectionRequestUseCase,
    private val acceptConnectionRequestUseCase: AcceptConnectionRequestUseCase,
    private val getConnectionsUseCase: GetConnectionsUseCase,
    private val findExistingConnectionUseCase: FindExistingConnectionUseCase,
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ChatConnectionScreenState>(ChatConnectionScreenState.Initial)
    val connectionState = _connectionState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun processEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendConnectionRequest -> sendConnectionRequest(
                event.currentUserId,
                event.targetUserId
            )
            is ChatEvent.AcceptConnectionRequest -> acceptConnectionRequest(
                event.connectionId
            )
            is ChatEvent.FindExistingConnection -> findExistingConnection(
                event.user1Id,
                event.user2Id
            )
        }
    }

    private fun sendConnectionRequest(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionScreenState.Loading

            val result = sendConnectionRequestUseCase(currentUserId, targetUserId)

            result.onSuccess { connection ->
                _connectionState.value = ChatConnectionScreenState.Success(listOf(connection))
                _uiEvent.emit(UiEvent.ShowSnackbarError("Connection request sent successfully"))
            }.onFailure { error ->
                _connectionState.value = ChatConnectionScreenState.Error(error.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbarError(error.message ?: "Failed to send connection request"))
            }
        }
    }

    private fun acceptConnectionRequest(connectionId: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionScreenState.Loading

            val result = acceptConnectionRequestUseCase(connectionId)

            result.onSuccess {
                _uiEvent.emit(UiEvent.ShowSnackbarError("Connection request accepted"))
            }.onFailure { error ->
                _connectionState.value = ChatConnectionScreenState.Error(error.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbarError(error.message ?: "Failed to accept connection request"))
            }
        }
    }

    private fun findExistingConnection(user1Id: String, user2Id: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionScreenState.Loading

            val connection = findExistingConnectionUseCase(user1Id, user2Id)

            connection?.let {
                _connectionState.value = ChatConnectionScreenState.Success(listOf(it))
            } ?: run {
                _connectionState.value = ChatConnectionScreenState.Error("No existing connection found")
                _uiEvent.emit(UiEvent.ShowSnackbarError("No existing connection"))
            }
        }
    }

    fun loadConnections(userId: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionScreenState.Loading

            getConnectionsUseCase(userId).collect { connections ->
                _connectionState.value = if (connections.isNotEmpty()) {
                    ChatConnectionScreenState.Success(connections)
                } else {
                    ChatConnectionScreenState.Error("No connections found")
                }
            }
        }
    }
}