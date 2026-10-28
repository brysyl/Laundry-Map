package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "laundry_map_secure_session",
        Context.MODE_PRIVATE
    )

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    init {
        loadSessionFromStorage()
    }

    private fun loadSessionFromStorage() {
        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val fullName = prefs.getString(KEY_FULL_NAME, null)
        val phone = prefs.getString(KEY_PHONE, null)
        val roleStr = prefs.getString(KEY_ROLE, null)
        val authToken = prefs.getString(KEY_AUTH_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val orgName = prefs.getString(KEY_ORG_NAME, null)
        val plate = prefs.getString(KEY_PLATE, null)

        if (userId != null && email != null && authToken != null && roleStr != null) {
            val role = try {
                UserRole.valueOf(roleStr)
            } catch (e: Exception) {
                UserRole.CUSTOMER
            }

            // Check expiry (give 30 days validity)
            if (expiresAt == 0L || expiresAt > System.currentTimeMillis()) {
                _currentSession.value = UserSession(
                    userId = userId,
                    email = email,
                    fullName = fullName ?: "User",
                    phoneNumber = phone ?: "",
                    role = role,
                    authToken = authToken,
                    refreshToken = refreshToken ?: "",
                    expiresAtTimestamp = if (expiresAt == 0L) System.currentTimeMillis() + 2592000000L else expiresAt,
                    organizationOrBusinessName = orgName,
                    vehiclePlateNumber = plate
                )
            } else {
                clearSession()
            }
        }
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_FULL_NAME, session.fullName)
            .putString(KEY_PHONE, session.phoneNumber)
            .putString(KEY_ROLE, session.role.name)
            .putString(KEY_AUTH_TOKEN, session.authToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.expiresAtTimestamp)
            .putString(KEY_ORG_NAME, session.organizationOrBusinessName)
            .putString(KEY_PLATE, session.vehiclePlateNumber)
            .apply()

        _currentSession.value = session
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentSession.value = null
    }

    fun switchActiveRole(newRole: UserRole) {
        val current = _currentSession.value ?: return
        val updated = current.copy(role = newRole)
        saveSession(updated)
    }

    companion object {
        private const val KEY_USER_ID = "session_user_id"
        private const val KEY_EMAIL = "session_email"
        private const val KEY_FULL_NAME = "session_full_name"
        private const val KEY_PHONE = "session_phone"
        private const val KEY_ROLE = "session_role"
        private const val KEY_AUTH_TOKEN = "session_auth_token"
        private const val KEY_REFRESH_TOKEN = "session_refresh_token"
        private const val KEY_EXPIRES_AT = "session_expires_at"
        private const val KEY_ORG_NAME = "session_org_name"
        private const val KEY_PLATE = "session_plate"
    }
}
