package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OceanBluePrimary

@Composable
fun SignaturePad(
    onSignatureConfirmed: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val points = remember { mutableStateListOf<Offset?>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Client Digital Sign-off",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Please sign in the box below to confirm delivery receipt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                .testTag("signature_canvas_box")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                points.add(offset)
                            },
                            onDrag = { change, _ ->
                                points.add(change.position)
                            },
                            onDragEnd = {
                                points.add(null) // separator between strokes
                            }
                        )
                    }
            ) {
                if (points.isNotEmpty()) {
                    val path = Path()
                    var isStart = true

                    for (p in points) {
                        if (p == null) {
                            isStart = true
                        } else if (isStart) {
                            path.moveTo(p.x, p.y)
                            isStart = false
                        } else {
                            path.lineTo(p.x, p.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = OceanBluePrimary,
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (points.isEmpty()) {
                Text(
                    text = "Sign here with finger",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { points.clear() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("signature_clear_button")
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    val signatureToken = "SIG_${System.currentTimeMillis()}_${points.size}_pts"
                    onSignatureConfirmed(signatureToken)
                },
                enabled = points.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("signature_confirm_button")
            ) {
                Text("Confirm Signature", fontWeight = FontWeight.Bold)
            }
        }
    }
}
