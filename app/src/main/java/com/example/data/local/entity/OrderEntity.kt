package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
import com.example.domain.model.ProcessingStage
import com.example.domain.model.SampleCouriers
import com.example.domain.model.SampleLaundromats
import com.example.domain.model.ServiceType

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerLat: Double,
    val customerLng: Double,
    val customerLocName: String,
    val customerLandmark: String,
    val customerZone: CorridorZone,
    val customerAddressDetail: String,
    val laundromatId: String,
    val serviceType: ServiceType,
    val serializedItems: String, // format: itemId:qty:notes;...
    val subtotalNgn: Int,
    val deliveryFeeNgn: Int,
    val totalNgn: Int,
    val stateKey: String,
    val processingStage: ProcessingStage?,
    val paymentProvider: PaymentProvider,
    val paymentRef: String,
    val paymentStatus: PaymentStatus,
    val courierId: String?,
    val currentLeg: DispatchLeg,
    val pickupOtp: String,
    val deliveryOtp: String,
    val clientSignaturePath: String?,
    val createdAtTimestamp: Long,
    val estimatedDeliveryTimestamp: Long,
    val specialInstructions: String,
    val photoAttachmentsJoined: String // pipe separated
) {
    fun toDomain(): Order {
        val laundromat = SampleLaundromats.ALL.find { it.id == laundromatId }
            ?: SampleLaundromats.KARU_CRYSTAL_WASH

        val courier = courierId?.let { id ->
            if (id == SampleCouriers.IBRAHIM_MUSA.id) SampleCouriers.IBRAHIM_MUSA
            else SampleCouriers.EMMANUEL_OKON
        }

        val items = serializedItems.split(";").filter { it.isNotBlank() }.mapNotNull { raw ->
            val parts = raw.split(":")
            if (parts.isNotEmpty()) {
                val item = LaundryCatalog.ITEMS.find { it.id == parts[0] }
                val qty = parts.getOrNull(1)?.toIntOrNull() ?: 1
                val notes = parts.getOrNull(2) ?: ""
                item?.let { OrderItem(it, qty, notes) }
            } else null
        }

        val domainState = OrderState.fromKey(stateKey, processingStage?.name)

        return Order(
            id = id,
            customerName = customerName,
            customerPhone = customerPhone,
            customerLocation = GeoPoint(
                latitude = customerLat,
                longitude = customerLng,
                name = customerLocName,
                landmark = customerLandmark,
                zone = customerZone
            ),
            customerAddressDetail = customerAddressDetail,
            laundromat = laundromat,
            serviceType = serviceType,
            items = items,
            subtotalNgn = subtotalNgn,
            deliveryFeeNgn = deliveryFeeNgn,
            totalNgn = totalNgn,
            state = domainState,
            payment = PaymentDetails(
                provider = paymentProvider,
                transactionRef = paymentRef,
                amountNgn = totalNgn,
                status = paymentStatus,
                paidAtTimestamp = if (paymentStatus == PaymentStatus.PAID) createdAtTimestamp else null
            ),
            courier = courier,
            currentLeg = currentLeg,
            pickupOtp = pickupOtp,
            deliveryOtp = deliveryOtp,
            clientSignaturePath = clientSignaturePath,
            createdAtTimestamp = createdAtTimestamp,
            estimatedDeliveryTimestamp = estimatedDeliveryTimestamp,
            specialInstructions = specialInstructions,
            photoAttachments = photoAttachmentsJoined.split("|").filter { it.isNotBlank() }
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderEntity {
            val serialized = order.items.joinToString(";") {
                "${it.item.id}:${it.quantity}:${it.customNotes}"
            }
            val (stateKey, processingStage) = when (val s = order.state) {
                is OrderState.Processing -> Pair(s.key, s.stage)
                else -> Pair(s.key, null)
            }

            return OrderEntity(
                id = order.id,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                customerLat = order.customerLocation.latitude,
                customerLng = order.customerLocation.longitude,
                customerLocName = order.customerLocation.name,
                customerLandmark = order.customerLocation.landmark,
                customerZone = order.customerLocation.zone,
                customerAddressDetail = order.customerAddressDetail,
                laundromatId = order.laundromat.id,
                serviceType = order.serviceType,
                serializedItems = serialized,
                subtotalNgn = order.subtotalNgn,
                deliveryFeeNgn = order.deliveryFeeNgn,
                totalNgn = order.totalNgn,
                stateKey = stateKey,
                processingStage = processingStage,
                paymentProvider = order.payment.provider,
                paymentRef = order.payment.transactionRef,
                paymentStatus = order.payment.status,
                courierId = order.courier?.id,
                currentLeg = order.currentLeg,
                pickupOtp = order.pickupOtp,
                deliveryOtp = order.deliveryOtp,
                clientSignaturePath = order.clientSignaturePath,
                createdAtTimestamp = order.createdAtTimestamp,
                estimatedDeliveryTimestamp = order.estimatedDeliveryTimestamp,
                specialInstructions = order.specialInstructions,
                photoAttachmentsJoined = order.photoAttachments.joinToString("|")
            )
        }
    }
}
