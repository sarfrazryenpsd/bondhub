package com.ryen.bondhub.presentation.state

data class ChatUiState(
    val inputText: String = "",
    val showEmojiPicker: Boolean = false,
    val scrollToBottom: Boolean = false
)