package com.ryen.bondhub.presentation.screens.userProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.userProfile.CompleteProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import com.ryen.bondhub.presentation.state.UserProfileScreenState
import com.ryen.bondhub.presentation.state.UserProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val authRepository: AuthRepository,
): ViewModel() {

    private val _screenState = MutableStateFlow<UserProfileScreenState>(UserProfileScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun onDisplayNameChanged(name: String) {
        _uiState.update { it.copy( displayName = name.trim()) }
    }

    fun onBioChanged(bio: String) {
        _uiState.update { it.copy( bio = bio.trim()) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy( profilePictureUrl = uri.toString()) }
    }

    private fun loadUserProfile(){
        viewModelScope.launch {
            _screenState.value = UserProfileScreenState.Loading

            try{
                authRepository.getCurrentUser()!!.let { user ->
                    val userProfileResult = getUserProfileUseCase(user.uid)
                    userProfileResult.onSuccess { userProfile ->

                        _uiState.value = _uiState.value.copy(
                            email = userProfile.email,
                            displayName = userProfile.displayName,
                        )

                        if (!userProfile.isProfileSetupComplete) {
                            completeProfileUseCase(userProfile)
                        }
                        _screenState.value = UserProfileScreenState.Success(userProfile)
                    }.onFailure {
                        _screenState.value = UserProfileScreenState.Error(it.message ?: "Failed To Fetch Profile")
                    }
                }
            } catch (e: Exception) {
                _screenState.value = UserProfileScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateUserProfile(userProfile: UserProfile){
        viewModelScope.launch {

        }

    }

}