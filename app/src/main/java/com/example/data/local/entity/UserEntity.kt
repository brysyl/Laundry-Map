package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val phoneNumber: String,
    val fullName: String,
    val role: UserRole,
    val passwordHash: String,
    val salt: String,
    val organizationOrBusinessName: String? = null,
    val vehiclePlateNumber: String? = null,
    val passwordResetOtp: String? = null,
    val resetOtpExpiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toSession(authToken: String, refreshToken: String, expiresAt: Long): UserSession {
        return UserSession(
            userId = userId,
            email = email,
            fullName = fullName,
            phoneNumber = phoneNumber,
            role = role,
            authToken = authToken,
            refreshToken = refreshToken,
            expiresAtTimestamp = expiresAt,
            organizationOrBusinessName = organizationOrBusinessName,
            vehiclePlateNumber = vehiclePlateNumber
        )
    }
}
