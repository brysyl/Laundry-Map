package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.security.SessionManager
import com.example.domain.model.AuthState
import com.example.domain.model.UserRole
import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthTab {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

data class AuthUiState(
    val activeTab: AuthTab = AuthTab.SIGN_IN,
    val selectedRole: UserRole = UserRole.CUSTOMER,
    val emailInput: String = "",
    val passwordInput: String = "",
    val fullNameInput: String = "",
    val phoneInput: String = "",
    val businessNameInput: String = "",
    val vehiclePlateInput: String = "",
    val resetEmailInput: String = "",
    val resetOtpInput: String = "",
    val resetNewPasswordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val resetOtpSimulated: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentSession: StateFlow<UserSession?> = sessionManager.currentSession

    val authState: StateFlow<AuthState> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Unauthenticated
    )

    fun selectTab(tab: AuthTab) {
        _uiState.value = _uiState.value.copy(
            activeTab = tab,
            errorMessage = null,
            successMessage = null
        )
    }

    fun selectRole(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun updateEmail(value: String) { _uiState.value = _uiState.value.copy(emailInput = value, errorMessage = null) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(passwordInput = value, errorMessage = null) }
    fun updateFullName(value: String) { _uiState.value = _uiState.value.copy(fullNameInput = value) }
    fun updatePhone(value: String) { _uiState.value = _uiState.value.copy(phoneInput = value) }
    fun updateBusinessName(value: String) { _uiState.value = _uiState.value.copy(businessNameInput = value) }
    fun updateVehiclePlate(value: String) { _uiState.value = _uiState.value.copy(vehiclePlateInput = value) }
    fun updateResetEmail(value: String) { _uiState.value = _uiState.value.copy(resetEmailInput = value, errorMessage = null) }
    fun updateResetOtp(value: String) { _uiState.value = _uiState.value.copy(resetOtpInput = value, errorMessage = null) }
    fun updateResetNewPassword(value: String) { _uiState.value = _uiState.value.copy(resetNewPasswordInput = value, errorMessage = null) }

    fun quickFillDemo(role: UserRole) {
        when (role) {
            UserRole.CUSTOMER -> {
                _uiState.value = _uiState.value.copy(
                    emailInput = "customer@laundrymap.ng",
                    passwordInput = "password123",
                    selectedRole = UserRole.CUSTOMER
                )
            }
            UserRole.VENDOR -> {
                _uiState.value = _uiState.value.copy(
                    emailInput = "vendor@crystalwash.ng",
                    passwordInput = "password123",
                    selectedRole = UserRole.VENDOR
                )
            }
            UserRole.COURIER -> {
                _uiState.value = _uiState.value.copy(
                    emailInput = "courier@laundrymap.ng",
                    passwordInput = "password123",
                    selectedRole = UserRole.COURIER
                )
            }
        }
    }

    fun login() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        emailInput = "",
                        passwordInput = ""
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun register() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput
        val fullName = _uiState.value.fullNameInput.trim()
        val phone = _uiState.value.phoneInput.trim()
        val role = _uiState.value.selectedRole
        val org = _uiState.value.businessNameInput.trim().ifEmpty { null }
        val plate = _uiState.value.vehiclePlateInput.trim().ifEmpty { null }

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phone,
                role = role,
                orgOrBusinessName = org,
                vehiclePlate = plate
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        emailInput = "",
                        passwordInput = "",
                        fullNameInput = "",
                        phoneInput = ""
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun requestPasswordReset() {
        val email = _uiState.value.resetEmailInput.trim()
        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please provide your registered email")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.requestPasswordReset(email)
            result.fold(
                onSuccess = { otp ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        resetOtpSimulated = otp,
                        successMessage = "Reset PIN sent to $email: Code is $otp"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not request reset"
                    )
                }
            )
        }
    }

    fun confirmPasswordReset() {
        val email = _uiState.value.resetEmailInput.trim()
        val otp = _uiState.value.resetOtpInput.trim()
        val newPass = _uiState.value.resetNewPasswordInput

        if (email.isEmpty() || otp.isEmpty() || newPass.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email, 6-digit OTP, and new password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.confirmPasswordReset(email, otp, newPass)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeTab = AuthTab.SIGN_IN,
                        successMessage = "Password successfully reset! Please sign in with your new password.",
                        resetEmailInput = "",
                        resetOtpInput = "",
                        resetNewPasswordInput = ""
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Reset verification failed"
                    )
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository, sessionManager) as T
        }
    }
}
