package com.ryen.bondhub.presentation.screens.userProfile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var displayName by remember { mutableStateOf(uiState.displayName) }
    var bio by remember { mutableStateOf(uiState.bio) }

    ProfileUpdateScreenContent(
        email = uiState.email,
        profilePictureUrl = uiState.profilePictureUrl,
        displayName = displayName,
        bio = bio,
        onDisplayNameChange = { displayName = it },
        onBioChange = { bio = it },
        onEditProfilePictureClick = { /* Handle profile picture edit */ },
        onSkip = onSkip,
        onSave = {
            //viewModel.onEvent(UserProfileEvent.UpdateProfile(displayName, bio))
            onDone()
        }
    )
}