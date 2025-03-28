package com.ryen.bondhub.presentation.screens.userProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.R
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.LogoutDialog
import com.ryen.bondhub.presentation.components.SnackBarState
import com.ryen.bondhub.presentation.components.UserProfileTopAppBar
import com.ryen.bondhub.presentation.contents.ProfileUpdateScreenContent
import com.ryen.bondhub.presentation.event.UiEvent


@Composable
fun ProfileUpdateScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onSkip: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier,
    showTopBar: Boolean,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val uiStateChange by viewModel.uiChangeState.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState = remember { mutableStateOf(SnackBarState.INITIAL) }

    LaunchedEffect(uiStateChange.isUpdateCompleted) {
        if (uiStateChange.isUpdateCompleted && uiStateChange.isInitialSetup) {
            onDone()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {}
                is UiEvent.ShowSnackbarSuccess -> {
                    snackbarState.value = SnackBarState.SUCCESS
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                }
                is UiEvent.ShowSnackbarError -> {
                    snackbarState.value = SnackBarState.ERROR
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }

                UiEvent.Logout -> {
                    onLogout()
                }
            }
        }
    }
    
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState, snackBarState = snackbarState.value) },
        topBar = {
            if(showTopBar){
                UserProfileTopAppBar(
                    onBackClick = onBackClick,
                    onLogoutClick = { showDialog = true }
                )
            }
        },
        content = { padding ->
            ProfileUpdateScreenContent(
                email = uiState.email,
                profilePictureUrl = uiState.profilePictureUrl,
                displayName = uiState.displayName,
                bio = uiState.bio,
                onDisplayNameChange = { viewModel.onDisplayNameChanged(it) },
                onBioChange = { viewModel.onBioChanged(it) },
                onEditProfilePictureClick = { imagePickerLauncher.launch("image/*") },
                onSkip = onSkip,
                onSave = {
                    viewModel.updateUserProfile()
                },
                context = context,
                isInitialSetup = uiStateChange.isInitialSetup,
                hasChanges = uiStateChange.hasChanges,
                screenState = screenState,
                padding = padding,
                content = {
                    if (showDialog && showTopBar) {
                        LogoutDialog(
                            onDismissRequest = { showDialog = false },
                            onConfirmation = {
                                viewModel.logout()
                                showDialog = false
                            },
                            dialogTitle = "Are you sure you want to logout?",
                            icon = (R.drawable.logout)
                        )
                    }
                }
            )
        }
    )
}