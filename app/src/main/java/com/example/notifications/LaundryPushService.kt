package com.example.notifications

import com.example.data.repository.NotificationRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.AppNotification
import com.example.domain.model.NotificationType
import com.example.domain.model.OrderState
import com.example.domain.model.ProcessingStage
import com.example.domain.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LaundryPushService(
    private val notificationManager: LaundryNotificationManager,
    private val notificationRepository: NotificationRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _inAppBannerAlert = MutableStateFlow<AppNotification?>(null)
    val inAppBannerAlert: StateFlow<AppNotification?> = _inAppBannerAlert.asStateFlow()

    private var bannerDismissJob: Job? = null

    init {
        scope.launch {
            seedInitialCorridorAlerts()
        }
    }

    fun dispatchPushEvent(
        targetRole: UserRole?,
        type: NotificationType,
        title: String,
        body: String,
        orderId: String? = null
    ) {
        val notification = AppNotification(
            id = "NOTIF_" + UUID.randomUUID().toString().take(8),
            targetRole = targetRole,
            type = type,
            title = title,
            body = body,
            orderId = orderId,
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            // 1. Save to Room database history
            notificationRepository.saveNotification(notification)

            // 2. Post OS Heads-up notification
            notificationManager.postSystemNotification(notification)

            // 3. Emit in-app banner alert with auto-dismiss
            _inAppBannerAlert.value = notification
            bannerDismissJob?.cancel()
            bannerDismissJob = launch {
                delay(4500L)
                if (_inAppBannerAlert.value?.id == notification.id) {
                    _inAppBannerAlert.value = null
                }
            }
        }
    }

    fun dismissBanner() {
        bannerDismissJob?.cancel()
        _inAppBannerAlert.value = null
    }

    /**
     * Helper to dispatch role-specific alerts when order lifecycle events occur.
     */
    fun onOrderLifecycleEvent(orderId: String, newState: OrderState, customerName: String) {
        when (newState) {
            is OrderState.Created -> {
                // Alert Vendor of new order with 2-minute timer
                dispatchPushEvent(
                    targetRole = UserRole.VENDOR,
                    type = NotificationType.NEW_ORDER_ALERT,
                    title = "🚨 New Incoming Order (#$orderId)!",
                    body = "2-Minute acceptance countdown started for Karu/Mararaba corridor.",
                    orderId = orderId
                )
            }
            is OrderState.DriverAssignedForPickup -> {
                // Alert Customer that courier is en route
                dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = NotificationType.COURIER_ASSIGNMENT,
                    title = "Courier En Route for Pickup",
                    body = "Rider Ibrahim Musa is on his way to collect your laundry.",
                    orderId = orderId
                )
                // Alert Courier
                dispatchPushEvent(
                    targetRole = UserRole.COURIER,
                    type = NotificationType.COURIER_ASSIGNMENT,
                    title = "New Leg 1 Dispatch Assignment",
                    body = "Pick up laundry from $customerName at Karu Site.",
                    orderId = orderId
                )
            }
            is OrderState.PickedUp -> {
                dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = NotificationType.ORDER_STATUS_UPDATE,
                    title = "Laundry Bag Secured",
                    body = "Courier has collected your items and is heading to the laundromat hub.",
                    orderId = orderId
                )
            }
            is OrderState.AtLaundromat -> {
                dispatchPushEvent(
                    targetRole = UserRole.VENDOR,
                    type = NotificationType.ORDER_STATUS_UPDATE,
                    title = "Laundry Arrived at Hub",
                    body = "Order #$orderId has arrived and is ready to load into washers.",
                    orderId = orderId
                )
            }
            is OrderState.Processing -> {
                val stageText = newState.stage.displayName
                dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = NotificationType.ORDER_STATUS_UPDATE,
                    title = "Laundry in Process: $stageText",
                    body = "Your garments are currently in $stageText at Karu Crystal Wash Hub.",
                    orderId = orderId
                )
            }
            is OrderState.DriverAssignedForDropoff -> {
                dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = NotificationType.ORDER_STATUS_UPDATE,
                    title = "Out for Final Delivery!",
                    body = "Your clean, pressed laundry is en route to your doorstep. Have your PIN ready.",
                    orderId = orderId
                )
                dispatchPushEvent(
                    targetRole = UserRole.COURIER,
                    type = NotificationType.COURIER_ASSIGNMENT,
                    title = "Leg 2 Return Delivery Assigned",
                    body = "Return sealed laundry bag to $customerName. Client PIN and signature required.",
                    orderId = orderId
                )
            }
            is OrderState.Delivered -> {
                dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = NotificationType.DELIVERY_VERIFICATION,
                    title = "Laundry Delivered & Verified 🎉",
                    body = "Handover confirmed with your digital sign-off. Thank you for using Laundry Map!",
                    orderId = orderId
                )
            }
            else -> {}
        }
    }

    private suspend fun seedInitialCorridorAlerts() {
        // Seed initial notifications for realistic first-launch demonstration
        dispatchPushEvent(
            targetRole = UserRole.CUSTOMER,
            type = NotificationType.SYSTEM_NOTICE,
            title = "Welcome to Laundry Map Nasarawa!",
            body = "Enjoy rapid same-day laundry pickups across Karu, Mararaba & Nyanya."
        )
        dispatchPushEvent(
            targetRole = UserRole.VENDOR,
            type = NotificationType.SYSTEM_NOTICE,
            title = "Pilot Hub Online",
            body = "Karu Crystal Wash Hub is connected to the real-time order dispatch stream."
        )
        dispatchPushEvent(
            targetRole = UserRole.COURIER,
            type = NotificationType.SYSTEM_NOTICE,
            title = "Transporter Telemetry Active",
            body = "GPS corridor tracking enabled. Stand by for dual-leg dispatches."
        )
    }
}
