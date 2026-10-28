package com.example.domain.model

enum class NotificationType {
    ORDER_STATUS_UPDATE,
    NEW_ORDER_ALERT,
    COURIER_ASSIGNMENT,
    DELIVERY_VERIFICATION,
    PAYMENT_SUCCESS,
    SYSTEM_NOTICE
}

data class AppNotification(
    val id: String,
    val targetRole: UserRole?, // null means broadcast to all
    val type: NotificationType,
    val title: String,
    val body: String,
    val orderId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionPayload: String? = null
)
