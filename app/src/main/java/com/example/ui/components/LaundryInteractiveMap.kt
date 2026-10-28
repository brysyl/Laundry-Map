package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.GeoPoint
import com.example.domain.model.PilotLocations
import com.example.ui.theme.CoralExpress
import com.example.ui.theme.ElectricAqua
import com.example.ui.theme.MintFoamSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBluePrimary

@Composable
fun LaundryInteractiveMap(
    customerLoc: GeoPoint = PilotLocations.KARU_SITE,
    laundromatLoc: GeoPoint = PilotLocations.MARARABA_CHECKPOINT,
    courierLat: Double = 9.0220,
    courierLng: Double = 7.5700,
    courierHeading: Float = 45f,
    courierSpeedKmh: Float = 36f,
    isCourierActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    var mapFocus by remember { mutableStateOf("ALL") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
            .testTag("interactive_laundry_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Map Base Background Grid & Micro-topography
            val gridColor = Color(0xFFD8E2DC).copy(alpha = 0.6f)
            val step = 40.dp.toPx()
            for (x in 0..(w / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(h / step).toInt()) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y * step),
                    end = Offset(w, y * step),
                    strokeWidth = 1f
                )
            }

            // 2. Draw Major Corridor Arterials (Keffi - Abuja Expressway corridor)
            val roadColor = Color(0xFFFFFFFF)
            val roadBorder = Color(0xFF94A3B8)

            // Main Expressway Spine from Mararaba (Top Right) to Karu / Nyanya (Bottom Left)
            val roadPath = Path().apply {
                moveTo(w * 0.15f, h * 0.82f) // Karu Site area
                cubicTo(
                    w * 0.35f, h * 0.68f,
                    w * 0.45f, h * 0.45f,
                    w * 0.82f, h * 0.22f  // Mararaba / Nyanya
                )
            }
            // Expressway Casing
            drawPath(
                path = roadPath,
                color = roadBorder,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
            // Expressway Inner
            drawPath(
                path = roadPath,
                color = roadColor,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Feeder Road (Ado Junction branch)
            val feederPath = Path().apply {
                moveTo(w * 0.45f, h * 0.52f)
                lineTo(w * 0.85f, h * 0.75f) // Ado
            }
            drawPath(
                path = feederPath,
                color = roadColor,
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Coordinate Projections (Normalized between Karu and Mararaba bounds)
            val customerPos = Offset(w * 0.22f, h * 0.74f)
            val laundromatPos = Offset(w * 0.78f, h * 0.26f)

            // Dynamic Courier position interpolated or offset along route
            val courierProgress = ((courierLat - 8.98) / 0.08).coerceIn(0.15, 0.85).toFloat()
            val courierPos = Offset(
                x = customerPos.x + (laundromatPos.x - customerPos.x) * courierProgress,
                y = customerPos.y + (laundromatPos.y - customerPos.y) * courierProgress
            )

            // 4. Draw Route Polyline
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
            val routePath = Path().apply {
                moveTo(customerPos.x, customerPos.y)
                lineTo(courierPos.x, courierPos.y)
                lineTo(laundromatPos.x, laundromatPos.y)
            }
            drawPath(
                path = routePath,
                color = OceanBluePrimary.copy(alpha = 0.85f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = dashEffect,
                    cap = StrokeCap.Round
                )
            )

            // 5. Draw Customer Location Pin
            drawCircle(
                color = OceanBluePrimary.copy(alpha = 0.25f),
                radius = 16.dp.toPx(),
                center = customerPos
            )
            drawCircle(
                color = OceanBluePrimary,
                radius = 8.dp.toPx(),
                center = customerPos
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = customerPos
            )

            // 6. Draw Laundromat Hub Pin
            drawCircle(
                color = MintFoamSecondary.copy(alpha = 0.25f),
                radius = 18.dp.toPx(),
                center = laundromatPos
            )
            drawCircle(
                color = MintFoamSecondary,
                radius = 9.dp.toPx(),
                center = laundromatPos
            )
            drawCircle(
                color = Color.White,
                radius = 4.5.dp.toPx(),
                center = laundromatPos
            )

            // 7. Draw Moving Courier Bike Marker
            if (isCourierActive) {
                // Animated Pulse Radar Ring
                drawCircle(
                    color = CoralExpress.copy(alpha = pulseAlpha),
                    radius = pulseRadius.dp.toPx(),
                    center = courierPos
                )
                // Vehicle Base Circle
                drawCircle(
                    color = Color.White,
                    radius = 13.dp.toPx(),
                    center = courierPos
                )
                drawCircle(
                    color = CoralExpress,
                    radius = 10.dp.toPx(),
                    center = courierPos
                )
            }
        }

        // Overlay: Map Landmarks & Badges
        // Customer Pin Label
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 48.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OceanBluePrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customerLoc.name.take(18),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = NavyDark
                )
            }
        }

        // Laundromat Pin Label
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 12.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MintFoamSecondary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = laundromatLoc.name.take(18),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = NavyDark
                )
            }
        }

        // Bottom Map Bar: Corridor Zone + Telemetry Speed Badge
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Corridor",
                    tint = OceanBluePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nasarawa Corridor (Karu ↔ Mararaba)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = NavyDark
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CoralExpress.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBike,
                        contentDescription = "Courier Speed",
                        tint = CoralExpress,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${courierSpeedKmh.toInt()} km/h",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CoralExpress
                    )
                }
            }
        }
    }
}
