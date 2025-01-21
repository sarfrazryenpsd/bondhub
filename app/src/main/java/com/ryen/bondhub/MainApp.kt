package com.ryen.bondhub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ryen.bondhub.presentation.screens.auth.SignInScreen
import com.ryen.bondhub.presentation.screens.auth.SignUpScreen

@Composable
fun MainApp() {
    Surface(modifier = Modifier.fillMaxSize()){
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "signIn") {
            composable("signIn"){
                SignInScreen(navController = navController)
            }
            composable("signUp"){
                SignUpScreen(navController = navController)
            }
        }
    }
}