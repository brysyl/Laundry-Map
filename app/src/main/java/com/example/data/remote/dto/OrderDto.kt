package com.example.data.remote.dto

data class OrderCreateRequest(
    val customerName: String,
    val customerPhone: String,
    val customerLat: Double,
    val customerLng: Double,
    val customerLocName: String,
    val customerLandmark: String,
    val customerZone: String,
    val customerAddressDetail: String,
    val laundromatId: String,
    val serviceTypeId: String,
    val items: List<OrderItemDto>,
    val specialInstructions: String,
    val paymentProvider: String
)

data class OrderItemDto(
    val itemId: String,
    val quantity: Int,
    val customNotes: String
)

data class OrderResponseDto(
    val id: String,
    val stateKey: String,
    val processingStage: String?,
    val totalNgn: Int,
    val message: String
)

data class TelemetryUpdate(
    val courierId: String,
    val orderId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float,
    val headingDegrees: Float,
    val timestamp: Long = System.currentTimeMillis()
)
