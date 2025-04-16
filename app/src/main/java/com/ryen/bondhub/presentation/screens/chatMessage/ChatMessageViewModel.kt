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
import com.ryen.bondhub.domain.useCases.chat.CheckChatExistsByBaseChatIdUseCase
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
    private val checkChatExistsByBaseChatIdUseCase: CheckChatExistsByBaseChatIdUseCase,
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
    private var baseChatId: String = ""
    private var otherUserId: String = ""
    private var currentUserId: String = ""
    private var messageJob: Job? = null
    private var chatExists: Boolean = false

    fun initialize(chatId: String, connectionId: String, otherUserId: String) {
        if (this.currentChatId == chatId) return  // Already initialized with this chat

        this.currentChatId = chatId
        this.otherUserId = otherUserId

        // Extract baseChatId from chatId (format: baseChatId_userId)
        this.baseChatId = chatId.split("_").firstOrNull() ?: ""

        viewModelScope.launch {
            currentUserId = authRepository.getCurrentUser()?.uid ?: ""

            // Load other user's profile
            loadFriendProfile()

            if (_chatMessageScreenState.value is ChatMessageScreenState.Initial) {
                // Check if chat exists in Firestore
                chatExists = checkChatExists()
                loadMessages()
                markMessagesAsRead()
            }
        }
    }

    private fun loadFriendProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val newProfile = getUserProfileUseCase(otherUserId).fold(
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
    }

    private suspend fun checkChatExists(): Boolean {
        try {
            val result = checkChatExistsByBaseChatIdUseCase(baseChatId)
            return result.isSuccess && result.getOrNull() == true
        } catch (e: Exception) {
            Log.e("ChatMessageViewModel", "Failed to check if chat exists", e)
            return false
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

        if (baseChatId.isEmpty()) {
            _chatMessageScreenState.value = ChatMessageScreenState.Success(emptyList())
            return
        }

        _chatMessageScreenState.value = ChatMessageScreenState.Loading

        messageJob = viewModelScope.launch {
            try {
                getChatMessagesUseCase(baseChatId).collect { messages ->
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
                // Update UI state to show message is being sent
                if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
                    _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                        .copy(canSendMessage = false)
                }

                // Check if chat exists in Firestore and create it if this is the first message
                if (!chatExists) {
                    try {
                        val createChatResult = createChatInFirestore(
                            currentChatId, currentUserId, otherUserId
                        )

                        if (createChatResult.isFailure) {
                            _events.emit(ChatMessageUiEvent.ShowSnackbarError("Failed to create chat"))
                            // Re-enable sending
                            resetSendingState()
                            return@launch
                        }

                        // Mark chat as existing now
                        chatExists = true
                    } catch (e: Exception) {
                        _events.emit(ChatMessageUiEvent.ShowSnackbarError(e.message ?: "Failed to create chat"))
                        resetSendingState()
                        return@launch
                    }
                }

                // Create message object with baseChatId
                val message = ChatMessage(
                    chatId = currentChatId,
                    baseChatId = baseChatId,
                    senderId = currentUserId,
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
                        resetSendingState()
                    },
                    onFailure = { exception ->
                        _events.emit(ChatMessageUiEvent.ShowSnackbarError(exception.message ?: "Failed to send message"))
                        resetSendingState(exception.message)
                    }
                )
            } catch (e: Exception) {
                _events.emit(ChatMessageUiEvent.ShowSnackbarError(e.message ?: "Unknown error"))
                resetSendingState(e.message)
            }
        }
    }

    private fun resetSendingState(errorMessage: String? = null) {
        if (_chatMessageScreenState.value is ChatMessageScreenState.Success) {
            _chatMessageScreenState.value = (_chatMessageScreenState.value as ChatMessageScreenState.Success)
                .copy(canSendMessage = true, error = errorMessage)
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
        if (currentChatId.isEmpty() || currentUserId.isEmpty()) return

        viewModelScope.launch {
            try {
                markMessagesAsReadUseCase(baseChatId, currentUserId)
                // No need to handle result since this is a background operation
            } catch (e: Exception) {
                Log.e("ChatMessageViewModel", "Failed to mark messages as read", e)
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