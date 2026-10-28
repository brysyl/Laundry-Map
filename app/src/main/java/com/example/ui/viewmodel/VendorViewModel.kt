package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LaundromatRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.Laundromat
import com.example.domain.model.Order
import com.example.domain.model.OrderState
import com.example.domain.model.ProcessingStage
import com.example.domain.model.SampleLaundromats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VendorUiState(
    val selectedLaundromat: Laundromat = SampleLaundromats.KARU_CRYSTAL_WASH,
    val pendingOrderCountdownSeconds: Int = 120, // 2-minute countdown timer
    val isTimerActive: Boolean = true,
    val alertAudioTriggered: Boolean = false,
    val selectedProcessingOrder: Order? = null
)

class VendorViewModel(
    private val orderRepository: OrderRepository,
    private val laundromatRepository: LaundromatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorUiState())
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()

    val laundromatOrders: StateFlow<List<Order>> = orderRepository
        .getOrdersForLaundromat(SampleLaundromats.KARU_CRYSTAL_WASH.id)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var countdownJob: Job? = null

    init {
        startAcceptanceCountdown()
    }

    private fun startAcceptanceCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value.pendingOrderCountdownSeconds
                if (current > 0) {
                    _uiState.value = _uiState.value.copy(
                        pendingOrderCountdownSeconds = current - 1,
                        alertAudioTriggered = current <= 10 // urgent alert in last 10s
                    )
                } else {
                    // reset countdown for demonstration
                    _uiState.value = _uiState.value.copy(pendingOrderCountdownSeconds = 120)
                }
            }
        }
    }

    fun acceptIncomingOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderState(orderId, OrderState.DriverAssignedForPickup)
            _uiState.value = _uiState.value.copy(pendingOrderCountdownSeconds = 120)
        }
    }

    fun markOrderAtLaundromat(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderState(orderId, OrderState.AtLaundromat)
        }
    }

    fun advanceProcessing(orderId: String, currentStage: ProcessingStage) {
        viewModelScope.launch {
            val next = when (currentStage) {
                ProcessingStage.WASHING -> OrderState.Processing(ProcessingStage.DRYING)
                ProcessingStage.DRYING -> OrderState.Processing(ProcessingStage.IRONING)
                ProcessingStage.IRONING -> OrderState.Processing(ProcessingStage.READY_FOR_DISPATCH)
                ProcessingStage.READY_FOR_DISPATCH -> OrderState.ReadyForDelivery
            }
            orderRepository.updateOrderState(orderId, next)
        }
    }

    fun readyForCourierDispatch(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderState(orderId, OrderState.DriverAssignedForDropoff)
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val laundromatRepository: LaundromatRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VendorViewModel(orderRepository, laundromatRepository) as T
        }
    }
}
