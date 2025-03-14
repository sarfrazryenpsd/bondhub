package com.ryen.bondhub.presentation.screens.chatMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType
import com.ryen.bondhub.domain.useCases.chatConnection.GetConnectionBetweenUsersUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.DeleteMessageUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.GetMessagesUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.GetUnreadMessagesCountUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.MarkMessagesAsReadUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.SendMessageUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.UpdateMessageStatusUseCase
import com.ryen.bondhub.presentation.event.ChatMessageEvent
import com.ryen.bondhub.presentation.event.ChatMessageUiEvent
import com.ryen.bondhub.presentation.state.ChatMessageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatMessageViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val updateMessageStatusUseCase: UpdateMessageStatusUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val getUnreadMessagesCountUseCase: GetUnreadMessagesCountUseCase,
    private val getConnectionBetweenUsersUseCase: GetConnectionBetweenUsersUseCase,
) : ViewModel() {

    private val _messageState = MutableStateFlow<ChatMessageState>(ChatMessageState.Initial)
    val messageState = _messageState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ChatMessageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Map of connectionId to unread count
    private val _unreadCountsMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCountsMap = _unreadCountsMap.asStateFlow()

    fun processEvent(event: ChatMessageEvent) {
        when (event) {
            is ChatMessageEvent.SendMessage -> sendMessage(
                event.connectionId,
                event.senderId,
                event.content,
                event.messageType
            )
            is ChatMessageEvent.LoadMessages -> loadMessages(event.connectionId)
            is ChatMessageEvent.UpdateMessageStatus -> updateMessageStatus(
                event.messageId,
                event.status
            )
            is ChatMessageEvent.DeleteMessage -> deleteMessage(event.messageId)
            is ChatMessageEvent.MarkAsRead -> markMessagesAsRead(
                event.connectionId,
                event.receiverId
            )
        }
    }

    private fun sendMessage(
        connectionId: String,
        senderId: String,
        content: String,
        messageType: MessageType
    ) {
        viewModelScope.launch {
            val result = sendMessageUseCase(connectionId, senderId, content, messageType)

            result.onSuccess { message ->
                _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar("Message sent"))
            }.onFailure { error ->
                _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar(error.message ?: "Failed to send message"))
            }
        }
    }

    private fun loadMessages(connectionId: String) {
        viewModelScope.launch {
            _messageState.value = ChatMessageState.Loading

            getMessagesUseCase(connectionId)
                .catch { error ->
                    _messageState.value = ChatMessageState.Error(error.message ?: "Failed to load messages")
                }
                .collect { messages ->
                    _messageState.value = if (messages.isNotEmpty()) {
                        ChatMessageState.Success(messages)
                    } else {
                        ChatMessageState.Success(emptyList())
                    }
                }
        }
    }

    private fun updateMessageStatus(messageId: String, status: MessageStatus) {
        viewModelScope.launch {
            updateMessageStatusUseCase(messageId, status)
                .onFailure { error ->
                    _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar(error.message ?: "Failed to update message status"))
                }
        }
    }

    private fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteMessageUseCase(messageId)
                .onSuccess {
                    _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar("Message deleted"))
                }
                .onFailure { error ->
                    _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar(error.message ?: "Failed to delete message"))
                }
        }
    }

    private fun markMessagesAsRead(connectionId: String, receiverId: String) {
        viewModelScope.launch {
            markMessagesAsReadUseCase(connectionId, receiverId)
                .onFailure { error ->
                    _uiEvent.emit(ChatMessageUiEvent.ShowSnackbar(error.message ?: "Failed to mark messages as read"))
                }
        }
    }

    fun loadUnreadMessagesCount(connectionId: String, userId: String) {
        viewModelScope.launch {
            getUnreadMessagesCountUseCase(connectionId, userId)
                .catch { /* Handle error silently */ }
                .collect { count ->
                    val currentMap = _unreadCountsMap.value.toMutableMap()
                    currentMap[connectionId] = count
                    _unreadCountsMap.value = currentMap
                }
        }
    }
}