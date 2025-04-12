package com.ryen.bondhub.presentation.screens.chatMessage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.ChatMessage
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.chat.CheckChatExistRemotelyUseCase
import com.ryen.bondhub.domain.useCases.chat.CreateChatInFirestore
import com.ryen.bondhub.domain.useCases.chatMessage.GetChatMessagesUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.MarkMessagesAsReadUseCase
import com.ryen.bondhub.domain.useCases.chatMessage.SendMessageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.presentation.event.ChatMessageEvent
import com.ryen.bondhub.presentation.event.ChatMessageUiEvent
import com.ryen.bondhub.presentation.state.ChatMessageScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatMessageViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val authRepository: AuthRepository,
    private val checkChatExistRemotelyUseCase: CheckChatExistRemotelyUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val createChatInFirestore: CreateChatInFirestore,
) : ViewModel() {

    private val _chatMessageScreenState = MutableStateFlow<ChatMessageScreenState>(ChatMessageScreenState.Initial)
    val chatMessageScreenState: StateFlow<ChatMessageScreenState> = _chatMessageScreenState.asStateFlow()

    private val _friendProfile = MutableStateFlow(UserProfile())
    val friendProfile: StateFlow<UserProfile> = _friendProfile.asStateFlow()

    private val _events = MutableSharedFlow<ChatMessageUiEvent>()
    val events = _events.asSharedFlow()

    private var currentChatId: String = ""
    private var otherUserId: String = ""
    private var messageJob: Job? = null
    private var chatExists: Boolean = false

    fun initialize(chatId: String, friendConnectionId: String, friendUserId: String) {
        this.currentChatId = chatId
        this.otherUserId = friendConnectionId

        viewModelScope.launch(Dispatchers.IO) {
            val newProfile = getUserProfileUseCase(friendUserId).fold(
                onSuccess = {
                    it
                },
                onFailure = {
                    Log.e("ChatMessageViewModel", "Failed to get user profile", it)
                    null
                }
            )
            _friendProfile.value = newProfile ?: UserProfile()
        }

        if (_chatMessageScreenState.value is ChatMessageScreenState.Initial) {
            // Check if chat exists remotely
            checkChatExists()
            loadMessages()
            markMessagesAsRead()
        }
    }

    private fun checkChatExists() {
        viewModelScope.launch {
            chatExists = try {
                checkChatExistRemotelyUseCase(currentChatId).getOrDefault(false)
            } catch (e: Exception) {
                Log.e("ChatMessageViewModel", "Error checking if chat exists", e)
                false
            }
        }
    }

    fun onEvent(event: ChatMessageEvent) {
        when (event) {
            is ChatMessageEvent.SendMessage -> sendMessage(event.content)
            is ChatMessageEvent.LoadMessages -> loadMessages()
            is ChatMessageEvent.UpdateMessageStatus -> updateMessageStatus(event.messageId, event.status)
            is ChatMessageEvent.AttachImage -> attachImage(event.uri)
            is ChatMessageEvent.MarkMessagesAsRead -> markMessagesAsRead()
            is ChatMessageEvent.NavigateBack -> navigateBack()
        }
    }

    private fun loadMessages() {
        // Cancel previous job if any
        messageJob?.cancel()

        if (currentChatId.isEmpty()) {
            _chatMessageScreenState.value = ChatMessageScreenState.Success(emptyList())
            return
        }

        _chatMessageScreenState.value = ChatMessageScreenState.Loading

        messageJob = viewModelScope.launch {
            try {
                getChatMessagesUseCase(currentChatId).collect { messages ->
                    _chatMessageScreenState.value = ChatMessageScreenState.Success(messages)
                }
            } catch (e: Exception) {
                _chatMessageScreenState.value = ChatMessageScreenState.Error(e.message ?: "Failed to load messages")
                _events.emit(ChatMessageUiEvent.ShowSnackbarError(e.message ?: "Failed to load messages"))
            }
        }
    }

    private fun sendMessage(content: String) {
        if (content.isBlank()) return

        val currentState = _chatMessageScreenState.value
        if (currentState is ChatMessageScreenState.Success && !currentState.canSendMessage) return

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _events.emit(ChatMessageUiEvent.ShowSnackbarError("User not authenticated"))
                    return@launch
                }

                // Update UI state to show message is being sent
                if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                    _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                        .copy(canSendMessage = false)
                }

                // Check if chat exists in Firestore and create it if this is the first message
                if (!chatExists) {
                    try {
                        val createChatResult = createChatInFirestore(
                            currentChatId, currentUser.uid, otherUserId
                        )

                        if (createChatResult.isFailure) {
                            _events.emit(ChatMessageUiEvent.ShowSnackbarError("Failed to create chat"))
                            // Re-enable sending
                            if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                                _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                                    .copy(canSendMessage = true)
                            }
                            return@launch
                        }

                        // Mark chat as existing now
                        chatExists = true
                    } catch (e: Exception) {
                        _events.emit(ChatMessageUiEvent.ShowSnackbarError(e.message ?: "Failed to create chat"))
                        // Re-enable sending
                        if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                            _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                                .copy(canSendMessage = true)
                        }
                        return@launch
                    }
                }

                // Create message object
                val message = ChatMessage(
                    chatId = currentChatId,
                    senderId = currentUser.uid,
                    receiverId = otherUserId,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    messageType = MessageType.TEXT,
                    status = MessageStatus.SENDING
                )

                sendMessageUseCase(message).fold(
                    onSuccess = {
                        // Reset input field via event
                        _events.emit(ChatMessageUiEvent.ClearInput)

                        // Re-enable sending
                        if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                            _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                                .copy(canSendMessage = true)
                        }
                    },
                    onFailure = { exception ->
                        _events.emit(ChatMessageUiEvent.ShowSnackbarError(exception.message ?: "Failed to send message"))

                        // Re-enable sending
                        if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                            _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                                .copy(canSendMessage = true, error = exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _events.emit(ChatMessageUiEvent.ShowSnackbarError(e.message ?: "Unknown error"))

                // Re-enable sending
                if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                    _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                        .copy(canSendMessage = true, error = e.message)
                }
            }
        }
    }

    private fun updateMessageStatus(messageId: String, status: MessageStatus) {
        // Not implemented yet, will be added in future tasks
    }

    private fun attachImage(uri: Uri) {
        // Not implemented yet, will be added in future tasks for rich media support
        viewModelScope.launch {
            _events.emit(ChatMessageUiEvent.ShowSnackbarInfo("Image attachment will be available soon"))
        }
    }

    private fun markMessagesAsRead() {
        if (currentChatId.isEmpty() || otherUserId.isEmpty()) return

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser() ?: return@launch

                markMessagesAsReadUseCase(currentChatId, currentUser.uid)
                    .onFailure { exception ->
                        Log.e("ChatMessageViewModel", "Failed to mark messages as read", exception)
                    }
            } catch (e: Exception) {
                Log.e("ChatMessageViewModel", "Error marking messages as read", e)
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _events.emit(ChatMessageUiEvent.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageJob?.cancel()
    }


}