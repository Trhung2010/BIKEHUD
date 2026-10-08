package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.HudColorTheme
import com.example.model.RideTelemetry
import com.example.model.SpeedUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Contrast Digital Speedometer UI Component optimized for Windshield Projection (HUD).
 *
 * Key Windshield HUD Optimizations:
 * 1. True OLED Black (#000000) base to eliminate windshield backlight glare and halo bleed.
 * 2. Segmented Neon Phosphor Speed Display with faint ghost "888" backdrop for maximum optical depth.
 * 3. 36-Segment Radial Velocity Arc that transitions smoothly through Cruise, Threshold, and Overspeed Strobe.
 * 4. Real-time acceleration / deceleration trend vector (+/- km/h/s delta).
 * 5. Instant windshield mirror flip toggle (horizontal inversion via scaleX = -1f).
 * 6. Fullscreen Windshield Projection dialog for placing device flat beneath vehicle glass.
 */
@Composable
fun SpeedometerGauge(
    telemetry: RideTelemetry,
    unit: SpeedUnit,
    colorTheme: HudColorTheme,
    isNightMode: Boolean,
    onOpenSpeedLimitDialog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showFullscreenHud by remember { mutableStateOf(false) }

    // Acceleration delta tracking
    var previousSpeed by remember { mutableFloatStateOf(telemetry.speedKmh) }
    var speedDelta by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(telemetry.speedKmh) {
        speedDelta = telemetry.speedKmh - previousSpeed
        previousSpeed = telemetry.speedKmh
    }

    val displaySpeed = (telemetry.speedKmh * unit.factor).toInt()
    val displayLimit = (telemetry.currentSpeedLimit * unit.factor).toInt()
    val isOverSpeed = telemetry.isOverSpeed

    // Overspeed strobe animation
    val infiniteTransition = rememberInfiniteTransition(label = "speedometer_strobe")
    val strobePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobePulse"
    )

    val warningColor = if (isOverSpeed) {
        colorTheme.warningColor.copy(alpha = strobePulse)
    } else {
        colorTheme.primaryColor
    }

    val animatedSpeedColor by animateColorAsState(
        targetValue = if (isOverSpeed) colorTheme.warningColor else colorTheme.primaryColor,
        animationSpec = tween(200),
        label = "animatedSpeedColor"
    )

    Column(
        modifier = modifier
            .testTag("speedometer_gauge_container")
            .background(Color.Black)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main Speedometer Dial Container
        Box(
            modifier = Modifier.size(246.dp),
            contentAlignment = Alignment.Center
        ) {
            // High-Contrast Radial Multi-Segment Arc Canvas
            RadialSpeedometerArcCanvas(
                speedKmh = telemetry.speedKmh,
                speedLimitKmh = telemetry.currentSpeedLimit.toFloat(),
                maxSpeedScale = 160f,
                rpm = telemetry.rpm,
                isOverSpeed = isOverSpeed,
                colorTheme = colorTheme,
                isNightMode = isNightMode,
                strobePulse = strobePulse,
                modifier = Modifier.size(240.dp)
            )

            // Centered High-Contrast Digital Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Top Telemetry Mini-Row: Gear + Acceleration Vector + RPM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    // Gear Box (OLED Glowing Badge)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (telemetry.gear == "N") Color(0xFF00E676).copy(alpha = 0.25f)
                                else colorTheme.primaryColor.copy(alpha = 0.2f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (telemetry.gear == "N") Color(0xFF00E676) else colorTheme.primaryColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (telemetry.gear == "N") "GEAR N" else "SỐ ${telemetry.gear}",
                            color = if (telemetry.gear == "N") Color(0xFF00E676) else colorTheme.primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Acceleration Trend Chevron
                    val accelColor = when {
                        speedDelta > 0.4f -> Color(0xFF00E676)
                        speedDelta < -0.4f -> Color(0xFFFF9100)
                        else -> colorTheme.accentColor.copy(alpha = 0.6f)
                    }
                    val accelText = when {
                        speedDelta > 0.4f -> "▲ GA"
                        speedDelta < -0.4f -> "▼ PHANH"
                        else -> "● ĐỀU"
                    }
                    Text(
                        text = accelText,
                        color = accelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // RPM Readout
                    Text(
                        text = "${telemetry.rpm} RPM",
                        color = if (telemetry.rpm > 9000) colorTheme.warningColor else colorTheme.primaryColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Digital HUD Speed Readout with Ghost Backdrop ("888")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(86.dp)
                ) {
                    // Ghost Backdrop Segments for 3D Digital HUD optical effect on windshield glass
                    Text(
                        text = "888",
                        color = colorTheme.primaryColor.copy(alpha = 0.06f),
                        fontSize = 82.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-3).sp,
                        textAlign = TextAlign.Center
                    )

                    // Active Glowing Digital Speed Readout
                    Text(
                        text = "$displaySpeed",
                        color = animatedSpeedColor,
                        fontSize = 82.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-3).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("hud_speed_digits")
                    )
                }

                // Speed Unit Badge (KM/H or MPH)
                Text(
                    text = unit.label.uppercase(Locale.US),
                    color = colorTheme.accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.5.sp
                )
            }

            // Fullscreen Windshield Projection Expand Icon (Top Right)
            IconButton(
                onClick = { showFullscreenHud = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111827).copy(alpha = 0.8f))
                    .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.4f), CircleShape)
                    .testTag("expand_fullscreen_hud_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Chiếu kính toàn màn hình",
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Speed Limit Road Sign & Visual Overspeed Warning
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .then(
                    if (onOpenSpeedLimitDialog != null) {
                        Modifier.clickable { onOpenSpeedLimitDialog() }
                    } else Modifier
                )
        ) {
            // Speed Limit Sign (Standard High-Contrast Road Sign Style)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 4.dp, color = Color(0xFFFF1744), shape = CircleShape)
                    .testTag("speed_limit_sign"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$displayLimit",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isOverSpeed) {
                val overDelta = (displaySpeed - displayLimit).coerceAtLeast(1)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorTheme.warningColor.copy(alpha = 0.25f))
                        .border(1.5.dp, colorTheme.warningColor.copy(alpha = strobePulse), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cảnh báo vượt tốc độ",
                        tint = warningColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VƯỢT +$overDelta ${unit.label.uppercase()}",
                        color = warningColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GIỚI HẠN: $displayLimit ${unit.label}",
                        color = colorTheme.primaryColor.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✎",
                        color = colorTheme.accentColor.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Trip Telemetry Stats Bar
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C101C))
                .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TripMetricItem(
                label = "TRIP",
                value = String.format(Locale.US, "%.1f km", telemetry.tripDistanceKm),
                color = colorTheme.accentColor
            )
            TripMetricItem(
                label = "MAX",
                value = String.format(Locale.US, "%.0f %s", telemetry.maxSpeedKmh * unit.factor, unit.label),
                color = colorTheme.primaryColor
            )
            val minutes = telemetry.rideDurationSeconds / 60
            val seconds = telemetry.rideDurationSeconds % 60
            TripMetricItem(
                label = "TIME",
                value = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                color = colorTheme.accentColor
            )
            TripMetricItem(
                label = "AVG",
                value = String.format(Locale.US, "%.0f %s", telemetry.averageSpeedKmh * unit.factor, unit.label),
                color = colorTheme.primaryColor
            )
        }
    }

    // Full-Screen Dedicated Windshield HUD Speedometer Dialog
    if (showFullscreenHud) {
        FullScreenSpeedometerHudDialog(
            telemetry = telemetry,
            unit = unit,
            colorTheme = colorTheme,
            isNightMode = isNightMode,
            onDismiss = { showFullscreenHud = false },
            onOpenSpeedLimitDialog = onOpenSpeedLimitDialog
        )
    }
}

