package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.NotificationRepository
import com.example.domain.model.AppNotification
import com.example.domain.model.NotificationType
import com.example.domain.model.UserRole
import com.example.notifications.LaundryPushService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val pushService: LaundryPushService
) : ViewModel() {

    private val _activeRole = MutableStateFlow(UserRole.CUSTOMER)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

    private val _isCenterOpen = MutableStateFlow(false)
    val isCenterOpen: StateFlow<Boolean> = _isCenterOpen.asStateFlow()

    val inAppBannerAlert: StateFlow<AppNotification?> = pushService.inAppBannerAlert

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<AppNotification>> = _activeRole.flatMapLatest { role ->
        notificationRepository.getNotificationsForRole(role)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadCount: StateFlow<Int> = _activeRole.flatMapLatest { role ->
        notificationRepository.getUnreadCount(role)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setActiveRole(role: UserRole) {
        _activeRole.value = role
    }

    fun openNotificationCenter() {
        _isCenterOpen.value = true
    }

    fun closeNotificationCenter() {
        _isCenterOpen.value = false
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(_activeRole.value)
        }
    }

    fun dismissBanner() {
        pushService.dismissBanner()
    }

    fun triggerSimulatedPush(type: NotificationType) {
        val role = _activeRole.value
        when (role) {
            UserRole.CUSTOMER -> {
                pushService.dispatchPushEvent(
                    targetRole = UserRole.CUSTOMER,
                    type = type,
                    title = "Laundry Transit Update",
                    body = "Courier Ibrahim Musa is 4 mins away on Keffi-Abuja Expressway with your clean laundry.",
                    orderId = "LM-7291"
                )
            }
            UserRole.VENDOR -> {
                pushService.dispatchPushEvent(
                    targetRole = UserRole.VENDOR,
                    type = NotificationType.NEW_ORDER_ALERT,
                    title = "🚨 Urgent: New Order #LM-8924",
                    body = "New Karu order received. 2-minute confirmation timer running!",
                    orderId = "LM-8924"
                )
            }
            UserRole.COURIER -> {
                pushService.dispatchPushEvent(
                    targetRole = UserRole.COURIER,
                    type = NotificationType.COURIER_ASSIGNMENT,
                    title = "⚡ Leg 1 Dispatch Assigned",
                    body = "Collect 4 items from Karu Site Phase 1 and transport to Crystal Wash Hub.",
                    orderId = "LM-7291"
                )
            }
        }
    }

    class Factory(
        private val notificationRepository: NotificationRepository,
        private val pushService: LaundryPushService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(notificationRepository, pushService) as T
        }
    }
}
