package com.example.ui.screens.vendor

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Order
import com.example.domain.model.OrderState
import com.example.domain.model.ProcessingStage
import com.example.ui.theme.CoralExpress
import com.example.ui.theme.MintFoamSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.VendorUiState

@Composable
fun VendorPortalScreen(
    state: VendorUiState,
    orders: List<Order>,
    onAcceptOrder: (String) -> Unit,
    onMarkOrderAtLaundromat: (String) -> Unit,
    onAdvanceProcessing: (String, ProcessingStage) -> Unit,
    onReadyForDispatch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingIncoming = orders.filter { it.state is OrderState.Created }
    val inHubOrders = orders.filter {
        it.state is OrderState.AtLaundromat || it.state is OrderState.Processing
    }
    val otherOrders = orders.filter {
        it.state !is OrderState.Created && it.state !is OrderState.AtLaundromat && it.state !is OrderState.Processing
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hub Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.selectedLaundromat.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Nasarawa Corridor Pilot Partner Hub",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MintFoamSecondary
                        ) {
                            Text(
                                text = "OPEN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Active Washers",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                            Text(
                                text = "${state.selectedLaundromat.activeWashers} Units",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Column {
                            Text(
                                text = "Daily Capacity",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                            Text(
                                text = "${state.selectedLaundromat.totalCapacityKg} Kg",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Column {
                            Text(
                                text = "Corridor Queue",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                            Text(
                                text = "${orders.size} Orders",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralExpress
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Incoming Orders with 2-Minute Countdown Timer & Audio/Visual Alert
        if (pendingIncoming.isNotEmpty()) {
            item {
                val topPending = pendingIncoming.first()
                val progress = state.pendingOrderCountdownSeconds / 120f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            if (state.alertAudioTriggered) CoralExpress else OceanBluePrimary,
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.alertAudioTriggered)
                            Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CoralExpress.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Alert",
                                        tint = CoralExpress,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "NEW ORDER INCOMING!",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CoralExpress
                                        )
                                    )
                                    Text(
                                        text = "Order #${topPending.id} • ${topPending.totalGarmentsCount} Garments",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // 2-Minute Circular Countdown Timer
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(54.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (state.pendingOrderCountdownSeconds < 30) CoralExpress else OceanBluePrimary,
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    text = "${state.pendingOrderCountdownSeconds}s",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Pickup Zone: ${topPending.customerLocation.name} (${topPending.customerAddressDetail})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Payout: ₦%,d (Paid via Paystack)".format(topPending.subtotalNgn),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { /* Decline */ },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vendor_decline_order_button")
                            ) {
                                Text("Decline")
                            }

                            Button(
                                onClick = { onAcceptOrder(topPending.id) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("vendor_accept_order_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary)
                            ) {
                                Text("Accept Order (${state.pendingOrderCountdownSeconds}s)")
                            }
                        }
                    }
                }
            }
        }

        // 3. Processing Status Updater Board (Washing -> Drying -> Ironing -> Ready for Dispatch)
        item {
            Text(
                text = "In-Laundromat Active Processing Queue",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (inHubOrders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No laundry currently in machine drums. Accepted orders will arrive from couriers.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(inHubOrders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order #${order.id} (${order.customerName})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintFoamSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = order.state.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MintFoamSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${order.totalGarmentsCount} Garments • ${order.serviceType.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Processing stage progression buttons
                    when (val s = order.state) {
                        is OrderState.AtLaundromat -> {
                            Button(
                                onClick = {
                                    onAdvanceProcessing(order.id, ProcessingStage.WASHING)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_start_wash_${order.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary)
                            ) {
                                Icon(Icons.Default.LocalLaundryService, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Load into Washer (Washing Stage)")
                            }
                        }
                        is OrderState.Processing -> {
                            val nextStage = when (s.stage) {
                                ProcessingStage.WASHING -> "Advance to Drying Stage"
                                ProcessingStage.DRYING -> "Advance to Steam Ironing"
                                ProcessingStage.IRONING -> "Seal in Protective Garment Bag"
                                ProcessingStage.READY_FOR_DISPATCH -> "Notify Courier for Return Dispatch"
                            }

                            Button(
                                onClick = {
                                    if (s.stage == ProcessingStage.READY_FOR_DISPATCH) {
                                        onReadyForDispatch(order.id)
                                    } else {
                                        onAdvanceProcessing(order.id, s.stage)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vendor_advance_stage_${order.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = MintFoamSecondary)
                            ) {
                                Text(nextStage)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // 4. Other Orders in Corridor
        if (otherOrders.isNotEmpty()) {
            item {
                Text(
                    text = "En Route / Completed Corridor Orders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(otherOrders) { order ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order #${order.id} • ${order.customerName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = order.state.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₦%,d".format(order.totalNgn),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
