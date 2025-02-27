package com.ryen.bondhub.presentation.screens.userProfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryen.bondhub.presentation.components.ProfileUpdateScreenContent


@Composable
fun ProfileUpdateScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val screenState by viewModel.screenState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    ProfileUpdateScreenContent(
        email = uiState.email,
        profilePictureUrl = uiState.profilePictureUrl,
        displayName = uiState.displayName,
        bio = uiState.bio,
        onDisplayNameChange = { viewModel.onDisplayNameChanged(it) },
        onBioChange = { viewModel.onBioChanged(it) },
        onEditProfilePictureClick = { /* Handle profile picture edit */ },
        onSkip = onSkip,
        onSave = {
            //viewModel.onEvent(UserProfileEvent.UpdateProfile(displayName, bio))
            onDone()
        }
    )
}