package com.ryen.bondhub.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ryen.bondhub.presentation.screens.auth.AuthScreen
import com.ryen.bondhub.presentation.screens.chat.ChatScreen


@Composable
fun MainApp(
    navController: NavHostController = rememberNavController(),
    viewModel: MainAppViewModel = hiltViewModel()
) {
    Surface(modifier = Modifier.fillMaxSize()){
        val startDestination by viewModel.startDestination.collectAsState()

        LaunchedEffect(startDestination) {
            startDestination?.let { destination ->
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }

        NavHost(navController = navController, startDestination = Screen.LoadingScreen.route) {
            composable(Screen.AuthScreen.route){
                AuthScreen(
                    onNavigateToChat = {
                        navController.navigate(Screen.ChatScreen.route){
                            popUpTo(Screen.AuthScreen.route){ inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ChatScreen.route){
                ChatScreen(
                    onNavigateToAuth = {
                        navController.navigate(Screen.AuthScreen.route){
                            popUpTo(Screen.ChatScreen.route){ inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.LoadingScreen.route){
                LoadingScreen()
            }
        }
    }
}