package com.ryen.bondhub.presentation.event

import android.net.Uri
import com.ryen.bondhub.domain.model.MessageStatus

sealed class ChatMessageEvent {
    data class InputChanged(val newText: String) : ChatMessageEvent()
    data object ToggleEmojiPicker : ChatMessageEvent()
    data object ScrollHandled : ChatMessageEvent()
    data class SendMessage(val content: String) : ChatMessageEvent()
    data object LoadMessages : ChatMessageEvent()
    data class UpdateMessageStatus(val messageId: String, val status: MessageStatus) : ChatMessageEvent()
    data class AttachImage(val uri: Uri) : ChatMessageEvent()
    data object MarkMessagesAsRead : ChatMessageEvent()
    data object NavigateBack : ChatMessageEvent()
}