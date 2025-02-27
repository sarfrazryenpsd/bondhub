package com.ryen.bondhub.presentation.screens.userProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.ProfileUpdateScreenContent


@Composable
fun ProfileUpdateScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

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
            //viewModel.onEvent(UserProfileEvent.UpdateProfile(displayName, bio))
            onDone()
        },
        context = context,
    )
}