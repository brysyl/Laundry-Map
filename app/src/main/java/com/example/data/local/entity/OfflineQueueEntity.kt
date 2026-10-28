package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_queue")
data class OfflineQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val orderId: String,
    val actionType: String, // e.g. "UPDATE_STATE", "CONFIRM_OTP", "SUBMIT_SIGNATURE"
    val payloadJson: String,
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "laundromats")
data class LaundromatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val activeWashers: Int,
    val totalCapacityKg: Int,
    val currentQueueOrders: Int,
    val phone: String
)

@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val vehicleModel: String,
    val plateNumber: String,
    val rating: Float,
    val currentLat: Double,
    val currentLng: Double,
    val speedKmh: Float,
    val headingDegrees: Float,
    val isOnline: Boolean
)
