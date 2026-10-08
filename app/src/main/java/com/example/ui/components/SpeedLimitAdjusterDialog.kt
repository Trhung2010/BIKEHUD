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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.HudColorTheme
import com.example.model.SpeedUnit
import com.example.util.HapticHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedLimitAdjusterDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    currentSpeedLimit: Int,
    speedUnit: SpeedUnit,
    colorTheme: HudColorTheme,
    bufferKmh: Int,
    isWarningEnabled: Boolean,
    onSetSpeedLimit: (Int) -> Unit,
    onAdjustDelta: (Int) -> Unit,
    onSetBuffer: (Int) -> Unit,
    onToggleWarning: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var sliderValue by remember(currentSpeedLimit) { mutableFloatStateOf(currentSpeedLimit.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFFF1744).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("speed_limit_adjuster_dialog"),
            color = Color(0xFF0B0F1A)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF1744).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cài Đặt Giới Hạn Tốc Độ",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big European/Vietnamese-style Speed Limit Sign
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(7.dp, Color(0xFFFF1744), CircleShape)
                        .testTag("big_speed_limit_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currentSpeedLimit",
                        color = Color.Black,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${speedUnit.label.uppercase()} • CẢNH BÁO KHI VƯỢT QUÁ",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Slider
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        onSetSpeedLimit(it.toInt())
                    },
                    valueRange = 20f..140f,
                    steps = 23, // 5 km/h increments
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF1744),
                        activeTrackColor = Color(0xFFFF1744),
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speed_limit_slider")
                )

                // Stepper Buttons (-10, -5, -1, +1, +5, +10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(-10, -5, -1, 1, 5, 10).forEach { delta ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (delta < 0) Color(0xFF1E293B) else Color(0xFF1E293B)
                                )
                                .border(1.dp, if (delta > 0) colorTheme.primaryColor.copy(alpha = 0.5f) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable {
                                    onAdjustDelta(delta)
                                    sliderValue = (currentSpeedLimit + delta).toFloat().coerceIn(20f, 140f)
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (delta > 0) "+$delta" else "$delta",
                                color = if (delta > 0) colorTheme.primaryColor else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Common Presets
                Text(
                    text = "MỐC TỐC ĐỘ PHỔ BIẾN",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 40, 50, 60, 70, 80, 90, 100).forEach { preset ->
                        val isSelected = preset == currentSpeedLimit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFFFF1744).copy(alpha = 0.25f)
                                    else Color(0xFF131A29)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF1744) else Color(0xFF1E293B),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSetSpeedLimit(preset)
                                    sliderValue = preset.toFloat()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$preset",
                                color = if (isSelected) Color(0xFFFF5252) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buffer tolerance row (0, +2, +5 km/h)
                Text(
                    text = "DUNG SAI KÍCH HOẠT CẢNH BÁO",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair(0, "Ngay lập tức (+0)"),
                        Pair(2, "Dung sai nhẹ (+2)"),
                        Pair(5, "Dung sai (+5)")
                    ).forEach { (buf, label) ->
                        val isSelected = buf == bufferKmh
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1A73E8).copy(alpha = 0.25f) else Color(0xFF131A29))
                                .border(1.dp, if (isSelected) Color(0xFF1A73E8) else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .clickable { onSetBuffer(buf) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF64B5F6) else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Warning settings row: Haptic Vibration switch & Test Warning
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131A29))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = colorTheme.accentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rung phản hồi khi quá tốc",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isWarningEnabled,
                        onCheckedChange = { onToggleWarning() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF1744),
                            checkedTrackColor = Color(0xFFFF1744).copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test warning button
                ElevatedButton(
                    onClick = {
                        HapticHelper.vibrateSpeedWarning(context)
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thử Rung Cảnh Báo Haptic",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
