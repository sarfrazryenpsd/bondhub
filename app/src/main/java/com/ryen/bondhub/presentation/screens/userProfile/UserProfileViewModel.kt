package com.ryen.bondhub.presentation.screens.userProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.model.UserProfile
import com.ryen.bondhub.domain.useCases.userProfile.UpdateProfileImageUseCase
import com.ryen.bondhub.domain.useCases.userProfile.UpdateUserProfileUseCase
import com.ryen.bondhub.presentation.state.UserProfileScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase
): ViewModel() {

    private val _userProfileScreenState = MutableStateFlow<UserProfileScreenState>(UserProfileScreenState.Initial)
    val userProfileState = _userProfileScreenState.asStateFlow()

    fun updateProfile(userProfile: UserProfile, imageUri: Uri?) {
        viewModelScope.launch {
            if(imageUri != null) {

            }
        }

    }

}