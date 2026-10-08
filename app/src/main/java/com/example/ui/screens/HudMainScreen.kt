package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BluetoothDeviceInfo
import com.example.model.HudColorTheme
import com.example.model.NavDisplayMode
import com.example.model.TelemetrySource
import com.example.ui.components.GoogleMapHudView
import com.example.ui.components.HudQuickSettingsDialog
import com.example.ui.components.HudTopBar
import com.example.ui.components.LandscapeCockpitHud
import com.example.ui.components.LeanAngleGauge
import com.example.ui.components.MessageHudBanner
import com.example.ui.components.MusicHudControl
import com.example.ui.components.NavigationHudView
import com.example.ui.components.ObdConnectionDialog
import com.example.ui.components.SearchDestinationDialog
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.components.SpeedLimitAdjusterDialog
import com.example.ui.components.SpeedometerGauge
import com.example.viewmodel.HudViewModel

@Composable
fun HudMainScreen(
    viewModel: HudViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val musicState by viewModel.musicState.collectAsStateWithLifecycle()
    val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()
    val isMirrorMode by viewModel.isMirrorMode.collectAsStateWithLifecycle()
    val isNightMode by viewModel.isNightMode.collectAsStateWithLifecycle()
    val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val isSimMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val routes by viewModel.routes.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val remainingMeters by viewModel.stepRemainingDistance.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()
    val autoReplyEnabled by viewModel.autoReplyEnabled.collectAsStateWithLifecycle()
    val navDisplayMode by viewModel.navDisplayMode.collectAsStateWithLifecycle()
    val speedThresholdBuffer by viewModel.speedThresholdBuffer.collectAsStateWithLifecycle()
    val speedWarningEnabled by viewModel.speedWarningEnabled.collectAsStateWithLifecycle()
    val leanAngle by viewModel.leanAngle.collectAsStateWithLifecycle()
    val maxLeftLean by viewModel.maxLeftLean.collectAsStateWithLifecycle()
    val maxRightLean by viewModel.maxRightLean.collectAsStateWithLifecycle()
    val emergencyContact by viewModel.emergencyContact.collectAsStateWithLifecycle()
    val telemetrySource by viewModel.telemetrySource.collectAsStateWithLifecycle()
    val obdTelemetry by viewModel.obdTelemetry.collectAsStateWithLifecycle()
    val isGpsActive by viewModel.isGpsActive.collectAsStateWithLifecycle()
    val gpsAccuracy by viewModel.gpsAccuracy.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showSpeedLimitDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showObdDialog by remember { mutableStateOf(false) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDeviceInfo>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(showObdDialog) {
        if (showObdDialog) {
            pairedDevices = viewModel.getPairedObdDevices()
        }
    }

    // Screen-wide visual overspeed pulse animation (illuminates perimeter edges for peripheral vision)
    val infiniteTransition = rememberInfiniteTransition(label = "screen_overspeed_pulse")
    val overspeedBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "overspeedBorderAlpha"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentStep = currentRoute.steps.getOrNull(currentStepIndex)
        ?: currentRoute.steps.first()

    val displayCurrentSpeed = (telemetry.speedKmh * speedUnit.factor).toInt()
    val displayLimitSpeed = (telemetry.currentSpeedLimit * speedUnit.factor).toInt()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("hud_main_scaffold"),
        containerColor = Color.Black // True OLED black to eliminate windshield glare
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .then(
                    // VISUAL SCREEN WARNING ON PERIMETER: Pulsing red border around display when exceeding speed limit
                    if (telemetry.isOverSpeed) {
                        Modifier.border(
                            width = 5.dp,
                            color = Color(0xFFFF1744).copy(alpha = overspeedBorderAlpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else Modifier
                )
        ) {
            // Main HUD Content wrapper with horizontal mirror transformation for windshield projection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isMirrorMode) {
                            scaleX = -1f // Flips horizontally for windshield reflection
                        }
                    }
                    .testTag("hud_mirrorable_content")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar with HUD Controls (Show on portrait; in landscape, controls are built into the Cockpit Wing)
                    if (!isLandscape) {
                        HudTopBar(
                            isMirrorMode = isMirrorMode,
                            isNightMode = isNightMode,
                            colorTheme = colorTheme,
                            speedUnit = speedUnit,
                            isSimulationMode = isSimMode,
                            currentSpeedLimit = telemetry.currentSpeedLimit,
                            telemetrySource = telemetrySource,
                            onToggleMirror = { viewModel.toggleMirrorMode() },
                            onToggleNight = { viewModel.toggleNightMode() },
                            onToggleUnit = { viewModel.toggleSpeedUnit() },
                            onToggleSim = { viewModel.toggleSimulationMode() },
                            onCycleTheme = {
                                val allThemes = HudColorTheme.entries
                                val nextIndex = (allThemes.indexOf(colorTheme) + 1) % allThemes.size
                                viewModel.selectTheme(allThemes[nextIndex])
                            },
                            onOpenSpeedLimitDialog = { showSpeedLimitDialog = true },
                            onOpenSosDialog = { showSosDialog = true },
                            onOpenObdDialog = { showObdDialog = true },
                            onOpenSettings = { showSettingsDialog = true }
                        )
                    }

                    // FULL SCREEN VISUAL OVERSPEED WARNING BANNER
                    AnimatedVisibility(
                        visible = telemetry.isOverSpeed,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF1744).copy(alpha = 0.35f * overspeedBorderAlpha))
                                .border(2.dp, Color(0xFFFF1744).copy(alpha = overspeedBorderAlpha), RoundedCornerShape(8.dp))
                                .clickable { showSpeedLimitDialog = true }
                                .padding(vertical = 4.dp, horizontal = 10.dp)
                                .testTag("screen_overspeed_visual_banner"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Cảnh báo quá tốc",
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CẢNH BÁO: TỐC ĐỘ $displayCurrentSpeed > GIỚI HẠN $displayLimitSpeed ${speedUnit.label.uppercase()}! GIẢM TỐC ĐỘ!",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Message Notification Banner at top of HUD if active
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                        MessageHudBanner(
                            notification = activeNotification,
                            colorTheme = colorTheme,
                            onDismiss = { viewModel.dismissActiveNotification() },
                            onQuickReply = { reply -> viewModel.sendQuickReply(reply) }
                        )
                    }

                    // Main HUD Layout: Adaptive between Landscape & Portrait
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (isLandscape || maxWidth > 650.dp) {
                            // UPGRADED WIDESCREEN SUPERBIKE COCKPIT (Dual-Wing Cockpit with Gyro Horizon, Radar & Media)
                            LandscapeCockpitHud(
                                telemetry = telemetry,
                                speedUnit = speedUnit,
                                colorTheme = colorTheme,
                                isNightMode = isNightMode,
                                isMirrorMode = isMirrorMode,
                                leanAngle = leanAngle,
                                maxLeftLean = maxLeftLean,
                                maxRightLean = maxRightLean,
                                currentRoute = currentRoute,
                                currentStep = currentStep,
                                stepIndex = currentStepIndex,
                                totalSteps = currentRoute.steps.size,
                                remainingMeters = remainingMeters,
                                navDisplayMode = navDisplayMode,
                                musicState = musicState,
                                telemetrySource = telemetrySource,
                                onToggleMirror = { viewModel.toggleMirrorMode() },
                                onToggleNight = { viewModel.toggleNightMode() },
                                onToggleUnit = { viewModel.toggleSpeedUnit() },
                                onCycleTheme = {
                                    val allThemes = HudColorTheme.entries
                                    val nextIndex = (allThemes.indexOf(colorTheme) + 1) % allThemes.size
                                    viewModel.selectTheme(allThemes[nextIndex])
                                },
                                onToggleNavMode = { viewModel.toggleNavDisplayMode() },
                                onOpenSearch = { showSearchDialog = true },
                                onOpenSpeedLimitDialog = { showSpeedLimitDialog = true },
                                onOpenSosDialog = { showSosDialog = true },
                                onOpenObdDialog = { showObdDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNextTrack = { viewModel.nextTrack() },
                                onPrevTrack = { viewModel.previousTrack() },
                                onAdjustVolume = { inc -> viewModel.adjustVolume(inc) },
                                onCycleHelmetDevice = { viewModel.cycleHelmetDevice() },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // PORTRAIT MOTORCYCLE MOUNT (Tank bag or vertical handlebar phone mount)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SpeedometerGauge(
                                    telemetry = telemetry,
                                    unit = speedUnit,
                                    colorTheme = colorTheme,
                                    isNightMode = isNightMode,
                                    onOpenSpeedLimitDialog = { showSpeedLimitDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Lean Angle Gauge in Portrait
                                LeanAngleGauge(
                                    leanAngle = leanAngle,
                                    maxLeft = maxLeftLean,
                                    maxRight = maxRightLean,
                                    colorTheme = colorTheme,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Mode switcher buttons in Portrait
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (navDisplayMode == NavDisplayMode.VECTOR_HUD) colorTheme.primaryColor.copy(alpha = 0.2f)
                                                else Color(0xFF1E293B)
                                            )
                                            .border(
                                                1.dp,
                                                if (navDisplayMode == NavDisplayMode.VECTOR_HUD) colorTheme.primaryColor else Color(0xFF334155),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.setNavDisplayMode(NavDisplayMode.VECTOR_HUD) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "HUD CHỈ ĐƯỜNG",
                                            color = if (navDisplayMode == NavDisplayMode.VECTOR_HUD) colorTheme.primaryColor else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (navDisplayMode == NavDisplayMode.GOOGLE_MAPS) Color(0xFF1A73E8).copy(alpha = 0.25f)
                                                else Color(0xFF1E293B)
                                            )
                                            .border(
                                                1.dp,
                                                if (navDisplayMode == NavDisplayMode.GOOGLE_MAPS) Color(0xFF1A73E8) else Color(0xFF334155),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.setNavDisplayMode(NavDisplayMode.GOOGLE_MAPS) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "GOOGLE MAPS HUD",
                                            color = if (navDisplayMode == NavDisplayMode.GOOGLE_MAPS) Color(0xFF64B5F6) else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    // Quick SOS emergency button for motorcyclist with gloves
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFF1744))
                                            .clickable { showSosDialog = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .testTag("portrait_sos_quick_button"),
                                        contentAlignment = Alignment.Center
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

                                if (navDisplayMode == NavDisplayMode.VECTOR_HUD) {
                                    NavigationHudView(
                                        route = currentRoute,
                                        currentStep = currentStep,
                                        stepIndex = currentStepIndex,
                                        totalSteps = currentRoute.steps.size,
                                        remainingMeters = remainingMeters,
                                        compassHeading = telemetry.compassHeadingDegrees,
                                        colorTheme = colorTheme,
                                        onToggleToGoogleMaps = { viewModel.toggleNavDisplayMode() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    GoogleMapHudView(
                                        route = currentRoute,
                                        compassHeading = telemetry.compassHeadingDegrees,
                                        speedKmh = telemetry.speedKmh,
                                        colorTheme = colorTheme,
                                        onOpenSearch = { showSearchDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(280.dp)
                                    )
                                }

                                MusicHudControl(
                                    musicState = musicState,
                                    colorTheme = colorTheme,
                                    onTogglePlayPause = { viewModel.togglePlayPause() },
                                    onNext = { viewModel.nextTrack() },
                                    onPrevious = { viewModel.previousTrack() },
                                    onAdjustVolume = { inc -> viewModel.adjustVolume(inc) },
                                    onCycleDevice = { viewModel.cycleHelmetDevice() },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            // Discreet Mirror Mode Overlay Notice (Not mirrored so user can read if looking directly)
            AnimatedVisibility(
                visible = isMirrorMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF000000).copy(alpha = 0.85f))
                        .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.toggleMirrorMode() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("mirror_mode_active_banner")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = null,
                        tint = colorTheme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ĐANG LẬT GƯƠNG CHIẾU KÍNH • Chạm để tắt",
                        color = colorTheme.primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Settings Dialog
        HudQuickSettingsDialog(
            isOpen = showSettingsDialog,
            onDismiss = { showSettingsDialog = false },
            routes = routes,
            currentRoute = currentRoute,
            onSelectRoute = {
                viewModel.selectRoute(it)
                showSettingsDialog = false
            },
            currentSpeedLimit = telemetry.currentSpeedLimit,
            onSelectSpeedLimit = { viewModel.setSpeedLimit(it) },
            currentTheme = colorTheme,
            onSelectTheme = { viewModel.selectTheme(it) },
            autoReplyEnabled = autoReplyEnabled,
            onToggleAutoReply = { viewModel.toggleAutoReply() },
            onTriggerTestMessage = { sender, app, msg ->
                viewModel.triggerSampleMessage(sender, app, msg)
                showSettingsDialog = false
            },
            onResetTrip = { viewModel.resetTrip() },
            onCycleBluetoothDevice = { viewModel.cycleHelmetDevice() },
            onOpenSosDialog = { showSosDialog = true },
            onOpenObdDialog = { showObdDialog = true }
        )

        // Custom Destination Search & Google Maps Dialog
        SearchDestinationDialog(
            isOpen = showSearchDialog,
            onDismiss = { showSearchDialog = false },
            colorTheme = colorTheme,
            onSelectCustomDestination = { dest ->
                viewModel.setCustomDestination(dest)
                showSearchDialog = false
            }
        )

        // Custom Speed Limit Threshold Adjuster Dialog
        SpeedLimitAdjusterDialog(
            isOpen = showSpeedLimitDialog,
            onDismiss = { showSpeedLimitDialog = false },
            currentSpeedLimit = telemetry.currentSpeedLimit,
            speedUnit = speedUnit,
            colorTheme = colorTheme,
            bufferKmh = speedThresholdBuffer,
            isWarningEnabled = speedWarningEnabled,
            onSetSpeedLimit = { viewModel.setSpeedLimit(it) },
            onAdjustDelta = { viewModel.adjustSpeedLimit(it) },
            onSetBuffer = { viewModel.setSpeedThresholdBuffer(it) },
            onToggleWarning = { viewModel.toggleSpeedWarning() }
        )

        // Emergency SOS Broadcast Dialog
        SosEmergencyDialog(
            isOpen = showSosDialog,
            onDismiss = { showSosDialog = false },
            emergencyContact = emergencyContact,
            currentLatitude = viewModel.getCurrentCoordinates().first,
            currentLongitude = viewModel.getCurrentCoordinates().second,
            speedKmh = telemetry.speedKmh,
            colorTheme = colorTheme,
            onSendSos = { ctx ->
                viewModel.triggerEmergencySos(ctx) { _, _ -> }
            },
            onUpdateContact = { name, phone, msg ->
                viewModel.updateEmergencyContact(name, phone, msg)
            }
        )

        // Real OBD-II & GPS Telemetry Source Configuration Dialog
        ObdConnectionDialog(
            isOpen = showObdDialog,
            onDismiss = { showObdDialog = false },
            telemetrySource = telemetrySource,
            onSelectSource = { viewModel.setTelemetrySource(it) },
            obdTelemetry = obdTelemetry,
            isGpsActive = isGpsActive,
            gpsAccuracyMeters = gpsAccuracy,
            pairedDevices = pairedDevices,
            onRefreshDevices = { pairedDevices = viewModel.getPairedObdDevices() },
            onConnectObd = { addr, name -> viewModel.connectObd(addr, name) },
            onDisconnectObd = { viewModel.disconnectObd() },
            colorTheme = colorTheme
        )
    }
}
