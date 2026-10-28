package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.remote.websocket.WebSocketCourierClient
import com.example.data.repository.AuthRepository
import com.example.data.repository.LaundromatRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.OfflineSyncManager
import com.example.data.repository.OrderRepository
import com.example.data.security.SessionManager
import com.example.notifications.LaundryNotificationManager
import com.example.notifications.LaundryPushService

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)

    val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            sessionManager = sessionManager
        )
    }

    val notificationManager: LaundryNotificationManager by lazy {
        LaundryNotificationManager(context)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(database.notificationDao())
    }

    val pushService: LaundryPushService by lazy {
        LaundryPushService(
            notificationManager = notificationManager,
            notificationRepository = notificationRepository
        )
    }

    val offlineSyncManager: OfflineSyncManager by lazy {
        OfflineSyncManager(database.offlineQueueDao())
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(
            orderDao = database.orderDao(),
            offlineQueueDao = database.offlineQueueDao()
        ).apply {
            attachPushService(pushService)
        }
    }

    val laundromatRepository: LaundromatRepository by lazy {
        LaundromatRepository()
    }

    val webSocketCourierClient: WebSocketCourierClient by lazy {
        WebSocketCourierClient().apply {
            connect()
        }
    }
}
