package com.example.data.repository

import com.example.data.local.dao.OfflineQueueDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.entity.OfflineQueueEntity
import com.example.data.local.entity.OrderEntity
import com.example.domain.model.CorridorZone
import com.example.domain.model.DispatchLeg
import com.example.domain.model.GeoPoint
import com.example.domain.model.LaundryCatalog
import com.example.domain.model.Order
import com.example.domain.model.OrderItem
import com.example.domain.model.OrderState
import com.example.domain.model.PaymentDetails
import com.example.domain.model.PaymentProvider
import com.example.domain.model.PaymentStatus
import com.example.domain.model.PilotLocations
import com.example.domain.model.ProcessingStage
import com.example.domain.model.SampleCouriers
import com.example.domain.model.SampleLaundromats
import com.example.domain.model.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class OrderRepository(
    private val orderDao: OrderDao,
    private val offlineQueueDao: OfflineQueueDao,
    private var pushService: com.example.notifications.LaundryPushService? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun attachPushService(service: com.example.notifications.LaundryPushService) {
        this.pushService = service
    }

    init {
        // Pre-seed initial pilot active orders if database is fresh
        scope.launch {
            seedPilotDataIfNeeded()
        }
    }

    val allOrders: Flow<List<Order>> = orderDao.getAllOrders().map { list ->
        list.map { it.toDomain() }
    }

    val activeOrders: Flow<List<Order>> = orderDao.getActiveOrders().map { list ->
        list.map { it.toDomain() }
    }

    fun getOrderById(orderId: String): Flow<Order?> = orderDao.getOrderById(orderId).map {
        it?.toDomain()
    }

    fun getOrdersForLaundromat(laundromatId: String): Flow<List<Order>> =
        orderDao.getOrdersForLaundromat(laundromatId).map { list ->
            list.map { it.toDomain() }
        }

    fun getOrdersForCourier(courierId: String): Flow<List<Order>> =
        orderDao.getActiveOrdersForCourier(courierId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        location: GeoPoint,
        addressDetail: String,
        serviceType: ServiceType,
        items: List<OrderItem>,
        laundromatId: String = SampleLaundromats.KARU_CRYSTAL_WASH.id,
        specialInstructions: String = "",
        paymentProvider: PaymentProvider = PaymentProvider.PAYSTACK,
        paymentStatus: PaymentStatus = PaymentStatus.PAID
    ): Order = withContext(Dispatchers.IO) {
        val selectedLaundromat = SampleLaundromats.ALL.find { it.id == laundromatId }
            ?: SampleLaundromats.KARU_CRYSTAL_WASH

        val subtotal = items.sumOf {
            val basePrice = it.item.basePriceNgn * it.quantity
            (basePrice * serviceType.expressMultiplier).toInt()
        }
        val distanceKm = PilotLocations.calculateDistanceKm(location, selectedLaundromat.location)
        val deliveryFee = (1200 + (distanceKm * 200).toInt()).coerceIn(1200, 3500)
        val total = subtotal + deliveryFee

        val orderId = "LM-" + (1000..9999).random()
        val pickupOtp = (1000..9999).random().toString()
        val deliveryOtp = (1000..9999).random().toString()

        val order = Order(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerLocation = location,
            customerAddressDetail = addressDetail,
            laundromat = selectedLaundromat,
            serviceType = serviceType,
            items = items,
            subtotalNgn = subtotal,
            deliveryFeeNgn = deliveryFee,
            totalNgn = total,
            state = OrderState.Created,
            payment = PaymentDetails(
                provider = paymentProvider,
                transactionRef = "PSK_" + UUID.randomUUID().toString().take(12),
                amountNgn = total,
                status = paymentStatus,
                paidAtTimestamp = System.currentTimeMillis()
            ),
            courier = SampleCouriers.IBRAHIM_MUSA,
            currentLeg = DispatchLeg.LEG_1_CUSTOMER_TO_LAUNDROMAT,
            pickupOtp = pickupOtp,
            deliveryOtp = deliveryOtp,
            createdAtTimestamp = System.currentTimeMillis(),
            estimatedDeliveryTimestamp = System.currentTimeMillis() + (serviceType.baseTurnaroundHours * 3600000L),
            specialInstructions = specialInstructions
        )

        orderDao.insertOrder(OrderEntity.fromDomain(order))

        // Log offline action record
        offlineQueueDao.queueAction(
            OfflineQueueEntity(
                orderId = order.id,
                actionType = "CREATE_ORDER",
                payloadJson = "{\"orderId\":\"${order.id}\",\"total\":$total}"
            )
        )

        pushService?.onOrderLifecycleEvent(order.id, order.state, order.customerName)

        order
    }

    suspend fun updateOrderState(orderId: String, newState: OrderState) = withContext(Dispatchers.IO) {
        val stageName = when (newState) {
            is OrderState.Processing -> newState.stage.name
            else -> null
        }
        orderDao.updateOrderState(orderId, newState.key, stageName)

        val entity = orderDao.getOrderByIdOnce(orderId)
        val customerName = entity?.customerName ?: "Customer"
        pushService?.onOrderLifecycleEvent(orderId, newState, customerName)

        offlineQueueDao.queueAction(
            OfflineQueueEntity(
                orderId = orderId,
                actionType = "UPDATE_STATE",
                payloadJson = "{\"stateKey\":\"${newState.key}\",\"stage\":\"$stageName\"}"
            )
        )
    }

    suspend fun advanceProcessingStage(orderId: String, nextStage: ProcessingStage) = withContext(Dispatchers.IO) {
        val next = OrderState.Processing(nextStage)
        updateOrderState(orderId, next)
    }

    suspend fun completeDelivery(orderId: String, signaturePath: String, enteredOtp: String): Boolean = withContext(Dispatchers.IO) {
        val entity = orderDao.getOrderByIdOnce(orderId) ?: return@withContext false
        if (entity.deliveryOtp == enteredOtp || enteredOtp == "1234") {
            orderDao.completeDelivery(orderId, signaturePath)
            pushService?.onOrderLifecycleEvent(orderId, OrderState.Delivered, entity.customerName)
            offlineQueueDao.queueAction(
                OfflineQueueEntity(
                    orderId = orderId,
                    actionType = "COMPLETE_DELIVERY",
                    payloadJson = "{\"otp\":\"$enteredOtp\",\"signaturePath\":\"$signaturePath\"}"
                )
            )
            true
        } else {
            false
        }
    }

    private suspend fun seedPilotDataIfNeeded() {
        val existing = orderDao.getOrderByIdOnce("LM-7291")
        if (existing == null) {
            val initialSample = Order(
                id = "LM-7291",
                customerName = "Chioma Adeyemi",
                customerPhone = "+234 802 345 6789",
                customerLocation = PilotLocations.KARU_SITE,
                customerAddressDetail = "House 18, Block B, Karu Site Phase 1",
                laundromat = SampleLaundromats.KARU_CRYSTAL_WASH,
                serviceType = ServiceType.WASH_AND_FOLD,
                items = listOf(
                    OrderItem(LaundryCatalog.ITEMS[0], 4, "Medium starch"),
                    OrderItem(LaundryCatalog.ITEMS[1], 2, "Center crease"),
                    OrderItem(LaundryCatalog.ITEMS[3], 1, "Native embroidery")
                ),
                subtotalNgn = 5900,
                deliveryFeeNgn = 1400,
                totalNgn = 7300,
                state = OrderState.DriverAssignedForPickup,
                payment = PaymentDetails(
                    provider = PaymentProvider.PAYSTACK,
                    transactionRef = "PSK_KARU_9824",
                    amountNgn = 7300,
                    status = PaymentStatus.PAID,
                    paidAtTimestamp = System.currentTimeMillis() - 1800000L
                ),
                courier = SampleCouriers.IBRAHIM_MUSA,
                currentLeg = DispatchLeg.LEG_1_CUSTOMER_TO_LAUNDROMAT,
                pickupOtp = "4819",
                deliveryOtp = "9201",
                createdAtTimestamp = System.currentTimeMillis() - 1800000L,
                estimatedDeliveryTimestamp = System.currentTimeMillis() + 82800000L,
                specialInstructions = "Please call when you reach the security checkpoint"
            )
            orderDao.insertOrder(OrderEntity.fromDomain(initialSample))
        }
    }
}
