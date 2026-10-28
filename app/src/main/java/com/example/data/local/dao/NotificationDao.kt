package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE targetRole = :role OR targetRole IS NULL ORDER BY timestamp DESC")
    fun getNotificationsForRole(role: UserRole): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (targetRole = :role OR targetRole IS NULL) AND isRead = 0")
    fun getUnreadCountForRole(role: UserRole): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetRole = :role OR targetRole IS NULL")
    suspend fun markAllAsReadForRole(role: UserRole)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}
