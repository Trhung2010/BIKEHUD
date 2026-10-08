package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.RoutePreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudQuickSettingsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    routes: List<RoutePreset>,
    currentRoute: RoutePreset,
    onSelectRoute: (RoutePreset) -> Unit,
    currentSpeedLimit: Int,
    onSelectSpeedLimit: (Int) -> Unit,
    currentTheme: HudColorTheme,
    onSelectTheme: (HudColorTheme) -> Unit,
    autoReplyEnabled: Boolean,
    onToggleAutoReply: () -> Unit,
    onTriggerTestMessage: (sender: String, app: String, msg: String) -> Unit,
    onResetTrip: () -> Unit,
    onCycleBluetoothDevice: () -> Unit,
    onOpenSosDialog: (() -> Unit)? = null,
    onOpenObdDialog: (() -> Unit)? = null
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, currentTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("hud_settings_dialog"),
            color = Color(0xFF0C101C)
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
                                .background(currentTheme.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cài Đặt HUD Xe Máy",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: HUD Windshield Mirroring Tip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131D31))
                        .border(1.dp, currentTheme.secondaryColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = currentTheme.secondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cách dùng HUD phản chiếu kính chắn gió",
                                color = currentTheme.secondaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Đặt điện thoại nằm ngang dưới chân kính chắn gió / mũ bảo hiểm. Bấm nút 'LẬT KÍNH HUD' để chữ và số hiển thị xuôi khi nhìn vào ảnh phản chiếu trên kính ban đêm.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: HUD Color Themes
                Text(
                    text = "BẢNG MÀU HIỂN THỊ (THEME)",
                    color = currentTheme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HudColorTheme.entries.forEach { theme ->
                        val isSelected = theme == currentTheme
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) theme.primaryColor.copy(alpha = 0.25f)
                                    else Color(0xFF1E293B)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) theme.primaryColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTheme(theme) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = theme.title,
                                    color = if (isSelected) theme.primaryColor else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Select Motorcycle Route
                Text(
                    text = "LỘ TRÌNH ĐIỀU HƯỚNG BẢN ĐỒ",
                    color = currentTheme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routes.forEach { r ->
                        val isSelected = r.id == currentRoute.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) currentTheme.primaryColor.copy(alpha = 0.2f)
                                    else Color(0xFF161F33)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) currentTheme.primaryColor else Color(0xFF1E293B),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectRoute(r) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.name,
                                        color = if (isSelected) currentTheme.primaryColor else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${r.destination} • ${r.totalDistanceKm} km (~${r.estimatedTimeMinutes} phút)",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Đang chọn",
                                        tint = currentTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Speed Limit Selector
                Text(
                    text = "GIỚI HẠN TỐC ĐỘ CẢNH BÁO",
                    color = currentTheme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(40, 50, 60, 80, 100).forEach { limit ->
                        val isSelected = limit == currentSpeedLimit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFFFF1744).copy(alpha = 0.25f)
                                    else Color(0xFF1E293B)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF1744) else Color(0xFF334155),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectSpeedLimit(limit) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$limit",
                                color = if (isSelected) Color(0xFFFF5252) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: Auto-Reply & Notifications Test
                Text(
                    text = "THÔNG BÁO TIN NHẮN & AN TOÀN",
                    color = currentTheme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF161F33))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tự động trả lời khi lái xe",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "\"Đang lái xe máy, tôi sẽ gọi lại sau!\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = autoReplyEnabled,
                        onCheckedChange = { onToggleAutoReply() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.primaryColor,
                            checkedTrackColor = currentTheme.primaryColor.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Test notifications triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            onTriggerTestMessage("Mẹ", "Zalo", "Con chạy xe cẩn thận nhé, về ăn cơm sớm!")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Tin Zalo", fontSize = 11.sp)
                    }

                    ElevatedButton(
                        onClick = {
                            onTriggerTestMessage("Sếp Nam", "SMS", "Báo cáo duyệt rồi, đi đường an toàn nhé em.")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Tin SMS", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: OBD-II & GPS Telemetry Source Configuration
                if (onOpenObdDialog != null) {
                    Text(
                        text = "CỔNG OBD-II & GPS VỆ TINH",
                        color = colorTheme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F233A))
                            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                onDismiss()
                                onOpenObdDialog()
                            }
                            .padding(12.dp)
                            .testTag("settings_obd_shortcut")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colorTheme.primaryColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = colorTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kết Nối Cổng OBD-II / GPS Thật",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ghép đôi ELM327 Bluetooth để đọc ECU trực tiếp",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "MỞ >",
                                color = colorTheme.primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Section: Emergency SOS Configuration
                if (onOpenSosDialog != null) {
                    Text(
                        text = "SOS KHẨN CẤP & CỨU HỘ",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF1744).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable {
                                onDismiss()
                                onOpenSosDialog()
                            }
                            .padding(12.dp)
                            .testTag("settings_sos_shortcut")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF1744)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "SOS",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cài Đặt & Phát Tín Hiệu SOS",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Gửi SMS tọa độ GPS cứu hộ khi gặp sự cố",
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "MỞ >",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section 6: Bluetooth Helmet & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = onCycleBluetoothDevice,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = currentTheme.primaryColor.copy(alpha = 0.2f),
                            contentColor = currentTheme.primaryColor
                        )
                    ) {
                        Icon(imageVector = Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Đổi Tai Nghe BT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    ElevatedButton(
                        onClick = onResetTrip,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color(0xFFCBD5E1)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Reset Trip", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
