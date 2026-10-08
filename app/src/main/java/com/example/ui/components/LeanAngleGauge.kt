package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudColorTheme
import kotlin.math.abs

@Composable
fun LeanAngleGauge(
    leanAngle: Float,
    maxLeft: Float,
    maxRight: Float,
    colorTheme: HudColorTheme,
    modifier: Modifier = Modifier
) {
    val roundedAngle = leanAngle.toInt()
    val isLeaningLeft = roundedAngle < -2
    val isLeaningRight = roundedAngle > 2

    Box(
        modifier = modifier
            .testTag("lean_angle_gauge")
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF090E1A).copy(alpha = 0.9f))
            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Row: Max Left / Current / Max Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Max Left
                Text(
                    text = "L ${maxLeft.toInt()}°",
                    color = if (isLeaningLeft) colorTheme.primaryColor else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Current Lean Center Value
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = "Góc nghiêng xe",
                        tint = if (abs(roundedAngle) > 25) colorTheme.warningColor else colorTheme.primaryColor,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(-leanAngle)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${abs(roundedAngle)}° ${if (roundedAngle < 0) "TRÁI" else if (roundedAngle > 0) "PHẢI" else "CÂN BẰNG"}",
                        color = if (abs(roundedAngle) > 30) colorTheme.warningColor else colorTheme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Max Right
                Text(
                    text = "R ${maxRight.toInt()}°",
                    color = if (isLeaningRight) colorTheme.primaryColor else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Gyroscopic Horizon Arc
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.8f

                // Horizon reference line
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(cx - 80.dp.toPx(), cy),
                    end = Offset(cx + 80.dp.toPx(), cy),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center zero notch
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(cx, cy - 6.dp.toPx()),
                    end = Offset(cx, cy + 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Left limit mark (-45°)
                val leftX = cx - 60.dp.toPx()
                drawLine(
                    color = Color(0xFFFF5252).copy(alpha = 0.5f),
                    start = Offset(leftX, cy - 4.dp.toPx()),
                    end = Offset(leftX, cy + 4.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Right limit mark (+45°)
                val rightX = cx + 60.dp.toPx()
                drawLine(
                    color = Color(0xFFFF5252).copy(alpha = 0.5f),
                    start = Offset(rightX, cy - 4.dp.toPx()),
                    end = Offset(rightX, cy + 4.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Active Lean Indicator Bar (shifts and tilts with leanAngle)
                val clamped = leanAngle.coerceIn(-50f, 50f)
                val xShift = (clamped / 50f) * 60.dp.toPx()
                val barColor = if (abs(clamped) > 35) colorTheme.warningColor else colorTheme.primaryColor

                // Indicator pointer
                drawCircle(
                    color = barColor,
                    radius = 4.dp.toPx(),
                    center = Offset(cx + xShift, cy)
                )

                drawLine(
                    color = barColor,
                    start = Offset(cx + xShift, cy - 8.dp.toPx()),
                    end = Offset(cx + xShift, cy + 8.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
