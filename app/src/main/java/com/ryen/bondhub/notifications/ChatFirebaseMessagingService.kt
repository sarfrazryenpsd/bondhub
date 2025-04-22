package com.ryen.bondhub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ryen.bondhub.MainActivity
import com.ryen.bondhub.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var tokenRepository: FCMTokenRepository

    private val TAG = "ChatFirebaseMessagingService"

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            tokenRepository.updateUserFCMToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Received FCM message: ${remoteMessage.data}")

        remoteMessage.data.forEach { (key, value) ->
            Log.d(TAG, "FCM data: $key = $value")
        }

        val title = remoteMessage.data["title"] ?: "New message"
        val message = remoteMessage.data["message"] ?: ""
        val senderName = remoteMessage.data["senderName"] ?: ""
        val senderImage = remoteMessage.data["senderImage"] ?: ""
        val chatId = remoteMessage.data["chatId"] ?: ""
        val baseChatId = remoteMessage.data["baseChatId"] ?: ""
        val senderId = remoteMessage.data["senderId"] ?: ""
        val unreadCount = remoteMessage.data["unreadCount"]?.toIntOrNull() ?: 1

        showNotification(title, message, senderName, senderImage, chatId, baseChatId, senderId, unreadCount)

    }

    private fun getCurrentUserId(): String {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid ?: ""
    }

    private fun showNotification(
        title: String,
        message: String,
        senderName: String,
        senderImage: String,
        chatId: String,
        baseChatId: String,
        senderId: String,
        unreadCount: Int
    ) {
        val channelId = "chat_messages"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open the chat screen when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NAVIGATE_TO_CHAT", true)
            putExtra("CHAT_ID", chatId)
            putExtra("OTHER_USER_ID", senderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markAsReadIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            putExtra("CHAT_ID", chatId)
            putExtra("RECEIVER_ID", getCurrentUserId()) // Implement this to get current user ID
        }

        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            this,
            "markRead_${chatId}".hashCode(),
            markAsReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Load profile image for notification (optional)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Make sure to create this icon
            .setContentTitle(senderName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.double_tick, // Create this icon
                "Mark as Read",
                markAsReadPendingIntent
            )
            .setAutoCancel(true)

        // If there are multiple unread messages, show count
        if (unreadCount > 1) {
            notificationBuilder.setSubText("$unreadCount messages")
        }

        // For Android N+ show as notification group if there are multiple messages
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && unreadCount > 1) {
            notificationBuilder.setGroup("chat_$baseChatId")
        }

        // Display the notification
        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
    }
}