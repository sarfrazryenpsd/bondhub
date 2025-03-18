package com.ryen.bondhub.domain.model

import com.google.firebase.firestore.DocumentId

enum class ConnectionStatus {
    PENDING, ACCEPTED, BLOCKED, INITIAL
}

data class ChatConnection(
    @DocumentId
    val connectionId: String = "",
    val user1Id: String,
    val user2Id: String,
    val status: ConnectionStatus = ConnectionStatus.INITIAL,
    val initiatedAt: Long = System.currentTimeMillis(),
    val lastInteractedAt: Long = System.currentTimeMillis(),
    val initiatorId: String
)