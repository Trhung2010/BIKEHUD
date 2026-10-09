package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudColorTheme
import com.example.model.SpeedUnit
import com.example.model.TelemetrySource
import androidx.compose.material.icons.filled.Usb

@Composable
fun HudTopBar(
    isMirrorMode: Boolean,
    isNightMode: Boolean,
    colorTheme: HudColorTheme,
    speedUnit: SpeedUnit,
    isSimulationMode: Boolean,
    currentSpeedLimit: Int = 50,
    telemetrySource: TelemetrySource = TelemetrySource.AUTO,
    onToggleMirror: () -> Unit,
    onToggleNight: () -> Unit,
    onToggleUnit: () -> Unit,
    onToggleSim: () -> Unit,
    onCycleTheme: () -> Unit,
    onOpenSpeedLimitDialog: (() -> Unit)? = null,
    onOpenSosDialog: (() -> Unit)? = null,
    onOpenObdDialog: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Mirror Mode Button (CRUCIAL for Windshield reflection!)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isMirrorMode) colorTheme.primaryColor.copy(alpha = 0.25f)
                    else Color(0xFF111827).copy(alpha = 0.8f)
                )
                .border(
                    width = if (isMirrorMode) 2.dp else 1.dp,
                    color = if (isMirrorMode) colorTheme.primaryColor else Color(0xFF374151),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onToggleMirror() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("hud_mirror_mode_button")
        ) {
            Icon(
                imageVector = Icons.Default.Flip,
                contentDescription = "Chế độ phản chiếu kính",
                tint = if (isMirrorMode) colorTheme.primaryColor else Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMirrorMode) "LẬT GƯƠNG (BẬT)" else "LẬT KÍNH HUD",
                color = if (isMirrorMode) colorTheme.primaryColor else Color(0xFFE5E7EB),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Center / Right Quick Action Chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Speed Unit Chip (KM/H vs MPH)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                    .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .clickable { onToggleUnit() }
                    .semantics {
                        contentDescription = "Đơn vị tốc độ: ${speedUnit.label}"
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("unit_toggle_button")
            ) {
                Text(
                    text = speedUnit.label.uppercase(),
                    color = colorTheme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Speed Limit Pill (Tap to customize speed limit threshold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF1744).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(6.dp))
                    .then(
                        if (onOpenSpeedLimitDialog != null) Modifier.clickable { onOpenSpeedLimitDialog() }
                        else Modifier
                    )
                    .semantics {
                        contentDescription = "Giới hạn tốc độ cảnh báo: tối đa $currentSpeedLimit ${speedUnit.label}"
                    }
                    .padding(horizontal = 7.dp, vertical = 4.dp)
                    .testTag("topbar_speed_limit_button")
            ) {
                Text(
                    text = "MAX $currentSpeedLimit",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Night Mode Toggle (OLED Minimal Dark vs Day Boost)
            IconButton(
                onClick = onToggleNight,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isNightMode) colorTheme.primaryColor.copy(alpha = 0.2f)
                        else Color(0xFF1E293B)
                    )
                    .border(
                        1.dp,
                        if (isNightMode) colorTheme.primaryColor else Color(0xFF4B5563),
                        CircleShape
                    )
                    .testTag("night_mode_toggle_button")
            ) {
                Icon(
                    imageVector = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = if (isNightMode) "Chế độ ban đêm" else "Chế độ ban ngày",
                    tint = if (isNightMode) colorTheme.primaryColor else Color(0xFFFFD600),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Quick Theme Palette Switcher
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colorTheme.primaryColor.copy(alpha = 0.2f))
                    .border(1.dp, colorTheme.primaryColor, CircleShape)
                    .testTag("cycle_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Đổi màu HUD",
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Telemetry Source Chip (OBD-II LIVE vs GPS LIVE)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (telemetrySource == TelemetrySource.HUD_OBD) Color(0xFF064E3B)
                        else if (telemetrySource == TelemetrySource.AUTO) Color(0xFF1E1B4B)
                        else Color(0xFF0C243C)
                    )
                    .border(
                        1.dp,
                        if (telemetrySource == TelemetrySource.HUD_OBD) Color(0xFF10B981)
                        else if (telemetrySource == TelemetrySource.AUTO) Color(0xFF818CF8)
                        else Color(0xFF38BDF8),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        if (onOpenObdDialog != null) onOpenObdDialog() else onToggleSim()
                    }
                    .semantics {
                        contentDescription = "Nguồn định vị và tốc độ: ${if (isSimulationMode) "Chế độ mô phỏng" else telemetrySource.badgeText}"
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("telemetry_source_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (telemetrySource == TelemetrySource.HUD_OBD) Icons.Default.Usb else Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = if (telemetrySource == TelemetrySource.HUD_OBD) Color(0xFF6EE7B7) else Color(0xFF7DD3FC),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSimulationMode) "TEST LAB" else telemetrySource.badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2937))
                    .testTag("open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt HUD",
                    tint = Color(0xFFE5E7EB),
                    modifier = Modifier.size(18.dp)
                )
            }

            // SOS Emergency Button
            if (onOpenSosDialog != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF1744))
                        .clickable { onOpenSosDialog() }
                        .semantics {
                            contentDescription = "Cứu hộ khẩn cấp SOS"
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("topbar_sos_button")
                ) {
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
