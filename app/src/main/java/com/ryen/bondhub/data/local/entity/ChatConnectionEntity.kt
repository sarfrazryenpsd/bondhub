package com.ryen.bondhub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ryen.bondhub.domain.model.ConnectionStatus

@Entity(tableName = "chat_connections")
@TypeConverters(ConnectionStatusConverter::class)
data class ChatConnectionEntity(
    @PrimaryKey
    val connectionId: String,
    val user1Id: String,
    val user2Id: String,
    val status: ConnectionStatus,
    val initiatedAt: Long,
    val lastInteractedAt: Long,
    val initiatorId: String
)

// Type Converter for ConnectionStatus
class ConnectionStatusConverter {
    @androidx.room.TypeConverter
    fun fromConnectionStatus(status: ConnectionStatus): String {
        return status.name
    }

    @androidx.room.TypeConverter
    fun toConnectionStatus(statusString: String): ConnectionStatus {
        return ConnectionStatus.valueOf(statusString)
    }
}