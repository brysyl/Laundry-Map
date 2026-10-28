package com.example.domain.model

enum class PaymentProvider {
    PAYSTACK,
    FLUTTERWAVE,
    CASH_ON_DELIVERY
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

data class PaymentDetails(
    val provider: PaymentProvider,
    val transactionRef: String,
    val amountNgn: Int,
    val status: PaymentStatus,
    val paidAtTimestamp: Long? = null
)

data class OrderItem(
    val item: LaundryItem,
    val quantity: Int,
    val customNotes: String = ""
)

enum class DispatchLeg {
    LEG_1_CUSTOMER_TO_LAUNDROMAT,
    LEG_2_LAUNDROMAT_TO_CUSTOMER
}

data class Order(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerLocation: GeoPoint,
    val customerAddressDetail: String,
    val laundromat: Laundromat,
    val serviceType: ServiceType,
    val items: List<OrderItem>,
    val subtotalNgn: Int,
    val deliveryFeeNgn: Int,
    val totalNgn: Int,
    val state: OrderState,
    val payment: PaymentDetails,
    val courier: Courier?,
    val currentLeg: DispatchLeg,
    val pickupOtp: String,
    val deliveryOtp: String,
    val clientSignaturePath: String? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val estimatedDeliveryTimestamp: Long = System.currentTimeMillis() + 86400000L,
    val specialInstructions: String = "",
    val photoAttachments: List<String> = emptyList()
) {
    val totalGarmentsCount: Int
        get() = items.sumOf { it.quantity }
}
