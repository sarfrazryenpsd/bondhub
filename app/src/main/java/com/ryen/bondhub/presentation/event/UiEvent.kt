package com.ryen.bondhub.presentation.event

sealed class UiEvent {
    data object NavigateToChat : UiEvent()
}