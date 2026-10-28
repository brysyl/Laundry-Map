package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AppNotification
import com.example.domain.model.NotificationType
import com.example.domain.model.UserRole

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetRole: UserRole?,
    val type: NotificationType,
    val title: String,
    val body: String,
    val orderId: String?,
    val timestamp: Long,
    val isRead: Boolean,
    val actionPayload: String?
) {
    fun toDomain(): AppNotification {
        return AppNotification(
            id = id,
            targetRole = targetRole,
            type = type,
            title = title,
            body = body,
            orderId = orderId,
            timestamp = timestamp,
            isRead = isRead,
            actionPayload = actionPayload
        )
    }

    companion object {
        fun fromDomain(notification: AppNotification): NotificationEntity {
            return NotificationEntity(
                id = notification.id,
                targetRole = notification.targetRole,
                type = notification.type,
                title = notification.title,
                body = notification.body,
                orderId = notification.orderId,
                timestamp = notification.timestamp,
                isRead = notification.isRead,
                actionPayload = notification.actionPayload
            )
        }
    }
}
