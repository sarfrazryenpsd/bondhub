package com.ryen.bondhub.presentation.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.ryen.bondhub.MainActivity
import com.ryen.bondhub.di.module.LocalAuthRepository
import com.ryen.bondhub.di.module.ProvideAuthRepository
import com.ryen.bondhub.presentation.screens.auth.AuthScreen
import com.ryen.bondhub.presentation.screens.chat.ChatScreen
import com.ryen.bondhub.presentation.screens.chatMessage.ChatMessageScreen
import com.ryen.bondhub.presentation.screens.findFriends.FindFriendsScreen
import com.ryen.bondhub.presentation.screens.friendRequest.FriendRequestsScreen
import com.ryen.bondhub.presentation.screens.navBar.BottomNavItems
import com.ryen.bondhub.presentation.screens.navBar.CustomNavBar
import com.ryen.bondhub.presentation.screens.userProfile.ProfileUpdateScreen
import com.ryen.bondhub.presentation.screens.userProfile.UserProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun MainApp(navController: NavHostController = rememberNavController(), notificationIntent: Intent? = null) {
    ProvideAuthRepository {
        Surface {
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

                LaunchedEffect(startDestination) {
                    // Give some time for NavHost to be fully initialized
                    delay(300)
                    MainActivity.handleNotificationNavigation(navController)
                }

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
                                    },
                                    onLogout = {

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
                                    modifier = Modifier.padding(paddingValues),
                                    showTopBar = false,
                                    onBackClick = {  }
                                )
                            }

                            // ChatScreen is now the MessagesScreen
                            composable(Screen.ChatScreen.route) {
                                // Your existing ChatScreen content (which is now MessagesScreen content)
                                ChatScreen(
                                    onNavigateTo = { route ->
                                        navController.navigate(route)
                                    },
                                )
                            }

                            composable(BottomNavItems.FriendRequests.route) {
                                FriendRequestsScreen()
                            }

                            composable(BottomNavItems.FindFriends.route) {
                                FindFriendsScreen()
                            }

                            composable(
                                route = Screen.UserProfileEditScreen.route,
                                enterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { it }
                                    )
                                },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { -it }
                                    )
                                },
                                popEnterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { -it }
                                    )
                                },
                                popExitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { it }
                                    )
                                }
                                ) {
                                val viewModel: UserProfileViewModel = hiltViewModel()
                                LaunchedEffect(Unit) {
                                    viewModel.setInitialSetupMode(false)
                                }

                                ProfileUpdateScreen(
                                    onDone = {
                                        // Will only be called if we explicitly trigger it
                                        navController.navigateUp()
                                    },
                                    showTopBar = true,
                                    onBackClick = { navController.navigateUp() },
                                    onLogout = {
                                        // Implement logout logic
                                        navController.navigate(Screen.AuthScreen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onSkip = { /* This won't be shown anyway */ },
                                    modifier = Modifier.padding(paddingValues)
                                )
                            }

                            composable(
                                route = "chat_message_screen/{chatId}?friendUserId={friendUserId}",
                                arguments = listOf(
                                    navArgument("chatId") { type = NavType.StringType },
                                    navArgument("friendUserId") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                ),
                                enterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { it }
                                    )
                                },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { -it }
                                    )
                                },
                                popEnterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { -it }
                                    )
                                },
                                popExitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { it }
                                    )
                                }
                            ) { backStackEntry ->
                                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                                val friendUserId = backStackEntry.arguments?.getString("friendUserId") ?: ""

                                ChatMessageScreen(
                                    chatId = chatId,
                                    friendUserId = friendUserId,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//Fix

/*1. FATAL EXCEPTION: DefaultDispatcher-worker-1 (Ask Gemini)
Process: com.ryen.bondhub, PID: 5412
kotlin.UninitializedPropertyAccessException: lateinit property markMessagesAsReadUseCase has not been initialized
at com.ryen.bondhub.notifications.NotificationActionReceiver.getMarkMessagesAsReadUseCase(NotificationActionReciever.kt:13)
at com.ryen.bondhub.notifications.NotificationActionReceiver$onReceive$1.invokeSuspend(NotificationActionReciever.kt:21)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:101)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@b8929ed, Dispatchers.IO]*/

/*2.Mark messages as read use case and unreadMessageCount issue*/

//3.Find Friend status not updating on FindFriendScreen due to running on main thread

//6. Friend Request showing with currentUser profile in FriendRequest Screen of current user after sending friend request to other user

/*4.Error mapping document to chat (Ask Gemini)
java.lang.RuntimeException: Field 'lastMessageTime' is not a java.lang.Number
at com.google.firebase.firestore.DocumentSnapshot.castTypedValue(DocumentSnapshot.java:512)
at com.google.firebase.firestore.DocumentSnapshot.getTypedValue(DocumentSnapshot.java:504)
at com.google.firebase.firestore.DocumentSnapshot.getLong(DocumentSnapshot.java:373)
at com.ryen.bondhub.data.mappers.ChatMapper.mapDocumentToDomain(ChatMapper.kt:60)
at com.ryen.bondhub.data.repository.ChatRepositoryImpl$getUserChats$2$2.emit(ChatRepositoryImpl.kt:203)
at com.ryen.bondhub.data.repository.ChatRepositoryImpl$getUserChats$2$2.emit(ChatRepositoryImpl.kt:198)
at kotlinx.coroutines.flow.FlowKt__ErrorsKt$catchImpl$2.emit(Errors.kt:154)
at kotlinx.coroutines.flow.FlowKt__ChannelsKt.emitAllImpl$FlowKt__ChannelsKt(Channels.kt:33)
at kotlinx.coroutines.flow.FlowKt__ChannelsKt.access$emitAllImpl$FlowKt__ChannelsKt(Channels.kt:1)
at kotlinx.coroutines.flow.FlowKt__ChannelsKt$emitAllImpl$1.invokeSuspend(Unknown Source:14)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTaskKt.resume(DispatchedTask.kt:221)
at kotlinx.coroutines.DispatchedTaskKt.resumeUnconfined(DispatchedTask.kt:177)
at kotlinx.coroutines.DispatchedTaskKt.dispatch(DispatchedTask.kt:149)
at kotlinx.coroutines.CancellableContinuationImpl.dispatchResume(CancellableContinuationImpl.kt:470)
at kotlinx.coroutines.CancellableContinuationImpl.completeResume(CancellableContinuationImpl.kt:591)
at kotlinx.coroutines.channels.BufferedChannelKt.tryResume0(BufferedChannel.kt:2957)
at kotlinx.coroutines.channels.BufferedChannelKt.access$tryResume0(BufferedChannel.kt:1)
at kotlinx.coroutines.channels.BufferedChannel$BufferedChannelIterator.tryResumeHasNext(BufferedChannel.kt:1719)
at kotlinx.coroutines.channels.BufferedChannel.tryResumeReceiver(BufferedChannel.kt:662)
at kotlinx.coroutines.channels.BufferedChannel.updateCellSend(BufferedChannel.kt:478)
at kotlinx.coroutines.channels.BufferedChannel.access$updateCellSend(BufferedChannel.kt:33)
at kotlinx.coroutines.channels.BufferedChannel.trySend-JP2dKIU(BufferedChannel.kt:3360)
at kotlinx.coroutines.channels.ChannelCoroutine.trySend-JP2dKIU(Unknown Source:2)
at com.ryen.bondhub.data.remote.dataSource.ChatRemoteDataSource$getUserChats$1.invokeSuspend$lambda$1(ChatRemoteDataSource.kt:49)*/

//5.Sender picture not showing in message notification




