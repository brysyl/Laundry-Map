package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.dto.TelemetryUpdate
import com.example.data.remote.websocket.WebSocketCourierClient
import com.example.data.repository.LaundromatRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.GeoPoint
import com.example.domain.model.LaundryCatalog
import com.example.domain.model.LaundryItem
import com.example.domain.model.Order
import com.example.domain.model.OrderItem
import com.example.domain.model.PaymentProvider
import com.example.domain.model.PaymentStatus
import com.example.domain.model.PilotLocations
import com.example.domain.model.SampleLaundromats
import com.example.domain.model.ServiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerUiState(
    val selectedService: ServiceType = ServiceType.WASH_AND_FOLD,
    val basket: Map<String, Int> = mapOf(
        LaundryCatalog.ITEMS[0].id to 3, // 3 shirts
        LaundryCatalog.ITEMS[1].id to 2  // 2 trousers
    ),
    val itemNotes: Map<String, String> = emptyMap(),
    val selectedLocation: GeoPoint = PilotLocations.KARU_SITE,
    val addressDetails: String = "Block 4 Flat 2, Karu Site Phase 1",
    val customerName: String = "Amina Bello",
    val customerPhone: String = "+234 802 443 8910",
    val specialInstructions: String = "Handle white shirts with fabric bleach care",
    val isCheckoutSheetOpen: Boolean = false,
    val isPaymentProcessing: Boolean = false,
    val paymentSuccessOrderId: String? = null,
    val activeTrackedOrderId: String? = null,
    val latestCourierTelemetry: TelemetryUpdate? = null
) {
    val totalItemsCount: Int
        get() = basket.values.sum()

    val subtotalNgn: Int
        get() = basket.entries.sumOf { (itemId, qty) ->
            val item = LaundryCatalog.ITEMS.find { it.id == itemId }
            val unitPrice = item?.basePriceNgn ?: 0
            (unitPrice * qty * selectedService.expressMultiplier).toInt()
        }

    val deliveryFeeNgn: Int
        get() {
            val dist = PilotLocations.calculateDistanceKm(
                selectedLocation,
                SampleLaundromats.KARU_CRYSTAL_WASH.location
            )
            return (1200 + (dist * 200).toInt()).coerceIn(1200, 3500)
        }

    val totalNgn: Int
        get() = subtotalNgn + deliveryFeeNgn
}

class CustomerViewModel(
    private val orderRepository: OrderRepository,
    private val laundromatRepository: LaundromatRepository,
    private val webSocketClient: WebSocketCourierClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    val activeOrders: StateFlow<List<Order>> = orderRepository.activeOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Collect real-time telemetry from WebSocket client
        viewModelScope.launch {
            webSocketClient.telemetryUpdates.collect { update ->
                _uiState.value = _uiState.value.copy(latestCourierTelemetry = update)
            }
        }
    }

    fun selectService(service: ServiceType) {
        _uiState.value = _uiState.value.copy(selectedService = service)
    }

    fun selectLocation(location: GeoPoint) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
    }

    fun updateAddressDetails(details: String) {
        _uiState.value = _uiState.value.copy(addressDetails = details)
    }

    fun updateSpecialInstructions(instructions: String) {
        _uiState.value = _uiState.value.copy(specialInstructions = instructions)
    }

    fun updateItemQuantity(itemId: String, delta: Int) {
        val current = _uiState.value.basket[itemId] ?: 0
        val updated = (current + delta).coerceAtLeast(0)
        val newBasket = _uiState.value.basket.toMutableMap()
        if (updated > 0) {
            newBasket[itemId] = updated
        } else {
            newBasket.remove(itemId)
        }
        _uiState.value = _uiState.value.copy(basket = newBasket)
    }

    fun setItemNotes(itemId: String, notes: String) {
        val newNotes = _uiState.value.itemNotes.toMutableMap()
        newNotes[itemId] = notes
        _uiState.value = _uiState.value.copy(itemNotes = newNotes)
    }

    fun openCheckoutSheet() {
        _uiState.value = _uiState.value.copy(isCheckoutSheetOpen = true)
    }

    fun closeCheckoutSheet() {
        _uiState.value = _uiState.value.copy(isCheckoutSheetOpen = false, isPaymentProcessing = false)
    }

    fun selectOrderForTracking(orderId: String?) {
        _uiState.value = _uiState.value.copy(activeTrackedOrderId = orderId)
    }

    fun processPaystackCheckout(provider: PaymentProvider = PaymentProvider.PAYSTACK) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPaymentProcessing = true)

            // Simulate Paystack/Flutterwave gateway handshake and PIN prompt
            kotlinx.coroutines.delay(1800L)

            val state = _uiState.value
            val orderItems = state.basket.mapNotNull { (itemId, qty) ->
                val item = LaundryCatalog.ITEMS.find { it.id == itemId }
                item?.let {
                    OrderItem(
                        item = it,
                        quantity = qty,
                        customNotes = state.itemNotes[itemId] ?: it.defaultNotes
                    )
                }
            }

            val created = orderRepository.createOrder(
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                location = state.selectedLocation,
                addressDetail = state.addressDetails,
                serviceType = state.selectedService,
                items = orderItems,
                laundromatId = SampleLaundromats.KARU_CRYSTAL_WASH.id,
                specialInstructions = state.specialInstructions,
                paymentProvider = provider,
                paymentStatus = PaymentStatus.PAID
            )

            // Start WebSocket corridor telemetry for this order
            webSocketClient.startCorridorTelemetryEngine(
                courierId = "cr_ibrahim_01",
                orderId = created.id,
                from = state.selectedLocation,
                to = created.laundromat.location
            )

            _uiState.value = _uiState.value.copy(
                isPaymentProcessing = false,
                isCheckoutSheetOpen = false,
                paymentSuccessOrderId = created.id,
                activeTrackedOrderId = created.id,
                basket = emptyMap() // clear basket after order placement
            )
        }
    }

    class Factory(
        private val orderRepository: OrderRepository,
        private val laundromatRepository: LaundromatRepository,
        private val webSocketClient: WebSocketCourierClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomerViewModel(orderRepository, laundromatRepository, webSocketClient) as T
        }
    }
}
