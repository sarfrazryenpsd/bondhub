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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.CustomSnackbar
import com.ryen.bondhub.presentation.components.ProfileUpdateScreenContent
import com.ryen.bondhub.presentation.event.UiEvent


@Composable
fun ProfileUpdateScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onSkip: () -> Unit = {},
    isInitialSetup: Boolean = true
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val initialValues = remember {
        mutableStateOf(uiState)
    }

    // Determine if any changes have been made
    val hasChanges = remember(uiState, initialValues.value) {
        uiState.displayName != initialValues.value.displayName ||
                uiState.bio != initialValues.value.bio ||
                uiState.profilePictureUrl != initialValues.value.profilePictureUrl
    }

    var isUpdateCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {}
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is UiEvent.ProfileUpdateCompleted -> {
                    isUpdateCompleted = true
                    if (isInitialSetup) {
                        onDone()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { CustomSnackbar(snackbarHostState) },
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
                isInitialSetup = isInitialSetup,
                hasChanges = hasChanges,
                screenState = screenState,
                padding = padding
            )
        }
    )
}