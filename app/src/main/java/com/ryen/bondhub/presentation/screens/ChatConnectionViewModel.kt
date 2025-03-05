package com.ryen.bondhub.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.chatConnection.AcceptConnectionRequestUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.FindExistingConnectionUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionsUseCase
import com.ryen.bondhub.domain.useCases.chatConnection.SendConnectionRequestUseCase
import com.ryen.bondhub.presentation.event.ChatEvent
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.ChatConnectionState
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
    private val findExistingConnectionUseCase: FindExistingConnectionUseCase
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ChatConnectionState>(ChatConnectionState.Initial)
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
            _connectionState.value = ChatConnectionState.Loading

            val result = sendConnectionRequestUseCase(currentUserId, targetUserId)

            result.onSuccess { connection ->
                _connectionState.value = ChatConnectionState.Success(listOf(connection))
                _uiEvent.emit(UiEvent.ShowSnackbar("Connection request sent successfully"))
            }.onFailure { error ->
                _connectionState.value = ChatConnectionState.Error(error.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(error.message ?: "Failed to send connection request"))
            }
        }
    }

    private fun acceptConnectionRequest(connectionId: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionState.Loading

            val result = acceptConnectionRequestUseCase(connectionId)

            result.onSuccess {
                _uiEvent.emit(UiEvent.ShowSnackbar("Connection request accepted"))
            }.onFailure { error ->
                _connectionState.value = ChatConnectionState.Error(error.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbar(error.message ?: "Failed to accept connection request"))
            }
        }
    }

    private fun findExistingConnection(user1Id: String, user2Id: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionState.Loading

            val connection = findExistingConnectionUseCase(user1Id, user2Id)

            connection?.let {
                _connectionState.value = ChatConnectionState.Success(listOf(it))
            } ?: run {
                _connectionState.value = ChatConnectionState.Error("No existing connection found")
                _uiEvent.emit(UiEvent.ShowSnackbar("No existing connection"))
            }
        }
    }

    fun loadConnections(userId: String) {
        viewModelScope.launch {
            _connectionState.value = ChatConnectionState.Loading

            getConnectionsUseCase(userId).collect { connections ->
                _connectionState.value = if (connections.isNotEmpty()) {
                    ChatConnectionState.Success(connections)
                } else {
                    ChatConnectionState.Error("No connections found")
                }
            }
        }
    }
}