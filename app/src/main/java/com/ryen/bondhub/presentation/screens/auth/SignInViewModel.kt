package com.ryen.bondhub.presentation.screens.auth

import androidx.lifecycle.ViewModel
import com.ryen.bondhub.presentation.state.SignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


//@HiltViewModel
class SignInViewModel: ViewModel() {
    private val _state: MutableStateFlow<SignInState> = MutableStateFlow(SignInState.Initial)
    val state = _state.asStateFlow()
}