package com.ryen.bondhub.presentation.screens.userProfile

import androidx.lifecycle.ViewModel
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.userProfile.CompleteProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import com.ryen.bondhub.presentation.state.UserProfileScreenState
import com.ryen.bondhub.presentation.state.UserProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val completeProfileImageUseCase: CompleteProfileImageUseCase,
    private val authRepository: AuthRepository,
): ViewModel() {

    private val _userProfileScreenState = MutableStateFlow<UserProfileScreenState>(UserProfileScreenState.Initial)
    val userProfileState = _userProfileScreenState.asStateFlow()

    private val _userProfileUiState = MutableStateFlow(UserProfileUiState())
    val userProfile = _userProfileUiState.asStateFlow()

    init {
        // Fetch the user's profile data from the repository
        // You can use the authRepository to get the user's UID
        // Initialize the UI state with the user's profile data
        // Update isProfileSetupComplete to true once user sign up
    }

}