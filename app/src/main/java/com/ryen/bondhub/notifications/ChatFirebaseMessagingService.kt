package com.ryen.bondhub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ryen.bondhub.MainActivity
import com.ryen.bondhub.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChatFirebaseMessagingService : FirebaseMessagingService() {

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

        showNotification(
            title,
            message,
            senderName,
            senderImage,
            chatId,
            baseChatId,
            senderId,
            unreadCount
        )

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
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            channelId,
            "Chat Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new chat messages"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

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
            putExtra("RECEIVER_ID", getCurrentUserId())
        }

        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            this,
            "markRead_${chatId}".hashCode(),
            markAsReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Load profile image for notification (optional)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_ic) // Make sure to create this icon
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
        if (unreadCount > 1) {
            notificationBuilder.setGroup("chat_$baseChatId")
        }

        // Display the notification

        if (senderImage.isNotEmpty()) {
            Log.d(TAG, "Loading profile image: $senderImage")

            // First, show the notification without the image
            val initialNotificationId = chatId.hashCode()
            notificationManager.notify(initialNotificationId, notificationBuilder.build())

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Use Coil's suspending get function to fetch the bitmap directly
                    val bitmap = ImageLoader(this@ChatFirebaseMessagingService)
                        .execute(
                            ImageRequest.Builder(this@ChatFirebaseMessagingService)
                                .data(senderImage)
                                .allowHardware(false)
                                .size(128, 128) // Set a specific size for the notification icon
                                .build()
                        ).image?.toBitmap()

                    // Update the notification with the bitmap on the main thread
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            notificationBuilder.setLargeIcon(bitmap)
                            // Update the existing notification with the new bitmap
                            notificationManager.notify(
                                initialNotificationId,
                                notificationBuilder.build()
                            )
                        } else {
                            Log.e(TAG, "Bitmap is null after loading")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception loading image: ${e.message}", e)
                }
            }
        } else {
            notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
        }
    }
}