package com.ryen.bondhub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ryen.bondhub.presentation.screens.auth.AuthScreen

@Composable
fun MainApp() {
    Surface(modifier = Modifier.fillMaxSize()){
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "authScreen") {
            composable("authScreen"){
                AuthScreen(navController = navController)
            }
        }
    }
}