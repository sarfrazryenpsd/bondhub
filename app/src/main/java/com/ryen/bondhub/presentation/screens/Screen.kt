package com.ryen.bondhub.presentation.screens

sealed class Screen(val route: String) {
    data object AuthScreen : Screen("auth_screen")
    data object ChatScreen : Screen("chat_screen")
    data object LoadingScreen : Screen("loading_screen")
    data object UserProfileSetupScreen : Screen("profile_setup_screen")
    data object UserProfileEditScreen : Screen("profile_edit_screen")
}