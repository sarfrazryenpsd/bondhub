package com.ryen.bondhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.ryen.bondhub.presentation.screens.MainApp
import com.ryen.bondhub.presentation.screens.Screen
import com.ryen.bondhub.presentation.theme.BondHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        val launchIntent = intent

        setContent {
            BondHubTheme {
                MainApp(notificationIntent = launchIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val navController = findNavController()
        handleNotificationNavigation(intent, navController)
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

    private fun findNavController(): NavController? {
        // This is a fallback method - won't be needed with proper implementation
        return null
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 123

        fun handleNotificationNavigation(intent: Intent, navController: NavController?) {
            if (intent.getBooleanExtra("NAVIGATE_TO_CHAT", false)) {
                val chatId = intent.getStringExtra("CHAT_ID") ?: return
                val friendUserId = intent.getStringExtra("OTHER_USER_ID") ?: return

                // Navigate to chat message screen
                val route = "chat_message_screen/$chatId?friendUserId=$friendUserId"

                // Navigate while ensuring proper back stack behavior
                navController?.navigate(route) {
                    // Pop up to the main chat screen to avoid stacking
                    popUpTo(Screen.ChatScreen.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}



