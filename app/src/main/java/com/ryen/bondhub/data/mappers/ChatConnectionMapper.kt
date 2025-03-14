package com.ryen.bondhub.data.mappers

import com.ryen.bondhub.data.local.entity.ChatConnectionEntity
import com.ryen.bondhub.domain.model.ChatConnection

private fun ChatConnection.toEntity(): ChatConnectionEntity {
    return ChatConnectionEntity(
        connectionId = connectionId,
        user1Id = user1Id,
        user2Id = user2Id,
        status = status,
        initiatedAt = initiatedAt,
        lastInteractedAt = lastInteractedAt,
        initiatorId = initiatorId
    )
}

private fun ChatConnectionEntity.toDomain(): ChatConnection {
    return ChatConnection(
        connectionId = connectionId,
        user1Id = user1Id,
        user2Id = user2Id,
        status = status,
        initiatedAt = initiatedAt,
        lastInteractedAt = lastInteractedAt,
        initiatorId = initiatorId
    )
}