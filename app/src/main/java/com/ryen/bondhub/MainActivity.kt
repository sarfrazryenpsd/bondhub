package com.ryen.bondhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.ryen.bondhub.presentation.screens.MainApp
import com.ryen.bondhub.presentation.theme.BondHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Companion object to hold notification navigation data that can be accessed statically
    companion object {
        private var pendingChatId: String? = null
        private var pendingFriendUserId: String? = null
        private var pendingNavigationNeeded = false
        private const val NOTIFICATION_PERMISSION_CODE = 123

        fun handleNotificationNavigation(navController: NavHostController) {
            if (pendingNavigationNeeded && pendingChatId != null && pendingFriendUserId != null) {
                try {
                    val route = "chat_message_screen/$pendingChatId?friendUserId=$pendingFriendUserId"
                    navController.navigate(route)

                    // Reset pending navigation flags after successful navigation
                    pendingNavigationNeeded = false
                    pendingChatId = null
                    pendingFriendUserId = null
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to navigate from notification", e)
                }
            }
        }

        fun setPendingNavigation(chatId: String?, friendUserId: String?) {
            pendingChatId = chatId
            pendingFriendUserId = friendUserId
            pendingNavigationNeeded = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Store navigation data from intent, don't try to navigate yet
        handleNotificationIntent(intent)

        setContent {
            BondHubTheme {
                MainApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        if (intent.getBooleanExtra("NAVIGATE_TO_CHAT", false)) {
            val chatId = intent.getStringExtra("CHAT_ID")
            val friendUserId = intent.getStringExtra("OTHER_USER_ID")

            // Store these values for later use when NavController is ready
            setPendingNavigation(chatId, friendUserId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
}



