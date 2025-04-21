package com.ryen.bondhub.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ryen.bondhub.domain.useCases.chatMessage.MarkMessagesAsReadUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var markMessagesAsReadUseCase: MarkMessagesAsReadUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra("CHAT_ID") ?: return
        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            markMessagesAsReadUseCase(chatId, receiverId)
        }
    }
}