/**
 * Draws high-contrast radial 36-segment velocity arc and RPM track on Canvas.
 */
@Composable
private fun RadialSpeedometerArcCanvas(
    speedKmh: Float,
    speedLimitKmh: Float,
    maxSpeedScale: Float,
    rpm: Int,
    isOverSpeed: Boolean,
    colorTheme: HudColorTheme,
    isNightMode: Boolean,
    strobePulse: Float,
    modifier: Modifier = Modifier
) {
    val primaryCol = colorTheme.primaryColor
    val accentCol = colorTheme.accentColor
    val warnCol = colorTheme.warningColor

    Canvas(modifier = modifier) {
        val strokeW = 12.dp.toPx()
        val radius = (size.minDimension - strokeW) / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)

        val startAngle = 135f
        val totalSweep = 270f
        val totalSegments = 36

        val speedFraction = (speedKmh / maxSpeedScale).coerceIn(0f, 1f)
        val activeSegments = (speedFraction * totalSegments).toInt()
        val limitFraction = (speedLimitKmh / maxSpeedScale).coerceIn(0f, 1f)
        val limitSegmentIndex = (limitFraction * totalSegments).toInt()

        // Background inactive track (dim blocks)
        drawArc(
            color = Color(0xFF141A28).copy(alpha = if (isNightMode) 0.3f else 0.45f),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Draw segmented tick blocks for high visual scanning through reflections
        val segSweep = totalSweep / totalSegments
        val gapAngle = 1.2f

        for (i in 0 until totalSegments) {
            val segStart = startAngle + i * segSweep + gapAngle / 2f
            val segAngle = segSweep - gapAngle

            val isActive = i <= activeSegments
            val isBeyondLimit = i > limitSegmentIndex

            val segColor = when {
                !isActive -> Color(0xFF1E293B).copy(alpha = 0.35f)
                isOverSpeed -> warnCol.copy(alpha = strobePulse)
                isBeyondLimit -> Color(0xFFFF9100)
                else -> {
                    val ratio = i.toFloat() / totalSegments
                    if (ratio < 0.6f) primaryCol else accentCol
                }
            }

            drawArc(
                color = segColor,
                startAngle = segStart,
                sweepAngle = segAngle,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Butt)
            )
        }

        // Inner RPM progress ring (radius - 18dp)
        val innerRadius = radius - 18.dp.toPx()
        val rpmRatio = (rpm / 12000f).coerceIn(0f, 1f)
        val rpmSweep = totalSweep * rpmRatio

        drawArc(
            color = Color(0xFF1E293B).copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(centerOffset.x - innerRadius, centerOffset.y - innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val rpmBrush = Brush.sweepGradient(
            0.0f to primaryCol,
            0.7f to accentCol,
            0.85f to warnCol,
            1.0f to warnCol
        )

        drawArc(
            brush = rpmBrush,
            startAngle = startAngle,
            sweepAngle = rpmSweep,
            useCenter = false,
            topLeft = Offset(centerOffset.x - innerRadius, centerOffset.y - innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Speed limit tick mark on outer rim
        val limitAngleDeg = startAngle + (limitFraction * totalSweep)
        val limitAngleRad = Math.toRadians(limitAngleDeg.toDouble())
        val tickInnerR = radius - 8.dp.toPx()
        val tickOuterR = radius + 8.dp.toPx()

        drawLine(
            color = Color(0xFFFF1744),
            start = Offset(
                (centerOffset.x + tickInnerR * cos(limitAngleRad)).toFloat(),
                (centerOffset.y + tickInnerR * sin(limitAngleRad)).toFloat()
            ),
            end = Offset(
                (centerOffset.x + tickOuterR * cos(limitAngleRad)).toFloat(),
                (centerOffset.y + tickOuterR * sin(limitAngleRad)).toFloat()
            ),
            strokeWidth = 3.dp.toPx()
        )
    }
}

/**
 * Dedicated Fullscreen HUD Speedometer optimized for laying the phone flat on the vehicle dashboard.
 * Features 1-tap horizontal mirror reflection (scaleX = -1f) and extra-large digits.
 */
@Composable
fun FullScreenSpeedometerHudDialog(
    telemetry: RideTelemetry,
    unit: SpeedUnit,
    colorTheme: HudColorTheme,
    isNightMode: Boolean,
    onDismiss: () -> Unit,
    onOpenSpeedLimitDialog: (() -> Unit)? = null
) {
    var isMirrorMode by remember { mutableStateOf(false) }
    val displaySpeed = (telemetry.speedKmh * unit.factor).toInt()
    val displayLimit = (telemetry.currentSpeedLimit * unit.factor).toInt()
    val isOverSpeed = telemetry.isOverSpeed

    val infiniteTransition = rememberInfiniteTransition(label = "fullscreen_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_speedometer_hud"),
            color = Color.Black // True OLED pure black
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .then(
                        if (isOverSpeed) {
                            Modifier.border(
                                6.dp,
                                Color(0xFFFF1744).copy(alpha = pulseAlpha),
                                RoundedCornerShape(16.dp)
                            )
                        } else Modifier
                    )
            ) {
                // Mirrorable Projection Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (isMirrorMode) {
                                scaleX = -1f // Flips horizontally for windshield glass reflection
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Top HUD Status Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "GEAR ${telemetry.gear}",
                                color = if (telemetry.gear == "N") Color(0xFF00E676) else colorTheme.primaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${telemetry.rpm} RPM",
                                color = colorTheme.accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "HƯỚNG ${telemetry.compassHeadingDegrees.toInt()}°",
                                color = Color(0xFF94A3B8),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Massive Digital HUD Speed Readout
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(160.dp)
                        ) {
                            Text(
                                text = "888",
                                color = colorTheme.primaryColor.copy(alpha = 0.05f),
                                fontSize = 150.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-6).sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "$displaySpeed",
                                color = if (isOverSpeed) Color(0xFFFF1744) else colorTheme.primaryColor,
                                fontSize = 150.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-6).sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = unit.label.uppercase(Locale.US),
                            color = colorTheme.accentColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Overspeed Alert / Limit Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable { onOpenSpeedLimitDialog?.invoke() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(5.dp, Color(0xFFFF1744), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$displayLimit",
                                    color = Color.Black,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            if (isOverSpeed) {
                                Text(
                                    text = "VƯỢT GIỚI HẠN! GIẢM TỐC!",
                                    color = Color(0xFFFF1744),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = "GIỚI HẠN: $displayLimit ${unit.label.uppercase()}",
                                    color = colorTheme.primaryColor.copy(alpha = 0.9f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Controls Overlay (Directly readable, un-flipped for easy touching)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Windshield Mirror Flip Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMirrorMode) colorTheme.primaryColor.copy(alpha = 0.3f)
                                else Color(0xFF1E293B)
                            )
                            .border(
                                1.5.dp,
                                if (isMirrorMode) colorTheme.primaryColor else Color(0xFF475569),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { isMirrorMode = !isMirrorMode }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Lật gương kính chắn gió",
                            tint = if (isMirrorMode) colorTheme.primaryColor else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMirrorMode) "LẬT CHIẾU KÍNH (BẬT)" else "LẬT KÍNH HUD",
                            color = if (isMirrorMode) colorTheme.primaryColor else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Close Fullscreen Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
