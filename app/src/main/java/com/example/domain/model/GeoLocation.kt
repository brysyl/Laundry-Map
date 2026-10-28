package com.example.domain.model

enum class CorridorZone(val stateName: String, val corridorTitle: String) {
    KARU_MARARABA("Nasarawa State", "Karu/Mararaba Pilot Corridor"),
    ABUJA_METRO("Federal Capital Territory", "Abuja Metro Expansion")
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val landmark: String,
    val zone: CorridorZone
)

object PilotLocations {
    // Nasarawa State (Karu / Mararaba Corridor)
    val MARARABA_CHECKPOINT = GeoPoint(
        latitude = 9.0289,
        longitude = 7.5762,
        name = "Mararaba Checkpoint Junction",
        landmark = "Near Sharp Corner & Guarantee Trust Bank",
        zone = CorridorZone.KARU_MARARABA
    )

    val KARU_SITE = GeoPoint(
        latitude = 9.0065,
        longitude = 7.5684,
        name = "Karu Site Phase 1",
        landmark = "Behind Karu Local Government Secretariat",
        zone = CorridorZone.KARU_MARARABA
    )

    val ADO_JUNCTION = GeoPoint(
        latitude = 9.0211,
        longitude = 7.6012,
        name = "Ado Bus Stop / Flyover",
        landmark = "Opposite Total Energies, Ado Road",
        zone = CorridorZone.KARU_MARARABA
    )

    val NEW_KARU_CENTRAL = GeoPoint(
        latitude = 8.9892,
        longitude = 7.5910,
        name = "New Karu Central",
        landmark = "Along Keffi Expressway near Royal Market",
        zone = CorridorZone.KARU_MARARABA
    )

    val MASAKA_CORRIDOR = GeoPoint(
        latitude = 8.9610,
        longitude = 7.6254,
        name = "Masaka Market Gate",
        landmark = "Keffi-Abuja Dual Carriage Way",
        zone = CorridorZone.KARU_MARARABA
    )

    val NYANYA_INTERCHANGE = GeoPoint(
        latitude = 9.0345,
        longitude = 7.5512,
        name = "Nyanya Bridge Interchange",
        landmark = "Border Crossing towards Abuja",
        zone = CorridorZone.KARU_MARARABA
    )

    // Abuja Metro Expansion Zones
    val ASOKORO_PLATEAU = GeoPoint(
        latitude = 9.0432,
        longitude = 7.5189,
        name = "Asokoro High Plateau",
        landmark = "Near ECOWAS Secretariat & Yakubu Gowon Cres.",
        zone = CorridorZone.ABUJA_METRO
    )

    val GUZAPE_DISTRICT = GeoPoint(
        latitude = 9.0189,
        longitude = 7.5021,
        name = "Guzape Diplomatic Zone",
        landmark = "Near Channel View & Asokoro Extension",
        zone = CorridorZone.ABUJA_METRO
    )

    val CENTRAL_BUSINESS_DISTRICT = GeoPoint(
        latitude = 9.0579,
        longitude = 7.4898,
        name = "Abuja CBD Plaza",
        landmark = "Constitution Avenue near Central Mosque",
        zone = CorridorZone.ABUJA_METRO
    )

    val MAITAMA_GARDENS = GeoPoint(
        latitude = 9.0833,
        longitude = 7.4950,
        name = "Maitama District",
        landmark = "Gana Street Commercial Strip",
        zone = CorridorZone.ABUJA_METRO
    )

    val ALL_PRESETS = listOf(
        MARARABA_CHECKPOINT,
        KARU_SITE,
        ADO_JUNCTION,
        NEW_KARU_CENTRAL,
        MASAKA_CORRIDOR,
        NYANYA_INTERCHANGE,
        ASOKORO_PLATEAU,
        GUZAPE_DISTRICT,
        CENTRAL_BUSINESS_DISTRICT,
        MAITAMA_GARDENS
    )

    fun calculateDistanceKm(from: GeoPoint, to: GeoPoint): Double {
        val r = 6371.0 // Earth radius in km
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
