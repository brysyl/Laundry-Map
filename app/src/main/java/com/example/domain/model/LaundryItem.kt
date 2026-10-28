package com.example.domain.model

enum class ServiceType(
    val id: String,
    val title: String,
    val tagLine: String,
    val baseTurnaroundHours: Int,
    val expressMultiplier: Double,
    val colorHex: Long
) {
    WASH_AND_FOLD(
        id = "wash_fold",
        title = "Wash & Fold",
        tagLine = "Everyday wash, fabric softener & crisp folding",
        baseTurnaroundHours = 24,
        expressMultiplier = 1.0,
        colorHex = 0xFF0284C7
    ),
    DRY_CLEANING(
        id = "dry_clean",
        title = "Premium Dry Cleaning",
        tagLine = "Chemical solvent care for suits, gowns & delicate silks",
        baseTurnaroundHours = 48,
        expressMultiplier = 1.35,
        colorHex = 0xFF0D9488
    ),
    STEAM_IRONING(
        id = "steam_iron",
        title = "Steam Press & Ironing",
        tagLine = "Wrinkle-free pressing on hangers or precision folded",
        baseTurnaroundHours = 12,
        expressMultiplier = 1.0,
        colorHex = 0xFF8B5CF6
    ),
    EXPRESS_TURNAROUND(
        id = "express_same_day",
        title = "Express Same-Day",
        tagLine = "Rapid turnaround delivered back within 6 hours",
        baseTurnaroundHours = 6,
        expressMultiplier = 1.6,
        colorHex = 0xFFF97316
    )
}

data class LaundryItem(
    val id: String,
    val name: String,
    val category: String,
    val basePriceNgn: Int,
    val iconType: String,
    val defaultNotes: String = ""
)

object LaundryCatalog {
    val ITEMS = listOf(
        LaundryItem("shirt_formal", "Men / Women Shirts", "Tops", 700, "shirt", "Collar starch check"),
        LaundryItem("trousers_pants", "Trousers & Chinos", "Bottoms", 800, "pant", "Crease down the center"),
        LaundryItem("native_agbada", "Traditional Agbada / Senegalese", "Traditionals", 2500, "agbada", "Gentle steam only"),
        LaundryItem("native_kaftan", "Kaftan & 2-Piece Native", "Traditionals", 1500, "kaftan", "Embroidery protection"),
        LaundryItem("suit_2piece", "Full 2-Piece Suit", "Suits", 3500, "suit", "Dry clean only, wooden hanger"),
        LaundryItem("bedsheet_duvet", "Beddings & Duvet Quilt", "Household", 3000, "bedding", "Deep sanitization"),
        LaundryItem("dress_gown", "Dinner Gown / Abaya", "Dresses", 2000, "dress", "Delicate fabric steam"),
        LaundryItem("jeans_denim", "Denim Jeans & Jackets", "Heavywear", 900, "jeans", "Color preservation wash")
    )
}
