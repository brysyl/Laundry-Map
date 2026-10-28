package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.CorridorZone
import com.example.domain.model.DispatchLeg
import com.example.domain.model.NotificationType
import com.example.domain.model.PaymentProvider
import com.example.domain.model.PaymentStatus
import com.example.domain.model.ProcessingStage
import com.example.domain.model.ServiceType
import com.example.domain.model.UserRole

class Converters {
    @TypeConverter
    fun fromCorridorZone(zone: CorridorZone): String = zone.name

    @TypeConverter
    fun toCorridorZone(value: String): CorridorZone = try {
        CorridorZone.valueOf(value)
    } catch (e: Exception) {
        CorridorZone.KARU_MARARABA
    }

    @TypeConverter
    fun fromDispatchLeg(leg: DispatchLeg): String = leg.name

    @TypeConverter
    fun toDispatchLeg(value: String): DispatchLeg = try {
        DispatchLeg.valueOf(value)
    } catch (e: Exception) {
        DispatchLeg.LEG_1_CUSTOMER_TO_LAUNDROMAT
    }

    @TypeConverter
    fun fromPaymentProvider(provider: PaymentProvider): String = provider.name

    @TypeConverter
    fun toPaymentProvider(value: String): PaymentProvider = try {
        PaymentProvider.valueOf(value)
    } catch (e: Exception) {
        PaymentProvider.PAYSTACK
    }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = try {
        PaymentStatus.valueOf(value)
    } catch (e: Exception) {
        PaymentStatus.PENDING
    }

    @TypeConverter
    fun fromServiceType(serviceType: ServiceType): String = serviceType.name

    @TypeConverter
    fun toServiceType(value: String): ServiceType = try {
        ServiceType.valueOf(value)
    } catch (e: Exception) {
        ServiceType.WASH_AND_FOLD
    }

    @TypeConverter
    fun fromProcessingStage(stage: ProcessingStage?): String? = stage?.name

    @TypeConverter
    fun toProcessingStage(value: String?): ProcessingStage? = value?.let {
        try {
            ProcessingStage.valueOf(it)
        } catch (e: Exception) {
            ProcessingStage.WASHING
        }
    }

    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let {
        try {
            UserRole.valueOf(it)
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }
    }

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (e: Exception) {
        NotificationType.SYSTEM_NOTICE
    }
}
