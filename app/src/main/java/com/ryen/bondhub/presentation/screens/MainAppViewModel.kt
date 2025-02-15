package com.ryen.bondhub.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryen.bondhub.domain.useCases.auth.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainAppViewModel @Inject constructor(
    private val getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { isAuthenticated ->
                _startDestination.value = if (isAuthenticated) {
                    Screen.ChatScreen
                } else {
                    Screen.AuthScreen
                }
            }
        }
    }
}