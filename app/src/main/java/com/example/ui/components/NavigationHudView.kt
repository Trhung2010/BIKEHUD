package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudColorTheme
import com.example.model.NavStep
import com.example.model.RoutePreset
import com.example.model.TurnType

@Composable
fun NavigationHudView(
    route: RoutePreset,
    currentStep: NavStep,
    stepIndex: Int,
    totalSteps: Int,
    remainingMeters: Int,
    compassHeading: Float,
    colorTheme: HudColorTheme,
    onToggleToGoogleMaps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val distanceDisplay = if (remainingMeters >= 1000) {
        String.format("%.1f km", remainingMeters / 1000f)
    } else {
        "$remainingMeters m"
    }

    val stepProgress = (1f - (remainingMeters.toFloat() / currentStep.distanceMeters.coerceAtLeast(1))).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .testTag("nav_hud_view_container")
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0F1D).copy(alpha = 0.85f))
            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top info bar: Destination & Route name + Google Maps toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = "Chỉ đường xe máy",
                    tint = colorTheme.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = route.name,
                    color = colorTheme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onToggleToGoogleMaps != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1A73E8).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF1A73E8), RoundedCornerShape(6.dp))
                            .clickable { onToggleToGoogleMaps() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("switch_to_google_maps_btn")
                    ) {
                        Text(
                            text = "BẢN ĐỒ G-MAPS",
                            color = Color(0xFF64B5F6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Step counter
                Text(
                    text = "${stepIndex + 1}/$totalSteps",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Turn Indicator & Countdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Turn Icon Box with Glowing Neon Border
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorTheme.primaryColor.copy(alpha = 0.15f))
                    .border(2.dp, colorTheme.primaryColor, RoundedCornerShape(12.dp))
                    .testTag("turn_icon_box"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTurnIcon(currentStep.turnType),
                    contentDescription = currentStep.instruction,
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Distance to turn and Next street
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = distanceDisplay,
                    color = colorTheme.accentColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = currentStep.streetName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = currentStep.instruction,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar towards this turn
        LinearProgressIndicator(
            progress = { stepProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colorTheme.primaryColor,
            trackColor = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Bar: Lane info + Compass Radar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lane Assist
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorTheme.secondaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentStep.laneInfo,
                    color = colorTheme.secondaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Compass Bearing (e.g. 195° S)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "La bàn",
                    tint = colorTheme.primaryColor,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(compassHeading)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val headingCardinal = getCardinalDirection(compassHeading)
                Text(
                    text = "${compassHeading.toInt()}° $headingCardinal",
                    color = colorTheme.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mini Vector Road Horizon (HUD radar strip)
        MiniVectorRoadCanvas(
            turnType = currentStep.turnType,
            colorTheme = colorTheme,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        )
    }
}

@Composable
private fun MiniVectorRoadCanvas(
    turnType: TurnType,
    colorTheme: HudColorTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ground perspective guide lines
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, h),
            end = Offset(w * 0.4f, 0f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(w, h),
            end = Offset(w * 0.6f, 0f),
            strokeWidth = 1.dp.toPx()
        )

        // Trajectory line based on turn type
        val path = Path()
        val bottomCenter = Offset(w / 2f, h)
        path.moveTo(bottomCenter.x, bottomCenter.y)

        when (turnType) {
            TurnType.TURN_LEFT, TurnType.SHARP_LEFT, TurnType.SLIGHT_LEFT -> {
                path.cubicTo(
                    w * 0.45f, h * 0.6f,
                    w * 0.25f, h * 0.4f,
                    w * 0.15f, h * 0.1f
                )
            }
            TurnType.TURN_RIGHT, TurnType.SHARP_RIGHT, TurnType.SLIGHT_RIGHT -> {
                path.cubicTo(
                    w * 0.55f, h * 0.6f,
                    w * 0.75f, h * 0.4f,
                    w * 0.85f, h * 0.1f
                )
            }
            TurnType.U_TURN -> {
                path.cubicTo(
                    w * 0.4f, h * 0.5f,
                    w * 0.2f, h * 0.2f,
                    w * 0.35f, h * 0.8f
                )
            }
            else -> {
                // Straight path
                path.lineTo(w / 2f, 0f)
            }
        }

        drawPath(
            path = path,
            color = colorTheme.primaryColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

private fun getTurnIcon(type: TurnType): ImageVector {
    return when (type) {
        TurnType.STRAIGHT -> Icons.Default.Navigation
        TurnType.SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
        TurnType.TURN_RIGHT -> Icons.Default.TurnRight
        TurnType.SHARP_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        TurnType.SLIGHT_LEFT -> Icons.Default.TurnSlightLeft
        TurnType.TURN_LEFT -> Icons.Default.TurnLeft
        TurnType.SHARP_LEFT -> Icons.AutoMirrored.Filled.TrendingFlat
        TurnType.U_TURN -> Icons.Default.RotateLeft
        TurnType.ROUNDABOUT -> Icons.Default.NearMe
        TurnType.DESTINATION -> Icons.Default.AssistantPhoto
    }
}

private fun getCardinalDirection(angle: Float): String {
    val normalized = (angle % 360 + 360) % 360
    return when {
        normalized in 22.5..67.5 -> "NE"
        normalized in 67.5..112.5 -> "E"
        normalized in 112.5..157.5 -> "SE"
        normalized in 157.5..202.5 -> "S"
        normalized in 202.5..247.5 -> "SW"
        normalized in 247.5..292.5 -> "W"
        normalized in 292.5..337.5 -> "NW"
        else -> "N"
    }
}
