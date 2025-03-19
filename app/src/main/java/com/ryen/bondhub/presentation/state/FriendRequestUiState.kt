package com.ryen.bondhub.presentation.state

import com.ryen.bondhub.domain.model.ChatConnection
import com.ryen.bondhub.domain.model.UserProfile

data class FriendRequest(
    val connection: ChatConnection,
    val senderProfile: UserProfile
)