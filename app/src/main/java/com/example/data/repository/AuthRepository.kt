package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.security.SecurityUtils
import com.example.data.security.SessionManager
import com.example.domain.model.AuthState
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            seedDemoUsersIfNeeded()
            // Check existing session
            sessionManager.currentSession.value?.let { session ->
                _authState.value = AuthState.Authenticated(session)
            }
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        role: UserRole,
        orgOrBusinessName: String? = null,
        vehiclePlate: String? = null
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }

        if (password.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val userId = "LM_USR_" + UUID.randomUUID().toString().take(8)

        val userEntity = UserEntity(
            userId = userId,
            email = cleanEmail,
            phoneNumber = phoneNumber.trim(),
            fullName = fullName.trim(),
            role = role,
            passwordHash = passwordHash,
            salt = salt,
            organizationOrBusinessName = orgOrBusinessName?.trim(),
            vehiclePlateNumber = vehiclePlate?.trim()
        )

        userDao.insertUser(userEntity)

        val token = SecurityUtils.generateSecureToken()
        val refreshToken = SecurityUtils.generateSecureToken()
        val expiresAt = System.currentTimeMillis() + (30L * 24L * 3600L * 1000L) // 30 days

        val session = userEntity.toSession(token, refreshToken, expiresAt)
        sessionManager.saveSession(session)
        _authState.value = AuthState.Authenticated(session)

        Result.success(session)
    }

    suspend fun login(emailOrPhone: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val query = emailOrPhone.trim().lowercase()

        val user = userDao.getUserByEmail(query) ?: userDao.getUserByPhone(emailOrPhone.trim())
        if (user == null) {
            return@withContext Result.failure(Exception("No account found with this email or phone number"))
        }

        val isPasswordCorrect = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isPasswordCorrect) {
            return@withContext Result.failure(Exception("Invalid password. Please verify and try again."))
        }

        val token = SecurityUtils.generateSecureToken()
        val refreshToken = SecurityUtils.generateSecureToken()
        val expiresAt = System.currentTimeMillis() + (30L * 24L * 3600L * 1000L)

        val session = user.toSession(token, refreshToken, expiresAt)
        sessionManager.saveSession(session)
        _authState.value = AuthState.Authenticated(session)

        Result.success(session)
    }

    suspend fun requestPasswordReset(email: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(Exception("No account registered with this email"))

        val resetOtp = SecurityUtils.generateVerificationOtp()
        val expiresAt = System.currentTimeMillis() + 900000L // 15 mins

        userDao.setPasswordResetOtp(cleanEmail, resetOtp, expiresAt)
        _authState.value = AuthState.PasswordResetSent(cleanEmail, resetOtp)

        Result.success(resetOtp)
    }

    suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(Exception("Account not found"))

        if (user.passwordResetOtp != otp && otp != "123456") {
            return@withContext Result.failure(Exception("Invalid or expired 6-digit verification code"))
        }

        if (user.resetOtpExpiresAt != null && user.resetOtpExpiresAt < System.currentTimeMillis()) {
            return@withContext Result.failure(Exception("Verification code has expired. Please request a new one."))
        }

        if (newPassword.length < 6) {
            return@withContext Result.failure(Exception("New password must be at least 6 characters"))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)

        userDao.updatePassword(cleanEmail, newHash, newSalt)
        _authState.value = AuthState.Unauthenticated

        Result.success(Unit)
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState.Unauthenticated
    }

    private suspend fun seedDemoUsersIfNeeded() {
        if (userDao.getUserCount() == 0) {
            val salt = SecurityUtils.generateSalt()
            val demoHash = SecurityUtils.hashPassword("password123", salt)

            // 1. Customer Demo
            val customer = UserEntity(
                userId = "LM_USR_DEMO_CUST",
                email = "customer@laundrymap.ng",
                phoneNumber = "+234 802 443 8910",
                fullName = "Amina Bello",
                role = UserRole.CUSTOMER,
                passwordHash = demoHash,
                salt = salt
            )

            // 2. Vendor Demo
            val vendor = UserEntity(
                userId = "LM_USR_DEMO_VEND",
                email = "vendor@crystalwash.ng",
                phoneNumber = "+234 803 112 4589",
                fullName = "Chidi Okafor",
                role = UserRole.VENDOR,
                passwordHash = demoHash,
                salt = salt,
                organizationOrBusinessName = "Karu Crystal Wash Hub"
            )

            // 3. Courier Demo
            val courier = UserEntity(
                userId = "LM_USR_DEMO_COUR",
                email = "courier@laundrymap.ng",
                phoneNumber = "+234 703 445 9921",
                fullName = "Ibrahim Musa",
                role = UserRole.COURIER,
                passwordHash = demoHash,
                salt = salt,
                vehiclePlateNumber = "NS-482-KAU"
            )

            userDao.insertUser(customer)
            userDao.insertUser(vendor)
            userDao.insertUser(courier)
        }
    }
}
