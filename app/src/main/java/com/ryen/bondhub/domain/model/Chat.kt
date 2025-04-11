package com.ryen.bondhub.domain.model

data class Chat(
    val chatId: String = "",
    val connectionId: String = "",
    val participants: List<String> = emptyList(),
    val profilePictureUrlThumbnail: String = "",
    val displayName: String = "",
    val lastMessage: String? = null,
    val lastMessageTime: Long = 0,
    val unreadMessageCount: Int = 0,
)