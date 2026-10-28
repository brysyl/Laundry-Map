package com.example.data.repository

import com.example.data.local.dao.NotificationDao
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.AppNotification
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(
    private val notificationDao: NotificationDao
) {
    fun getNotificationsForRole(role: UserRole): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsForRole(role).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getUnreadCount(role: UserRole): Flow<Int> {
        return notificationDao.getUnreadCountForRole(role)
    }

    suspend fun saveNotification(notification: AppNotification) {
        notificationDao.insertNotification(NotificationEntity.fromDomain(notification))
    }

    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(role: UserRole) {
        notificationDao.markAllAsReadForRole(role)
    }
}
