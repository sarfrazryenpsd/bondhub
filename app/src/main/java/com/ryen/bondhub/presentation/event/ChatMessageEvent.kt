package com.ryen.bondhub.presentation.event

import android.net.Uri
import com.ryen.bondhub.domain.model.MessageStatus
import com.ryen.bondhub.domain.model.MessageType

sealed class ChatMessageEvent {
    data class SendMessage(val content: String) : ChatMessageEvent()
    data object LoadMessages : ChatMessageEvent()
    data class UpdateMessageStatus(val messageId: String, val status: MessageStatus) : ChatMessageEvent()
    data class AttachImage(val uri: Uri) : ChatMessageEvent()
    data object MarkMessagesAsRead : ChatMessageEvent()
    data object NavigateBack : ChatMessageEvent()
}