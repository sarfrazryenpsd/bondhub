package com.ryen.bondhub.presentation.screens.userProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.repository.AuthRepository
import com.ryen.bondhub.domain.useCases.auth.LogoutUseCase
import com.ryen.bondhub.domain.useCases.userProfile.CompleteProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.GetUserProfileUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import com.ryen.bondhub.presentation.event.UiEvent
import com.ryen.bondhub.presentation.state.UserProfileScreenState
import com.ryen.bondhub.presentation.state.UserProfileUiState
import com.ryen.bondhub.presentation.state.UserProfileUiChangeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
): ViewModel() {

    private val _screenState = MutableStateFlow<UserProfileScreenState>(UserProfileScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiChangeState = MutableStateFlow(UserProfileUiChangeState())
    val uiChangeState = _uiChangeState.asStateFlow()

    private val _dataSource = MutableStateFlow(DataSource.UNKNOWN)
    val dataSource = _dataSource.asStateFlow()



    fun setInitialSetupMode(isInitialSetup: Boolean) {
        _uiChangeState.update { it.copy(isInitialSetup = isInitialSetup) }
    }

    // After profile is loaded, store initial values
    private fun storeInitialValues() {
        _uiChangeState.update {
            it.copy(
                initialDisplayName = _uiState.value.displayName,
                initialBio = _uiState.value.bio,
                initialProfilePictureUrl = _uiState.value.profilePictureUrl
            )
        }
    }


    init {
        loadUserProfile()
    }



    fun onDisplayNameChanged(name: String) {
        _uiState.update { it.copy( displayName = name.trim()) }
        checkForChanges()
    }

    fun onBioChanged(bio: String) {
        _uiState.update { it.copy( bio = bio.trim()) }
        checkForChanges()
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(profilePictureUrl = uri.toString()) }
        checkForChanges()
    }

    private fun checkForChanges() {
        val current = _uiState.value
        val initial = _uiChangeState.value

        val hasChanges = listOf(
            current.displayName != initial.initialDisplayName,
            current.bio != initial.initialBio,
            current.profilePictureUrl != initial.initialProfilePictureUrl
        ).any { it }

        _uiChangeState.update { it.copy(hasChanges = hasChanges) }
    }

    private fun loadUserProfile(forceRefresh: Boolean = false){
        viewModelScope.launch {

            _screenState.value = UserProfileScreenState.Loading

            try{
                authRepository.getCurrentUser()!!.let { user ->
                    val userProfileResult = getUserProfileUseCase(user.uid)
                    userProfileResult.onSuccess { userProfile ->

                        _uiState.value = _uiState.value.copy(
                            email = userProfile.email,
                            displayName = userProfile.displayName,
                            profilePictureUrl = userProfile.profilePictureUrl,
                            uid = userProfile.uid,
                            bio = userProfile.bio
                        )

                        _dataSource.value = if (forceRefresh) DataSource.NETWORK else DataSource.CACHE

                        if (!userProfile.isProfileSetupComplete) {
                            completeProfileUseCase(userProfile)
                        }
                        _screenState.value = UserProfileScreenState.Success(userProfile)
                        storeInitialValues()
                    }.onFailure {
                        _screenState.value = UserProfileScreenState.Error(it.message ?: "Failed To Fetch Profile")
                        _uiEvent.emit(UiEvent.ShowSnackbarError(it.message ?: "Failed To Fetch Profile"))
                    }
                }
            } catch (e: Exception) {
                _screenState.value = UserProfileScreenState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbarError(e.message ?: "Unknown error"))
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile(forceRefresh = true)
    }

    private fun setUpdateCompleted(completed: Boolean) {
        _uiChangeState.update { it.copy(isUpdateCompleted = completed) }
    }

    fun updateUserProfile() {
        viewModelScope.launch {
            _screenState.value = UserProfileScreenState.Loading

            try {
                val userId = _uiState.value.uid
                val currentProfile = getUserProfileUseCase(userId).getOrNull()

                // Determine profile picture URLs
                val (pfp, pfpThumbnail) = if (_uiState.value.profilePictureUrl != null) {
                    // If a new URI is provided, check if it's different from existing
                    if (_uiState.value.profilePictureUrl != currentProfile?.profilePictureUrl) {
                        val imageResult = updateProfileImageUseCase(
                            userId,
                            _uiState.value.profilePictureUrl!!.toUri()
                        )
                        if (imageResult.isSuccess) {
                            val urls = imageResult.getOrThrow()
                            urls.mainUrl to urls.thumbnailUrl
                        } else {
                            throw imageResult.exceptionOrNull()
                                ?: Exception("Failed to upload image")
                        }
                    } else {
                        // Use existing URLs if no change
                        currentProfile?.profilePictureUrl to
                                currentProfile?.profilePictureThumbnailUrl
                    }
                } else {
                    // No new image provided
                    currentProfile?.profilePictureUrl to
                            currentProfile?.profilePictureThumbnailUrl
                }

                // Create user profile with only the fields needed for update
                val userProfile = UserProfile(
                    uid = userId,
                    profilePictureUrl = pfp,
                    profilePictureThumbnailUrl = pfpThumbnail,
                    displayName = _uiState.value.displayName,
                    bio = _uiState.value.bio,
                    email = _uiState.value.email // Needed for UserProfile constructor
                )

                // Update profile in database
                val updateResult = updateUserProfileUseCase(userProfile)

                if (updateResult.isSuccess) {
                    // Refresh the user profile after update
                    val updatedProfile = getUserProfileUseCase(userId).getOrNull()
                    _screenState.value = UserProfileScreenState.Success(updatedProfile ?: userProfile)
                    _uiEvent.emit(UiEvent.ShowSnackbarSuccess("Profile updated successfully"))
                    storeInitialValues()
                    setUpdateCompleted(true)
                } else {
                    _screenState.value = UserProfileScreenState.Error(
                        updateResult.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                    _uiEvent.emit(UiEvent.ShowSnackbarError("Failed to update profile"))
                }
            } catch (e: Exception) {
                _screenState.value = UserProfileScreenState.Error(e.message ?: "Unknown error")
                _uiEvent.emit(UiEvent.ShowSnackbarError(e.message ?: "Unknown error"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _screenState.value = UserProfileScreenState.Loading
            try {
                logoutUseCase().onSuccess {
                    // Emit logout event
                    _uiEvent.emit(UiEvent.Logout)
                }.onFailure { exception ->
                    // Emit error if logout fails
                    _uiEvent.emit(UiEvent.ShowSnackbarError(
                        exception.message ?: "Logout failed"
                    ))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbarError(
                    e.message ?: "Logout failed"
                ))
            }
        }
    }

}

enum class DataSource {
    CACHE, NETWORK, UNKNOWN
}