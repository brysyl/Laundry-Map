package com.example.domain.model

enum class ProcessingStage(val displayName: String, val description: String) {
    WASHING("Washing & Detergent Soak", "Garments in eco-friendly enzyme wash"),
    DRYING("Tumble Drying", "Gentle low-heat drying process"),
    IRONING("Steam Ironing & Pressing", "Precision crease and wrinkle removal"),
    READY_FOR_DISPATCH("Folded & Bagged", "Sealed in protective garment bags")
}

sealed class OrderState(
    val key: String,
    val stepIndex: Int,
    val title: String,
    val description: String,
    val isTerminal: Boolean = false
) {
    object Created : OrderState(
        key = "CREATED",
        stepIndex = 0,
        title = "Order Placed",
        description = "Awaiting laundromat confirmation in pilot corridor"
    )

    object DriverAssignedForPickup : OrderState(
        key = "DRIVER_ASSIGNED_FOR_PICKUP",
        stepIndex = 1,
        title = "Rider En Route for Pickup",
        description = "Leg 1: Courier heading to your location"
    )

    object PickedUp : OrderState(
        key = "PICKED_UP",
        stepIndex = 2,
        title = "Laundry Collected",
        description = "Courier has secured your laundry bag"
    )

    object AtLaundromat : OrderState(
        key = "AT_LAUNDROMAT",
        stepIndex = 3,
        title = "Arrived at Laundromat",
        description = "Checked in and tagged for washing"
    )

    data class Processing(
        val stage: ProcessingStage = ProcessingStage.WASHING
    ) : OrderState(
        key = "PROCESSING",
        stepIndex = 4,
        title = "Processing (${stage.displayName})",
        description = stage.description
    )

    object ReadyForDelivery : OrderState(
        key = "READY_FOR_DELIVERY",
        stepIndex = 5,
        title = "Ready for Delivery",
        description = "Clean, pressed, and packaged for return dispatch"
    )

    object DriverAssignedForDropoff : OrderState(
        key = "DRIVER_ASSIGNED_FOR_DROPOFF",
        stepIndex = 6,
        title = "Out for Delivery",
        description = "Leg 2: Courier en route to your dropoff address"
    )

    object Delivered : OrderState(
        key = "DELIVERED",
        stepIndex = 7,
        title = "Delivered & Verified",
        description = "Handover complete with client OTP/signature verification",
        isTerminal = true
    )

    data class Cancelled(val reason: String = "Cancelled by user") : OrderState(
        key = "CANCELLED",
        stepIndex = -1,
        title = "Cancelled",
        description = reason,
        isTerminal = true
    )

    companion object {
        fun fromKey(key: String, processingStageName: String? = null): OrderState {
            return when (key.uppercase()) {
                "CREATED" -> Created
                "DRIVER_ASSIGNED_FOR_PICKUP" -> DriverAssignedForPickup
                "PICKED_UP" -> PickedUp
                "AT_LAUNDROMAT" -> AtLaundromat
                "PROCESSING" -> {
                    val stage = processingStageName?.let {
                        try {
                            ProcessingStage.valueOf(it)
                        } catch (e: Exception) {
                            ProcessingStage.WASHING
                        }
                    } ?: ProcessingStage.WASHING
                    Processing(stage)
                }
                "READY_FOR_DELIVERY" -> ReadyForDelivery
                "DRIVER_ASSIGNED_FOR_DROPOFF" -> DriverAssignedForDropoff
                "DELIVERED" -> Delivered
                "CANCELLED" -> Cancelled()
                else -> Created
            }
        }
    }
}
