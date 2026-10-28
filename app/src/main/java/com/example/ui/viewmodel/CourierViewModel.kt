package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.dto.TelemetryUpdate
import com.example.data.remote.websocket.WebSocketCourierClient
import com.example.data.repository.OrderRepository
import com.example.domain.model.Courier
import com.example.domain.model.DispatchLeg
import com.example.domain.model.Order
import com.example.domain.model.OrderState
import com.example.domain.model.SampleCouriers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CourierUiState(
    val courier: Courier = SampleCouriers.IBRAHIM_MUSA,
    val isAvailable: Boolean = true,
    val navigationActive: Boolean = false,
    val navigationSpeedKmh: Float = 36.5f,
    val remainingDistanceKm: Double = 1.8,
    val nextTurnInstruction: String = "Turn right onto Keffi-Abuja Expressway in 400m",
    val isVerificationModalOpen: Boolean = false,
    val selectedDeliveryOrder: Order? = null,
    val enteredOtp: String = "",
    val otpError: String? = null,
    val signatureSaved: Boolean = false,
    val isDeliverySuccess: Boolean = false,
    val currentTelemetry: TelemetryUpdate? = null
)

class CourierViewModel(
    private val orderRepository: OrderRepository,
    private val webSocketClient: WebSocketCourierClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourierUiState())
    val uiState: StateFlow<CourierUiState> = _uiState.asStateFlow()

    val assignedOrders: StateFlow<List<Order>> = orderRepository
        .getOrdersForCourier(SampleCouriers.IBRAHIM_MUSA.id)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            webSocketClient.telemetryUpdates.collect { update ->
                _uiState.value = _uiState.value.copy(
                    currentTelemetry = update,
                    navigationSpeedKmh = update.speedKmh
                )
            }
        }
    }

    fun toggleAvailability() {
        val next = !_uiState.value.isAvailable
        _uiState.value = _uiState.value.copy(isAvailable = next)
    }

    fun toggleNavigationHud() {
        _uiState.value = _uiState.value.copy(navigationActive = !_uiState.value.navigationActive)
    }

    fun pickUpFromCustomer(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderState(orderId, OrderState.PickedUp)
        }
    }

    fun deliverToLaundromat(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderState(orderId, OrderState.AtLaundromat)
        }
    }

    fun openDeliveryVerification(order: Order) {
        _uiState.value = _uiState.value.copy(
            isVerificationModalOpen = true,
            selectedDeliveryOrder = order,
            enteredOtp = "",
            otpError = null,
            signatureSaved = false,
            isDeliverySuccess = false
        )
    }

    fun closeDeliveryVerification() {
        _uiState.value = _uiState.value.copy(
            isVerificationModalOpen = false,
            selectedDeliveryOrder = null,
            otpError = null
        )
    }

    fun setEnteredOtp(otp: String) {
        if (otp.length <= 4) {
            _uiState.value = _uiState.value.copy(enteredOtp = otp, otpError = null)
        }
    }

    fun confirmDeliveryWithSignature(signatureSvgOrPath: String) {
        val order = _uiState.value.selectedDeliveryOrder ?: return
        val otp = _uiState.value.enteredOtp

        viewModelScope.launch {
            val success = orderRepository.completeDelivery(
                orderId = order.id,
                signaturePath = signatureSvgOrPath,
                enteredOtp = otp
            )

            if (success) {
                _uiState.value = _uiState.value.copy(
                    isDeliverySuccess = true,
                    otpError = null,
                    signatureSaved = true
                )
                kotlinx.coroutines.delay(1200L)
                closeDeliveryVerification()
            } else {
                _uiState.value = _uiState.value.copy(
                    otpError = "Invalid 4-digit client delivery PIN. Check with client."
                )
            }
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val webSocketClient: WebSocketCourierClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CourierViewModel(orderRepository, webSocketClient) as T
        }
    }
}
