package com.ryen.bondhub.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.di.module.LocalAuthRepository
import com.ryen.bondhub.di.module.ProvideAuthRepository
import com.ryen.bondhub.presentation.screens.auth.AuthScreen
import com.ryen.bondhub.presentation.screens.chat.ChatScreen
import com.ryen.bondhub.presentation.screens.findFriends.FindFriendsScreen
import com.ryen.bondhub.presentation.screens.friendRequest.FriendRequestsScreen
import com.ryen.bondhub.presentation.screens.navBar.BottomNavItems
import com.ryen.bondhub.presentation.screens.navBar.CustomNavBar
import com.ryen.bondhub.presentation.screens.userProfile.ProfileUpdateScreen
import com.ryen.bondhub.presentation.screens.userProfile.UserProfileViewModel

@Composable
fun MainApp(navController: NavHostController = rememberNavController()) {
    ProvideAuthRepository {
        Surface() {
            var startDestination by remember { mutableStateOf<String?>(null) }
            val authRepository = LocalAuthRepository.current

            LaunchedEffect(Unit) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                startDestination = if (currentUser != null) {
                    if (authRepository.isProfileSetupComplete()) {
                        // Still navigate to ChatScreen (which is now MessagesScreen)
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
                // Keep track of the current route for highlighting the correct nav item
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine if we should show the bottom navigation bar
                val showBottomBar = currentRoute == Screen.ChatScreen.route ||
                        currentRoute == BottomNavItems.FriendRequests.route ||
                        currentRoute == BottomNavItems.FindFriends.route

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            CustomNavBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }

                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!
                        ) {
                            composable(Screen.AuthScreen.route) {
                                AuthScreen(
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(Screen.AuthScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }

                            composable(Screen.UserProfileSetupScreen.route) {
                                ProfileUpdateScreen(
                                    onDone = {
                                        navController.navigate(Screen.ChatScreen.route) {
                                            popUpTo(Screen.UserProfileSetupScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onSkip = {
                                        navController.navigate(Screen.ChatScreen.route) {
                                            popUpTo(Screen.UserProfileSetupScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(paddingValues)
                                )
                            }

                            // ChatScreen is now the MessagesScreen
                            composable(Screen.ChatScreen.route) {
                                // Your existing ChatScreen content (which is now MessagesScreen content)
                                ChatScreen(
                                    onNavigate = {
                                        navController.navigate(Screen.AuthScreen.route) {
                                            popUpTo(Screen.ChatScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }

                            composable(BottomNavItems.FriendRequests.route) {
                                FriendRequestsScreen()
                            }

                            composable(BottomNavItems.FindFriends.route) {
                                FindFriendsScreen()
                            }

                            composable(Screen.UserProfileEditScreen.route) {
                                val viewModel: UserProfileViewModel = hiltViewModel()
                                LaunchedEffect(Unit) {
                                    viewModel.setInitialSetupMode(false)
                                }

                                ProfileUpdateScreen(
                                    onDone = {
                                        // Will only be called if we explicitly trigger it
                                        navController.navigateUp()
                                    },
                                    onSkip = { /* This won't be shown anyway */ },
                                    modifier = Modifier.padding(paddingValues)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

