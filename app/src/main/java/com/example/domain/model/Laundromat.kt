package com.example.domain.model

data class Laundromat(
    val id: String,
    val name: String,
    val address: String,
    val location: GeoPoint,
    val rating: Float,
    val reviewCount: Int,
    val activeWashers: Int,
    val totalCapacityKg: Int,
    val currentQueueOrders: Int,
    val phone: String,
    val supportedServices: List<ServiceType>
)

object SampleLaundromats {
    val KARU_CRYSTAL_WASH = Laundromat(
        id = "lm_karu_01",
        name = "Karu Crystal Wash Hub",
        address = "Plot 12 Secretariat Road, Karu Site, Nasarawa",
        location = PilotLocations.KARU_SITE,
        rating = 4.9f,
        reviewCount = 184,
        activeWashers = 8,
        totalCapacityKg = 120,
        currentQueueOrders = 3,
        phone = "+234 803 112 4589",
        supportedServices = listOf(
            ServiceType.WASH_AND_FOLD,
            ServiceType.DRY_CLEANING,
            ServiceType.STEAM_IRONING,
            ServiceType.EXPRESS_TURNAROUND
        )
    )

    val MARARABA_EXPRESS = Laundromat(
        id = "lm_mararaba_02",
        name = "Mararaba UltraClean & Press",
        address = "Shop 4 Sharp Corner Plaza, Mararaba, Nasarawa",
        location = PilotLocations.MARARABA_CHECKPOINT,
        rating = 4.8f,
        reviewCount = 240,
        activeWashers = 12,
        totalCapacityKg = 180,
        currentQueueOrders = 5,
        phone = "+234 814 556 7890",
        supportedServices = listOf(
            ServiceType.WASH_AND_FOLD,
            ServiceType.STEAM_IRONING,
            ServiceType.EXPRESS_TURNAROUND
        )
    )

    val ASOKORO_ROYAL_GARMENTS = Laundromat(
        id = "lm_asokoro_03",
        name = "Asokoro Royal Garment Studio",
        address = "14 Justice Mohammed Bello Street, Asokoro, Abuja",
        location = PilotLocations.ASOKORO_PLATEAU,
        rating = 4.95f,
        reviewCount = 312,
        activeWashers = 16,
        totalCapacityKg = 240,
        currentQueueOrders = 2,
        phone = "+234 802 889 0012",
        supportedServices = listOf(
            ServiceType.WASH_AND_FOLD,
            ServiceType.DRY_CLEANING,
            ServiceType.STEAM_IRONING,
            ServiceType.EXPRESS_TURNAROUND
        )
    )

    val ALL = listOf(KARU_CRYSTAL_WASH, MARARABA_EXPRESS, ASOKORO_ROYAL_GARMENTS)
}

data class Courier(
    val id: String,
    val fullName: String,
    val phone: String,
    val vehicleModel: String,
    val plateNumber: String,
    val rating: Float,
    val completedTrips: Int,
    val currentLat: Double,
    val currentLng: Double,
    val speedKmh: Float,
    val headingDegrees: Float,
    val isOnline: Boolean
)

object SampleCouriers {
    val IBRAHIM_MUSA = Courier(
        id = "cr_ibrahim_01",
        fullName = "Ibrahim Musa",
        phone = "+234 703 445 9921",
        vehicleModel = "Bajaj Boxer 150 (Cargo Box)",
        plateNumber = "NS-482-KAU",
        rating = 4.92f,
        completedTrips = 430,
        currentLat = 9.0220,
        currentLng = 7.5700,
        speedKmh = 38.5f,
        headingDegrees = 42.0f,
        isOnline = true
    )

    val EMMANUEL_OKON = Courier(
        id = "cr_emmanuel_02",
        fullName = "Emmanuel Okon",
        phone = "+234 812 778 3341",
        vehicleModel = "TVS HLX Plus Courier Van",
        plateNumber = "ABJ-913-XY",
        rating = 4.88f,
        completedTrips = 315,
        currentLat = 9.0310,
        currentLng = 7.5620,
        speedKmh = 28.0f,
        headingDegrees = 120.0f,
        isOnline = true
    )
}
