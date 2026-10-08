package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.BluetoothMusicState
import com.example.model.HudColorTheme
import com.example.model.NavDisplayMode
import com.example.model.NavStep
import com.example.model.RideTelemetry
import com.example.model.RoutePreset
import com.example.model.SpeedUnit
import com.example.model.TelemetrySource
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Usb

@Composable
fun LandscapeCockpitHud(
    telemetry: RideTelemetry,
    speedUnit: SpeedUnit,
    colorTheme: HudColorTheme,
    isNightMode: Boolean,
    isMirrorMode: Boolean,
    leanAngle: Float,
    maxLeftLean: Float,
    maxRightLean: Float,
    currentRoute: RoutePreset,
    currentStep: NavStep,
    stepIndex: Int,
    totalSteps: Int,
    remainingMeters: Int,
    navDisplayMode: NavDisplayMode,
    musicState: BluetoothMusicState,
    telemetrySource: TelemetrySource = TelemetrySource.AUTO,
    onToggleMirror: () -> Unit,
    onToggleNight: () -> Unit,
    onToggleUnit: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleNavMode: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSpeedLimitDialog: () -> Unit,
    onOpenSosDialog: (() -> Unit)? = null,
    onOpenObdDialog: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onAdjustVolume: (Boolean) -> Unit,
    onCycleHelmetDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("landscape_cockpit_hud"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // -------------------------------------------------------------
        // LEFT WING (Panel 1): SPEEDOMETER & LEAN ANGLE TELEMETRY
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speedometer gauge with circular arc and digital numbers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerGauge(
                    telemetry = telemetry,
                    unit = speedUnit,
                    colorTheme = colorTheme,
                    isNightMode = isNightMode,
                    onOpenSpeedLimitDialog = onOpenSpeedLimitDialog,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // MotoGP-style Lean Angle Gyroscope Bar
            LeanAngleGauge(
                leanAngle = leanAngle,
                maxLeft = maxLeftLean,
                maxRight = maxRightLean,
                colorTheme = colorTheme,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // -------------------------------------------------------------
        // CENTER WING (Panel 2): NAVIGATION RADAR OR GOOGLE MAPS HUD
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (navDisplayMode == NavDisplayMode.VECTOR_HUD) {
                    NavigationHudView(
                        route = currentRoute,
                        currentStep = currentStep,
                        stepIndex = stepIndex,
                        totalSteps = totalSteps,
                        remainingMeters = remainingMeters,
                        compassHeading = telemetry.compassHeadingDegrees,
                        colorTheme = colorTheme,
                        onToggleToGoogleMaps = onToggleNavMode,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        GoogleMapHudView(
                            route = currentRoute,
                            compassHeading = telemetry.compassHeadingDegrees,
                            speedKmh = telemetry.speedKmh,
                            colorTheme = colorTheme,
                            onOpenSearch = onOpenSearch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { onToggleNavMode() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .align(Alignment.End)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = colorTheme.primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VỀ HUD VECTOR",
                                    color = colorTheme.primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-nav quick action bar (Search Destination & Mode Switch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Destination Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111827).copy(alpha = 0.9f))
                        .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onOpenSearch() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm điểm đến",
                        tint = colorTheme.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TÌM ĐỊA CHỈ GOOGLE MAPS",
                        color = colorTheme.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Altitude and Trip duration mini badge
                Text(
                    text = "CAO ĐỘ: ${telemetry.altitudeMeters.toInt()}m",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // -------------------------------------------------------------
        // RIGHT WING (Panel 3): MEDIA, HELMET BLUETOOTH & QUICK CONTROLS
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Landscape Quick Control Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0B101D).copy(alpha = 0.85f))
                    .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mirror HUD Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isMirrorMode) colorTheme.primaryColor.copy(alpha = 0.3f) else Color(0xFF1E293B))
                        .border(1.dp, if (isMirrorMode) colorTheme.primaryColor else Color(0xFF334155), RoundedCornerShape(6.dp))
                        .clickable { onToggleMirror() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            tint = if (isMirrorMode) colorTheme.primaryColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isMirrorMode) "LẬT: BẬT" else "LẬT KÍNH",
                            color = if (isMirrorMode) colorTheme.primaryColor else Color(0xFFE2E8F0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Speed Limit Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF1744).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(6.dp))
                        .clickable { onOpenSpeedLimitDialog() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MAX ${telemetry.currentSpeedLimit}",
                        color = Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
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
                        .clickable { onOpenObdDialog?.invoke() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("landscape_telemetry_source_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (telemetrySource == TelemetrySource.HUD_OBD) Icons.Default.Usb else Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (telemetrySource == TelemetrySource.HUD_OBD) Color(0xFF6EE7B7) else Color(0xFF7DD3FC),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = telemetrySource.badgeText,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Night Mode Icon
                IconButton(
                    onClick = onToggleNight,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isNightMode) colorTheme.primaryColor.copy(alpha = 0.2f) else Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (isNightMode) colorTheme.primaryColor else Color(0xFFFFD600),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Theme Icon
                IconButton(
                    onClick = onCycleTheme,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colorTheme.primaryColor.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = colorTheme.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Emergency SOS Button
                if (onOpenSosDialog != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF1744))
                            .clickable { onOpenSosDialog() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("landscape_sos_button")
                    ) {
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Full Music & Bluetooth Helmet Controller
            MusicHudControl(
                musicState = musicState,
                colorTheme = colorTheme,
                onTogglePlayPause = onTogglePlayPause,
                onNext = onNextTrack,
                onPrevious = onPrevTrack,
                onAdjustVolume = onAdjustVolume,
                onCycleDevice = onCycleHelmetDevice,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
