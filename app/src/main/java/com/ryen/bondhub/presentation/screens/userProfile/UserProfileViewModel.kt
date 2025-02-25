package com.ryen.bondhub.presentation.screens.userProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.userProfile.CompleteProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import com.ryen.bondhub.presentation.state.UserProfileScreenState
import com.ryen.bondhub.presentation.state.UserProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val getProfileImageUseCase: GetUserProfileUseCase,
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

        viewModelScope.launch(Dispatchers.IO) {
            try{
                authRepository.getCurrentUser()!!.let { user ->
                    val userProfileResult = getProfileImageUseCase(user.uid)
                    userProfileResult.onSuccess { userProfile ->

                        _userProfileUiState.value = _userProfileUiState.value.copy(
                            email = userProfile.email,
                            displayName = userProfile.displayName,
                        )

                        if (!userProfile.isProfileSetupComplete) {
                            completeProfileUseCase(userProfile)
                        }
                        _userProfileScreenState.value = UserProfileScreenState.Success(userProfile)
                    }.onFailure {
                        _userProfileScreenState.value = UserProfileScreenState.Error(it.message ?: "Failed To Fetch Profile")
                    }
                }
            } catch (e: Exception) {
                _userProfileScreenState.value = UserProfileScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }

}