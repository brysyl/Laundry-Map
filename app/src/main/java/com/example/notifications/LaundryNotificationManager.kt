package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.domain.model.AppNotification
import com.example.domain.model.NotificationType
import com.example.domain.model.UserRole

class LaundryNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Customer Orders Channel
            val customerChannel = NotificationChannel(
                CHANNEL_CUSTOMER_ORDERS,
                "Customer Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Status tracking and delivery updates for laundry requests"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            // 2. Vendor Hub Alerts Channel (Urgent 2-min alerts)
            val vendorChannel = NotificationChannel(
                CHANNEL_VENDOR_ALERTS,
                "Laundromat Incoming Orders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical 2-minute order countdown alerts for laundromat partners"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
            }

            // 3. Courier Dispatch Channel
            val courierChannel = NotificationChannel(
                CHANNEL_COURIER_DISPATCH,
                "Courier Dispatch & Assignments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Dual-leg pickup and return delivery notifications for riders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            systemManager.createNotificationChannels(listOf(customerChannel, vendorChannel, courierChannel))
        }
    }

    fun postSystemNotification(notification: AppNotification) {
        // Safe check for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val channelId = when (notification.targetRole) {
            UserRole.VENDOR -> CHANNEL_VENDOR_ALERTS
            UserRole.COURIER -> CHANNEL_COURIER_DISPATCH
            else -> CHANNEL_CUSTOMER_ORDERS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ORDER_ID", notification.orderId)
            putExtra("EXTRA_NOTIFICATION_ID", notification.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notifId = (notification.timestamp % 100000).toInt()
        try {
            notificationManager.notify(notifId, builder.build())
        } catch (e: SecurityException) {
            // Handled gracefully when permission is not active
        }
    }

    companion object {
        const val CHANNEL_CUSTOMER_ORDERS = "channel_customer_orders"
        const val CHANNEL_VENDOR_ALERTS = "channel_vendor_alerts"
        const val CHANNEL_COURIER_DISPATCH = "channel_courier_dispatch"
    }
}
