package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BluetoothDeviceInfo
import com.example.model.SpeedUnit
import com.example.model.isFreshReading
import android.os.SystemClock
import kotlinx.coroutines.delay
import com.example.ui.components.ObdConnectionDialog
import com.example.ui.components.RealHudDashboard
import com.example.ui.components.SpeedLimitAdjusterDialog
import com.example.viewmodel.HudViewModel

@Composable
fun HudMainScreen(viewModel: HudViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.realHud.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val unit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val theme by viewModel.colorTheme.collectAsStateWithLifecycle()
    val mirror by viewModel.isMirrorMode.collectAsStateWithLifecycle()
    val buffer by viewModel.speedThresholdBuffer.collectAsStateWithLifecycle()
    val warningEnabled by viewModel.speedWarningEnabled.collectAsStateWithLifecycle()
    val obd by viewModel.obdTelemetry.collectAsStateWithLifecycle()
    val gps by viewModel.gpsReading.collectAsStateWithLifecycle()
    var nowMs by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = SystemClock.elapsedRealtime()
            delay(500)
        }
    }
    val gpsIsFresh = isFreshReading(gps.receivedAtMs, nowMs)
    var showConnection by remember { mutableStateOf(false) }
    var showLimit by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<BluetoothDeviceInfo>>(emptyList()) }

    LaunchedEffect(showConnection) {
        if (showConnection) devices = viewModel.getPairedObdDevices()
    }
    Scaffold(modifier = modifier.fillMaxSize(), containerColor = Color.Black) { padding ->
        RealHudDashboard(
            state = state, unit = unit, limitKmh = telemetry.currentSpeedLimit,
            bufferKmh = buffer, warningEnabled = warningEnabled, mirror = mirror,
            onSelectMode = { viewModel.setTelemetrySource(it) },
            onToggleUnit = { viewModel.toggleSpeedUnit() },
            onToggleMirror = { viewModel.toggleMirrorMode() },
            onOpenLimit = { showLimit = true }, onOpenConnection = { showConnection = true },
            modifier = Modifier.padding(padding)
        )
    }
    ObdConnectionDialog(
        isOpen = showConnection, onDismiss = { showConnection = false },
        telemetrySource = state.mode, onSelectSource = { viewModel.setTelemetrySource(it) },
        obdTelemetry = obd, isGpsActive = gpsIsFresh,
        gpsAccuracyMeters = if (gpsIsFresh) gps.accuracyMeters ?: Float.NaN else Float.NaN,
        pairedDevices = devices, onRefreshDevices = { devices = viewModel.getPairedObdDevices() },
        onConnectObd = { address, name -> viewModel.connectObd(address, name) },
        onDisconnectObd = { viewModel.disconnectObd() }, colorTheme = theme
    )
    // Threshold settings intentionally use km/h internally and label that unit explicitly.
    SpeedLimitAdjusterDialog(
        isOpen = showLimit, onDismiss = { showLimit = false },
        currentSpeedLimit = telemetry.currentSpeedLimit, speedUnit = SpeedUnit.KMH,
        colorTheme = theme, bufferKmh = buffer, isWarningEnabled = warningEnabled,
        onSetSpeedLimit = { viewModel.setSpeedLimit(it) },
        onAdjustDelta = { viewModel.adjustSpeedLimit(it) },
        onSetBuffer = { viewModel.setSpeedThresholdBuffer(it) },
        onToggleWarning = { viewModel.toggleSpeedWarning() }
    )
}
