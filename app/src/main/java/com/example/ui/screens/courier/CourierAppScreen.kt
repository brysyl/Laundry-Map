package com.example.ui.screens.courier

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DispatchLeg
import com.example.domain.model.Order
import com.example.domain.model.OrderState
import com.example.ui.components.LaundryInteractiveMap
import com.example.ui.components.SignaturePad
import com.example.ui.theme.CoralExpress
import com.example.ui.theme.MintFoamSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.CourierUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierAppScreen(
    state: CourierUiState,
    assignedOrders: List<Order>,
    onToggleAvailability: () -> Unit,
    onToggleNavigation: () -> Unit,
    onPickUpFromCustomer: (String) -> Unit,
    onDeliverToLaundromat: (String) -> Unit,
    onOpenDeliveryModal: (Order) -> Unit,
    onCloseDeliveryModal: () -> Unit,
    onOtpEntered: (String) -> Unit,
    onConfirmDelivery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Transporter Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CoralExpress.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBike,
                                contentDescription = null,
                                tint = CoralExpress,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = state.courier.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${state.courier.vehicleModel} • ${state.courier.plateNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isAvailable) "Active" else "Offline",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.isAvailable) MintFoamSecondary else Color(0xFF94A3B8)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isAvailable,
                            onCheckedChange = { onToggleAvailability() },
                            modifier = Modifier.testTag("courier_availability_switch"),
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MintFoamSecondary)
                        )
                    }
                }
            }
        }

        // 2. Turn-by-Turn Navigation HUD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Turn by turn",
                                tint = OceanBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Corridor Turn-by-Turn HUD",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanBluePrimary.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onToggleNavigation() }
                        ) {
                            Text(
                                text = if (state.navigationActive) "Hide HUD" else "Live HUD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBluePrimary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Turn Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavyDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DriveEta,
                                contentDescription = null,
                                tint = CoralExpress,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = state.nextTurnInstruction,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Estimated Remaining: ${state.remainingDistanceKm} km",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Map
                    LaundryInteractiveMap(
                        courierLat = state.currentTelemetry?.latitude ?: state.courier.currentLat,
                        courierLng = state.currentTelemetry?.longitude ?: state.courier.currentLng,
                        courierSpeedKmh = state.navigationSpeedKmh,
                        isCourierActive = true
                    )
                }
            }
        }

        // 3. Dual-Leg Dispatch Assignment Cards
        item {
            Text(
                text = "Dual-Leg Dispatch Assignments",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(assignedOrders) { order ->
            val isLeg1 = order.currentLeg == DispatchLeg.LEG_1_CUSTOMER_TO_LAUNDROMAT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Leg Badge & Order ID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLeg1) OceanBluePrimary.copy(alpha = 0.15f) else CoralExpress.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isLeg1) "LEG 1: Pickup from Customer" else "LEG 2: Return to Customer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLeg1) OceanBluePrimary else CoralExpress
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "#${order.id}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Route details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null,
                            tint = OceanBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "From: " + if (isLeg1) order.customerLocation.name else order.laundromat.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "To: " + if (isLeg1) order.laundromat.name else order.customerLocation.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Client: ${order.customerName} • ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Address: ${order.customerAddressDetail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons depending on state
                    when (order.state) {
                        is OrderState.DriverAssignedForPickup -> {
                            Button(
                                onClick = { onPickUpFromCustomer(order.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("courier_pickup_button_${order.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary)
                            ) {
                                Text("Confirm Laundry Collected (Leg 1)")
                            }
                        }
                        is OrderState.PickedUp -> {
                            Button(
                                onClick = { onDeliverToLaundromat(order.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("courier_deliver_laundromat_${order.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = MintFoamSecondary)
                            ) {
                                Text("Handover at Laundromat Hub")
                            }
                        }
                        is OrderState.DriverAssignedForDropoff -> {
                            Button(
                                onClick = { onOpenDeliveryModal(order) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("courier_verify_delivery_${order.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = CoralExpress)
                            ) {
                                Text("Verify Client Delivery (OTP + Signature)")
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Current State: ${order.state.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delivery Confirmation Modal Sheet with OTP & Touch Signature Pad
    if (state.isVerificationModalOpen && state.selectedDeliveryOrder != null) {
        val order = state.selectedDeliveryOrder
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onCloseDeliveryModal,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("delivery_verification_modal")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leg 2: Delivery Verification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onCloseDeliveryModal) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ask customer (${order.customerName}) for their 4-digit PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.enteredOtp,
                    onValueChange = onOtpEntered,
                    label = { Text("4-Digit Client Delivery PIN (e.g. ${order.deliveryOtp})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.otpError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_otp_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.otpError != null) {
                    Text(
                        text = state.otpError,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusError,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Touch Signature Pad
                SignaturePad(
                    onSignatureConfirmed = { signatureToken ->
                        onConfirmDelivery(signatureToken)
                    },
                    onCancel = onCloseDeliveryModal
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
