package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GeoPoint
import com.example.domain.model.LaundryCatalog
import com.example.domain.model.Order
import com.example.domain.model.PilotLocations
import com.example.domain.model.ServiceType
import com.example.ui.components.LaundryInteractiveMap
import com.example.ui.theme.CoralExpress
import com.example.ui.theme.ElectricAqua
import com.example.ui.theme.MintFoamSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlueLight
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.viewmodel.CustomerUiState

@Composable
fun CustomerHomeScreen(
    state: CustomerUiState,
    activeOrders: List<Order>,
    onSelectService: (ServiceType) -> Unit,
    onSelectLocation: (GeoPoint) -> Unit,
    onUpdateAddressDetail: (String) -> Unit,
    onUpdateItemQuantity: (String, Int) -> Unit,
    onCheckoutClicked: () -> Unit,
    onTrackOrderClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Active Order Alert Banner (if courier is en route)
            if (activeOrders.isNotEmpty()) {
                val latest = activeOrders.first()
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTrackOrderClicked(latest.id) }
                            .testTag("active_order_tracking_banner"),
                        colors = CardDefaults.cardColors(containerColor = OceanBluePrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = "Rider Active",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Active Order: #${latest.id}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = latest.state.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = OceanBlueLight
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "Track Live",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBluePrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Service Catalog (Wash & Fold, Dry Cleaning, Ironing, Express)
            item {
                Column {
                    Text(
                        text = "Select Service Tier",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ServiceType.values().forEach { service ->
                            val isSelected = state.selectedService == service
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) OceanBluePrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectService(service) }
                                    .testTag("service_card_${service.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Icon(
                                        imageVector = when (service) {
                                            ServiceType.EXPRESS_TURNAROUND -> Icons.Default.Speed
                                            else -> Icons.Default.LocalLaundryService
                                        },
                                        contentDescription = service.title,
                                        tint = if (isSelected) Color.White else Color(service.colorHex),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = service.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${service.baseTurnaroundHours}h turnaround",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = if (isSelected) OceanBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Smart Address Input & Micro-zone Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = OceanBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pickup Micro-zone (Nasarawa Corridor)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Corridor Micro-zone Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PilotLocations.ALL_PRESETS.take(6).forEach { preset ->
                                val isSelected = state.selectedLocation.name == preset.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectLocation(preset) },
                                    label = {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OceanBluePrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = OceanBluePrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.addressDetails,
                            onValueChange = onUpdateAddressDetail,
                            label = { Text("Exact Street, Compound or Estate Details") },
                            placeholder = { Text("e.g. House 4, Behind Secretariat, Karu Site") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_address_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Map Snapshot
                        LaundryInteractiveMap(
                            customerLoc = state.selectedLocation,
                            courierLat = state.latestCourierTelemetry?.latitude ?: 9.0220,
                            courierLng = state.latestCourierTelemetry?.longitude ?: 7.5700,
                            courierSpeedKmh = state.latestCourierTelemetry?.speedKmh ?: 35f,
                            isCourierActive = activeOrders.isNotEmpty()
                        )
                    }
                }
            }

            // 4. Laundry Items Counter Catalog
            item {
                Text(
                    text = "Add Laundry Garments",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(LaundryCatalog.ITEMS) { item ->
                val quantity = state.basket[item.id] ?: 0
                val unitPriceEffective = (item.basePriceNgn * state.selectedService.expressMultiplier).toInt()

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("laundry_item_card_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "₦%,d each  •  %s".format(unitPriceEffective, item.category),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.defaultNotes.isNotEmpty()) {
                                Text(
                                    text = "Tip: ${item.defaultNotes}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MintFoamSecondary
                                )
                            }
                        }

                        // Quantity Counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onUpdateItemQuantity(item.id, -1) },
                                enabled = quantity > 0,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (quantity > 0) Color(0xFFF1F5F9) else Color(0xFFF8FAFC))
                                    .testTag("item_minus_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = if (quantity > 0) NavyDark else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$quantity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.width(22.dp)
                            )

                            IconButton(
                                onClick = { onUpdateItemQuantity(item.id, 1) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(OceanBluePrimary.copy(alpha = 0.12f))
                                    .testTag("item_plus_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = OceanBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Bottom Checkout Bar
        if (state.totalItemsCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                color = NavyDark,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${state.totalItemsCount} Garments Selected",
                            style = MaterialTheme.typography.labelSmall.copy(color = OceanBlueLight)
                        )
                        Text(
                            text = "₦%,d".format(state.totalNgn),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }

                    Button(
                        onClick = onCheckoutClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("proceed_checkout_button")
                    ) {
                        Text(
                            text = "Checkout",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
