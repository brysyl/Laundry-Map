package com.example.domain.model

enum class UserRole(val displayName: String, val description: String) {
    CUSTOMER("Customer", "Request on-demand laundry pickups and track deliveries"),
    VENDOR("Vendor (Laundromat)", "Manage order queues, washer capacities, and washing stages"),
    COURIER("Courier Rider", "Accept dual-leg dispatches and complete verified deliveries")
}

data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val role: UserRole,
    val authToken: String,
    val refreshToken: String,
    val expiresAtTimestamp: Long,
    val organizationOrBusinessName: String? = null,
    val vehiclePlateNumber: String? = null
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class PasswordResetSent(val email: String, val resetCode: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
