package com.ryen.bondhub.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.di.module.LocalAuthRepository
import com.ryen.bondhub.di.module.ProvideAuthRepository
import com.ryen.bondhub.presentation.screens.auth.AuthScreen
import com.ryen.bondhub.presentation.screens.chat.ChatScreen
import com.ryen.bondhub.presentation.screens.userProfile.ProfileUpdateScreen


@Composable
fun MainApp(
    navController: NavHostController = rememberNavController()
) {
    ProvideAuthRepository {
        Surface(modifier = Modifier.fillMaxSize()) {
            var startDestination by remember { mutableStateOf<String?>(null) }
            val authRepository = LocalAuthRepository.current

            LaunchedEffect(Unit) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                startDestination = if (currentUser != null) {
                    if (authRepository.isProfileSetupComplete()) {
                        Screen.ChatScreen.route
                    } else {
                        Screen.UserProfileSetupScreen.route
                    }
                } else {
                    Screen.AuthScreen.route
                }
            }

            // Show loading screen while determining start destination
            if (startDestination == null) {
                LoadingScreen()
            } else {
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!
                ) {
                    composable(Screen.AuthScreen.route) {
                        AuthScreen(
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(Screen.AuthScreen.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.UserProfileSetupScreen.route) {
                        ProfileUpdateScreen(
                            onDone = {
                                navController.navigate(Screen.ChatScreen.route) {
                                    popUpTo(Screen.UserProfileSetupScreen.route) { inclusive = true }
                                }
                            },
                            onSkip = {
                                navController.navigate(Screen.ChatScreen.route) {
                                    popUpTo(Screen.UserProfileSetupScreen.route) { inclusive = true }
                                }
                            },
                            isInitialSetup = true
                        )
                    }

                    composable(Screen.ChatScreen.route) {
                        ChatScreen(
                            onNavigateToAuth = {
                                navController.navigate(Screen.AuthScreen.route) {
                                    popUpTo(Screen.ChatScreen.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(Screen.UserProfileEditScreen.route){
                        ProfileUpdateScreen(
                            isInitialSetup = false
                        )
                    }
                }
            }
        }
    }
}

