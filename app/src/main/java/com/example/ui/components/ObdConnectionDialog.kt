package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BluetoothDeviceInfo
import com.example.model.HudColorTheme
import com.example.model.ObdConnectionState
import com.example.model.ObdTelemetry
import com.example.model.TelemetrySource

@Composable
fun ObdConnectionDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    telemetrySource: TelemetrySource,
    onSelectSource: (TelemetrySource) -> Unit,
    obdTelemetry: ObdTelemetry,
    isGpsActive: Boolean,
    gpsAccuracyMeters: Float,
    pairedDevices: List<BluetoothDeviceInfo>,
    onRefreshDevices: () -> Unit,
    onConnectObd: (address: String, name: String) -> Unit,
    onDisconnectObd: () -> Unit,
    colorTheme: HudColorTheme
) {
    if (!isOpen) return

    val infiniteTransition = rememberInfiniteTransition(label = "obd_active_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "obdPulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, colorTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("obd_connection_dialog"),
            color = Color(0xFF090D16)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorTheme.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Usb,
                                contentDescription = null,
                                tint = colorTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nguồn Dữ Liệu HUD Thật",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "HUD GPS Vệ Tinh & Cổng OBD-II ELM327",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Selector Tabs: GPS vs OBD-II vs AUTO
                Text(
                    text = "CHỌN NGUỒN TỐC ĐỘ & THÔNG SỐ",
                    color = colorTheme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(TelemetrySource.HUD_OBD, TelemetrySource.HUD_GPS).forEach { src ->
                        val isSelected = telemetrySource == src
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) colorTheme.primaryColor.copy(alpha = 0.25f)
                                    else Color(0xFF1E293B)
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) colorTheme.primaryColor else Color(0xFF334155),
                                    RoundedCornerShape(10.dp)
                                )
                                .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelectSource(src) })
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (src == TelemetrySource.HUD_GPS) Icons.Default.GpsFixed else Icons.Default.Usb,
                                    contentDescription = null,
                                    tint = if (isSelected) colorTheme.primaryColor else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = src.badgeText,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Telemetry Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131B2E))
                        .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (obdTelemetry.connectionState == ObdConnectionState.CONNECTED || isGpsActive) {
                                                Color(0xFF00E676).copy(alpha = pulseAlpha)
                                            } else Color(0xFFFF9100)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (telemetrySource == TelemetrySource.HUD_OBD || (telemetrySource == TelemetrySource.AUTO && obdTelemetry.connectionState == ObdConnectionState.CONNECTED)) {
                                        "TRẠNG THÁI OBD: ${obdTelemetry.connectionState.title}"
                                    } else {
                                        "TRẠNG THÁI GPS: ${if (isGpsActive) "ĐÃ CÓ VỊ TRÍ MỚI" else "CHƯA CÓ VỊ TRÍ MỚI"}"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (isGpsActive && gpsAccuracyMeters.isFinite()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• GPS Hardware: FusedLocationProviderClient (Độ chính xác: ±${gpsAccuracyMeters.toInt()}m)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (obdTelemetry.connectionState == ObdConnectionState.CONNECTED) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Thiết bị OBD: ${obdTelemetry.connectedDeviceName} (${obdTelemetry.connectedDeviceAddress})",
                                color = colorTheme.accentColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (obdTelemetry.errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Lỗi: ${obdTelemetry.errorMessage}",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bluetooth OBD Device Connection Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THIẾT BỊ BLUETOOTH OBD-II ĐÃ GHÉP ĐÔI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(onClick = onRefreshDevices) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Làm mới", tint = colorTheme.primaryColor)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (pairedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa tìm thấy thiết bị Bluetooth đã ghép đôi.\nVào Cài đặt Bluetooth của điện thoại để ghép đôi với cổng OBD-II (mã PIN thường là 1234 hoặc 0000).",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    pairedDevices.forEach { device ->
                        val isConnected = obdTelemetry.connectedDeviceAddress == device.address &&
                                obdTelemetry.connectionState == ObdConnectionState.CONNECTED
                        val isConnecting = obdTelemetry.connectedDeviceAddress == device.address &&
                                (obdTelemetry.connectionState == ObdConnectionState.CONNECTING ||
                                        obdTelemetry.connectionState == ObdConnectionState.INITIALIZING)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isConnected) colorTheme.primaryColor.copy(alpha = 0.15f)
                                    else Color(0xFF161F33)
                                )
                                .border(
                                    1.dp,
                                    if (isConnected) colorTheme.primaryColor else Color(0xFF334155),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = if (isConnected) Color(0xFF00E676) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = device.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (device.isObdLikely) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(text = "OBD", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(
                                            text = device.address,
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (isConnected) {
                                    ElevatedButton(
                                        onClick = onDisconnectObd,
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color(0xFF7F1D1D),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(text = "Ngắt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isConnecting) {
                                    ElevatedButton(
                                        onClick = onDisconnectObd,
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = Color(0xFF854D0E),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(text = "Đang nối...", fontSize = 11.sp)
                                    }
                                } else {
                                    ElevatedButton(
                                        onClick = { onConnectObd(device.address, device.name) },
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = colorTheme.primaryColor.copy(alpha = 0.3f),
                                            contentColor = colorTheme.primaryColor
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(text = "Kết Nối", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tip Box for Motorcycle Riders
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 MẸO RIDER: Dùng cọng cáp chuyển đổi OBD 4-pin/6-pin (Honda, Yamaha) gắn với tẩu ELM327 Bluetooth. HUD sẽ đọc trực tiếp vòng tua RPM động cơ, nhiệt độ máy và tốc độ bánh xe chuẩn xác từ ECU!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ObdMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0C1322))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}
