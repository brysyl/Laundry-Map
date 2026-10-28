package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.theme.CoralExpress
import com.example.ui.theme.MintFoamSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.AuthTab
import com.example.ui.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: AuthUiState,
    onTabSelected: (AuthTab) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onBusinessNameChanged: (String) -> Unit,
    onVehiclePlateChanged: (String) -> Unit,
    onResetEmailChanged: (String) -> Unit,
    onResetOtpChanged: (String) -> Unit,
    onResetNewPasswordChanged: (String) -> Unit,
    onQuickFillDemo: (UserRole) -> Unit,
    onLoginClicked: () -> Unit,
    onRegisterClicked: () -> Unit,
    onRequestResetClicked: () -> Unit,
    onConfirmResetClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var isForgotPasswordOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Brand Hero Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(OceanBluePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TwoWheeler,
                contentDescription = "Laundry Map Logo",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Laundry Map",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = "On-Demand Laundry Marketplace • Nasarawa & Abuja",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1-Tap Quick Fill Demo Credentials (for effortless review)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = OceanBlueLight.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "QUICK TEST DEMO LOGINS (1-TAP):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBluePrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.emailInput == "customer@laundrymap.ng",
                        onClick = { onQuickFillDemo(UserRole.CUSTOMER) },
                        label = { Text("Customer", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.testTag("demo_customer_chip")
                    )
                    FilterChip(
                        selected = state.emailInput == "vendor@crystalwash.ng",
                        onClick = { onQuickFillDemo(UserRole.VENDOR) },
                        label = { Text("Vendor Hub", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Store, null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.testTag("demo_vendor_chip")
                    )
                    FilterChip(
                        selected = state.emailInput == "courier@laundrymap.ng",
                        onClick = { onQuickFillDemo(UserRole.COURIER) },
                        label = { Text("Courier Rider", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.ElectricBike, null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.testTag("demo_courier_chip")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sign In / Register Tab Row
        TabRow(
            selectedTabIndex = if (state.activeTab == AuthTab.SIGN_IN) 0 else 1,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.activeTab == AuthTab.SIGN_IN,
                onClick = { onTabSelected(AuthTab.SIGN_IN) },
                text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("auth_tab_sign_in")
            )
            Tab(
                selected = state.activeTab == AuthTab.SIGN_UP,
                onClick = { onTabSelected(AuthTab.SIGN_UP) },
                text = { Text("Create Account", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("auth_tab_sign_up")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Role Selector for Registration
        if (state.activeTab == AuthTab.SIGN_UP) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Persona Account Role",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.values().forEach { role ->
                        val isSelected = state.selectedRole == role
                        val roleColor = when (role) {
                            UserRole.CUSTOMER -> OceanBluePrimary
                            UserRole.VENDOR -> CoralExpress
                            UserRole.COURIER -> MintFoamSecondary
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) roleColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, roleColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onRoleSelected(role) }
                                .padding(vertical = 2.dp)
                                .testTag("role_select_${role.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = role.displayName.take(10),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Error message banner
        if (state.errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                color = StatusError.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusError.copy(alpha = 0.4f))
            ) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(color = StatusError, fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Success message banner
        if (state.successMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                color = StatusSuccess.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f))
            ) {
                Text(
                    text = state.successMessage,
                    style = MaterialTheme.typography.bodySmall.copy(color = StatusSuccess, fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Form Fields
        if (state.activeTab == AuthTab.SIGN_UP) {
            OutlinedTextField(
                value = state.fullNameInput,
                onValueChange = onFullNameChanged,
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_fullname_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.phoneInput,
                onValueChange = onPhoneChanged,
                label = { Text("Phone Number (+234...)") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_phone_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Role specific extra input
            if (state.selectedRole == UserRole.VENDOR) {
                OutlinedTextField(
                    value = state.businessNameInput,
                    onValueChange = onBusinessNameChanged,
                    label = { Text("Laundromat Business Name") },
                    leadingIcon = { Icon(Icons.Default.Business, null) },
                    placeholder = { Text("e.g. Karu Crystal Wash Hub") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_business_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (state.selectedRole == UserRole.COURIER) {
                OutlinedTextField(
                    value = state.vehiclePlateInput,
                    onValueChange = onVehiclePlateChanged,
                    label = { Text("Motorbike Plate Number") },
                    leadingIcon = { Icon(Icons.Default.ElectricBike, null) },
                    placeholder = { Text("e.g. NS-482-KAU") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_plate_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        OutlinedTextField(
            value = state.emailInput,
            onValueChange = onEmailChanged,
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.passwordInput,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input"),
            shape = RoundedCornerShape(12.dp)
        )

        if (state.activeTab == AuthTab.SIGN_IN) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { isForgotPasswordOpen = true },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text("Forgot Password?", style = MaterialTheme.typography.bodySmall.copy(color = OceanBluePrimary))
                }
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Submit Button
        Button(
            onClick = {
                if (state.activeTab == AuthTab.SIGN_IN) onLoginClicked() else onRegisterClicked()
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = if (state.activeTab == AuthTab.SIGN_IN) "Sign In Securely" else "Register Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Forgot Password Bottom Sheet
    if (isForgotPasswordOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isForgotPasswordOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("forgot_password_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reset Account Password",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { isForgotPasswordOpen = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter your registered email to receive a 6-digit recovery PIN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.resetEmailInput,
                    onValueChange = onResetEmailChanged,
                    label = { Text("Registered Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRequestResetClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_otp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary)
                ) {
                    Text("Send Recovery PIN")
                }

                // If OTP was simulated / generated
                if (state.resetOtpSimulated != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MintFoamSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Recovery Code: ${state.resetOtpSimulated}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MintFoamSecondary
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.resetOtpInput,
                        onValueChange = onResetOtpChanged,
                        label = { Text("Enter 6-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_otp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.resetNewPasswordInput,
                        onValueChange = onResetNewPasswordChanged,
                        label = { Text("New Secure Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_new_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onConfirmResetClicked()
                            isForgotPasswordOpen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_reset_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MintFoamSecondary)
                    ) {
                        Text("Update Password & Return to Login")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
