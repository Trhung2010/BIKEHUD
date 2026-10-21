package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.HudColorTheme
import com.example.model.RideTelemetry
import com.example.model.SpeedUnit
import java.util.Locale

@Composable
fun TripTelemetryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    telemetry: RideTelemetry,
    speedUnit: SpeedUnit,
    maxLeftLean: Float,
    maxRightLean: Float,
    best0to60: Float?,
    best0to100: Float?,
    colorTheme: HudColorTheme,
    onResetTrip: () -> Unit
) {
    if (!isOpen) return

    val hours = telemetry.rideDurationSeconds / 3600
    val minutes = (telemetry.rideDurationSeconds % 3600) / 60
    val seconds = telemetry.rideDurationSeconds % 60
    val formattedDuration = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val displayDistance = String.format(Locale.US, "%.1f", telemetry.tripDistanceKm * speedUnit.factor)
    val displayAvgSpeed = String.format(Locale.US, "%.1f", telemetry.averageSpeedKmh * speedUnit.factor)
    val displayMaxSpeed = String.format(Locale.US, "%.1f", telemetry.maxSpeedKmh * speedUnit.factor)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, colorTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("trip_telemetry_dialog"),
            color = Color(0xFF070B14)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorTheme.primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, colorTheme.primaryColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = colorTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ĐỒNG HỒ HÀNH TRÌNH TRIP",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Thống kê chuyến đi & kỷ lục xe máy",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Hero Distance & Time Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B1324))
                        .border(1.5.dp, colorTheme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Trip Distance
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QUÃNG ĐƯỜNG",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = displayDistance,
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (speedUnit == SpeedUnit.KMH) "KM" else "MI",
                                    color = colorTheme.primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(46.dp)
                                .background(Color(0xFF1E293B))
                        )

                        // Duration
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "THỜI GIAN CHẠY",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedDuration,
                                color = colorTheme.accentColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid: Avg Speed, Max Speed, Max Lean
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "TỐC ĐỘ TB",
                        value = displayAvgSpeed,
                        unit = speedUnit.label,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "TỐC ĐỘ MAX",
                        value = displayMaxSpeed,
                        unit = speedUnit.label,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lean Angle Max Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "NGHIÊNG TRÁI",
                        value = "${maxLeftLean.toInt()}°",
                        unit = "GÓC ÔM CUA",
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "NGHIÊNG PHẢI",
                        value = "${maxRightLean.toInt()}°",
                        unit = "GÓC ÔM CUA",
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))

                // Best Acceleration Records
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KỶ LỤC TĂNG TỐC TỐT NHẤT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecordBadgeCard(
                        title = "KỶ LỤC 0-60 KM/H",
                        timeSeconds = best0to60,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                    RecordBadgeCard(
                        title = "KỶ LỤC 0-100 KM/H",
                        timeSeconds = best0to100,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reset Trip Button
                ElevatedButton(
                    onClick = onResetTrip,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_trip_button")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ĐẶT LẠI ĐỒNG HỒ CHUYẾN ĐI (RESET TRIP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    colorTheme: HudColorTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(3.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(unit, color = colorTheme.primaryColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun RecordBadgeCard(
    title: String,
    timeSeconds: Float?,
    colorTheme: HudColorTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B1324))
            .border(1.dp, if (timeSeconds != null) Color(0xFFFFD600).copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (timeSeconds != null) String.format(Locale.US, "%.2f s", timeSeconds) else "Chưa có",
                color = if (timeSeconds != null) Color(0xFFFFD600) else Color(0xFF475569),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
