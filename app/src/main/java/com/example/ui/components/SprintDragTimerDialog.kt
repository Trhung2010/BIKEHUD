package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.SprintResult
import com.example.model.SprintStatus
import com.example.model.SprintTarget
import com.example.model.SprintTimerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SprintDragTimerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    sprintState: SprintTimerState,
    currentSpeedKmh: Float,
    colorTheme: HudColorTheme,
    voiceAlertEnabled: Boolean,
    audioBeepEnabled: Boolean,
    onArmTimer: (SprintTarget) -> Unit,
    onCancelTimer: () -> Unit,
    onResetRecords: () -> Unit,
    onSimulateRun: () -> Unit,
    onToggleVoiceAlert: () -> Unit,
    onToggleAudioBeep: () -> Unit
) {
    if (!isOpen) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val armedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "armed_pulse"
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
                .testTag("sprint_drag_dialog"),
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = colorTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ĐO GIA TỐC SPRINT DRAG",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Bấm giờ 0-50, 0-60 & 0-100 km/h tự động",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Target Selector (0-50, 0-60, 0-100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SprintTarget.entries.forEach { target ->
                        val isSelected = sprintState.target == target
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) colorTheme.primaryColor.copy(alpha = 0.25f)
                                    else Color(0xFF111827)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colorTheme.primaryColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (sprintState.status != SprintStatus.LAUNCHED) {
                                        onArmTimer(target)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = target.label,
                                color = if (isSelected) colorTheme.primaryColor else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DRAG STRIP CHRISTMAS TREE & CHRONOGRAPH BOX
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B1324))
                        .border(1.5.dp, colorTheme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Christmas Tree Lights
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Red light (Staged / Armed)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (sprintState.status == SprintStatus.ARMED) Color(0xFFFF1744).copy(alpha = armedPulseAlpha)
                                        else Color(0xFF374151)
                                    )
                                    .border(1.dp, Color(0xFFFF1744), CircleShape)
                            )
                            // Yellow light (Staged ready)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (sprintState.status == SprintStatus.ARMED) Color(0xFFFFD600).copy(alpha = armedPulseAlpha)
                                        else Color(0xFF374151)
                                    )
                                    .border(1.dp, Color(0xFFFFD600), CircleShape)
                            )
                            // Green light (LAUNCHED - GO!)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (sprintState.status == SprintStatus.LAUNCHED) Color(0xFF00E676)
                                        else if (sprintState.status == SprintStatus.COMPLETED) Color(0xFF00E676).copy(alpha = 0.5f)
                                        else Color(0xFF374151)
                                    )
                                    .border(1.dp, Color(0xFF00E676), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Banner
                        Text(
                            text = when (sprintState.status) {
                                SprintStatus.IDLE -> "CHỜ KÍCH HOẠT"
                                SprintStatus.ARMED -> "ĐÃ SẴN SÀNG • VẶN GA ĐỂ XUẤT PHÁT!"
                                SprintStatus.LAUNCHED -> "ĐANG CHẠY • FULL THROTTLE!"
                                SprintStatus.COMPLETED -> "HOÀN THÀNH XUẤT SẮC!"
                            },
                            color = when (sprintState.status) {
                                SprintStatus.IDLE -> Color(0xFF94A3B8)
                                SprintStatus.ARMED -> Color(0xFFFFD600)
                                SprintStatus.LAUNCHED -> Color(0xFF00E676)
                                SprintStatus.COMPLETED -> colorTheme.primaryColor
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large Digital Chronograph Seconds
                        Text(
                            text = String.format(Locale.US, "%.2f s", sprintState.currentElapsedSeconds),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        // Live Speed Indicator
                        Text(
                            text = "Tốc độ hiện tại: ${currentSpeedKmh.toInt()} km/h (Đỉnh: ${sprintState.topSpeedReached.toInt()} km/h)",
                            color = colorTheme.accentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Split Times Grid (0-50, 0-60, 0-100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SplitTimeCard(
                        title = "0 - 50 km/h",
                        timeSeconds = sprintState.time0to50,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                    SplitTimeCard(
                        title = "0 - 60 km/h",
                        timeSeconds = sprintState.time0to60,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                    SplitTimeCard(
                        title = "0 - 100 km/h",
                        timeSeconds = sprintState.time0to100,
                        colorTheme = colorTheme,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sprintState.status == SprintStatus.IDLE || sprintState.status == SprintStatus.COMPLETED) {
                        ElevatedButton(
                            onClick = { onArmTimer(sprintState.target) },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = colorTheme.primaryColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sprint_arm_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SẴN SÀNG", fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        ElevatedButton(
                            onClick = onCancelTimer,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sprint_cancel_button")
                        ) {
                            Text("HỦY LƯỢT ĐO", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Test Lab Simulation Button
                    ElevatedButton(
                        onClick = onSimulateRun,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sprint_simulate_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST CHẠY THỬ", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))

                // Helmet Audio & Voice Co-Pilot Toggles
                Text(
                    text = "ÂM THANH & TRỢ LÝ GIỌNG NÓI TAI NGHE (BLUETOOTH)",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HeadsetMic, contentDescription = null, tint = colorTheme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Giọng nói Co-Pilot", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Đọc to cảnh báo tốc độ & xuất phát", color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = voiceAlertEnabled,
                        onCheckedChange = { onToggleVoiceAlert() },
                        colors = SwitchDefaults.colors(checkedThumbColor = colorTheme.primaryColor, checkedTrackColor = colorTheme.primaryColor.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = colorTheme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Âm thanh bíp cảnh báo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Phát tiếng bíp khi vượt tốc độ & về đích", color = Color(0xFF64748B), fontSize = 10.sp)
                        }
                    }
                    Switch(
                        checked = audioBeepEnabled,
                        onCheckedChange = { onToggleAudioBeep() },
                        colors = SwitchDefaults.colors(checkedThumbColor = colorTheme.primaryColor, checkedTrackColor = colorTheme.primaryColor.copy(alpha = 0.4f))
                    )
                }

                // Recent History List
                if (sprintState.recentResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LỊCH SỬ CÁC LẦN ĐO GIA TỐC",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Xóa lịch sử",
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onResetRecords() }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    sprintState.recentResults.take(5).forEachIndexed { idx, res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${idx + 1}", color = Color(0xFF64748B), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(res.target.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.2f s", res.elapsedSeconds),
                                    color = colorTheme.primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${res.topSpeedKmh.toInt()} km/h",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitTimeCard(
    title: String,
    timeSeconds: Float?,
    colorTheme: HudColorTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (timeSeconds != null) String.format(Locale.US, "%.2f s", timeSeconds) else "--.--",
                color = if (timeSeconds != null) colorTheme.primaryColor else Color(0xFF475569),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
