package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import java.util.Locale
import kotlin.math.roundToInt

private val Panel = Color(0xFF101820)
private val Muted = Color(0xFFB1C2CF)
private val ObdAccent = Color(0xFF6EF3B0)
private val GpsAccent = Color(0xFF75DFFF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealHudDashboard(
    state: RealHudState,
    unit: SpeedUnit,
    limitKmh: Int,
    bufferKmh: Int,
    warningEnabled: Boolean,
    mirror: Boolean,
    onSelectMode: (TelemetrySource) -> Unit,
    onToggleUnit: () -> Unit,
    onToggleMirror: () -> Unit,
    onOpenLimit: () -> Unit,
    onOpenConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isObd = state.mode == TelemetrySource.HUD_OBD
    val accent = if (isObd) ObdAccent else GpsAccent
    val overSpeed = state.speedKmh?.let { it > limitKmh + bufferKmh } ?: false
    Column(
        modifier = modifier.fillMaxSize().background(Color.Black)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("BIKE / HUD", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("DỮ LIỆU THỰC", color = accent, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(TelemetrySource.HUD_OBD to "OBD HUD", TelemetrySource.HUD_GPS to "GPS HUD").forEach { (mode, label) ->
                val selected = state.mode == mode
                Box(
                    Modifier.weight(1f).heightIn(min = 56.dp)
                        .background(if (selected) accent.copy(alpha = 0.15f) else Panel, RoundedCornerShape(14.dp))
                        .border(1.dp, if (selected) accent else Color(0xFF30404E), RoundedCornerShape(14.dp))
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelectMode(mode) })
                        .testTag(if (mode == TelemetrySource.HUD_OBD) "obd_hud_tab" else "gps_hud_tab")
                        .padding(12.dp), contentAlignment = Alignment.Center
                ) { Text(label, color = if (selected) accent else Color.White, fontWeight = FontWeight.Bold) }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(onClick = onOpenConnection, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(if (isObd) "Kết nối OBD" else "Nguồn dữ liệu")
            }
            OutlinedButton(onClick = onToggleUnit, modifier = Modifier.heightIn(min = 48.dp)
                .semantics { contentDescription = "Đổi đơn vị tốc độ, hiện tại ${unit.label}" }) {
                Text(unit.label)
            }
            OutlinedButton(onClick = onToggleMirror, modifier = Modifier.heightIn(min = 48.dp)
                .semantics { stateDescription = if (mirror) "Bật" else "Tắt" }) {
                Text("Phản chiếu kính")
            }
            OutlinedButton(onClick = onOpenLimit, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Giới hạn ${(limitKmh * unit.factor).roundToInt()} ${unit.label}")
            }
        }
        Column(
            Modifier.fillMaxWidth().graphicsLayer { scaleX = if (mirror) -1f else 1f },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(state.status, color = if (state.speedKmh != null) accent else Muted,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("hud_source_status"))
            if (overSpeed) {
                Text("QUÁ GIỚI HẠN • GIẢM TỐC ĐỘ", color = Color(0xFFFF8585), fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF3B111B), RoundedCornerShape(12.dp))
                        .padding(16.dp).semantics { liveRegion = LiveRegionMode.Assertive })
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth >= 680.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        SpeedPanel(state.speedKmh, unit, accent, Modifier.weight(1f))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isObd) ObdMetrics(state, accent) else GpsMetrics(state, accent)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SpeedPanel(state.speedKmh, unit, accent, Modifier.fillMaxWidth())
                        if (isObd) ObdMetrics(state, accent) else GpsMetrics(state, accent)
                    }
                }
            }
            Text(
                if (isObd) "Chỉ hiển thị chỉ số do bộ điều khiển xe cung cấp. PID chưa có dữ liệu hiển thị —."
                else "Tốc độ và vị trí từ dịch vụ định vị điện thoại. GPS không cung cấp RPM, số hay nhiệt độ động cơ.",
                color = Muted, fontSize = 12.sp
            )
            Text("Ngưỡng cảnh báo: ${((limitKmh + bufferKmh) * unit.factor).roundToInt()} ${unit.label} • Rung ${if (warningEnabled) "bật" else "tắt"}",
                color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SpeedPanel(speedKmh: Float?, unit: SpeedUnit, accent: Color, modifier: Modifier) {
    val value = speedKmh?.let { (it * unit.factor).roundToInt().toString() } ?: "—"
    Column(modifier.background(Panel, RoundedCornerShape(24.dp)).padding(24.dp)
        .clearAndSetSemantics {
            contentDescription = if (speedKmh == null) "Tốc độ: chưa có dữ liệu" else "Tốc độ: $value ${unit.label}"
        }.testTag("real_speed_panel"), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TỐC ĐỘ", color = Muted, fontSize = 14.sp, letterSpacing = 3.sp)
        Text(value, color = accent, fontSize = 88.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        Text(unit.label, color = Color.White, fontSize = 20.sp)
    }
}

@Composable
private fun ObdMetrics(state: RealHudState, accent: Color) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("VÒNG TUA ĐỘNG CƠ", color = Muted, fontSize = 12.sp)
        Text(state.rpm?.let { "$it rpm" } ?: "—", color = accent, fontSize = 32.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.testTag("obd_rpm"))
        if (state.rpm != null) {
            // Visual scale only; the numeric RPM remains the authoritative measured value.
            LinearProgressIndicator(progress = { (state.rpm / 12_000f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clearAndSetSemantics { }, color = accent, trackColor = Color(0xFF283A33))
            Text("Thang hiển thị 0–12.000 rpm", color = Muted, fontSize = 11.sp)
        }
        Text(state.deviceName.ifBlank { "Chưa kết nối bộ đọc OBD" }, color = Muted, fontSize = 12.sp)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Metric("Nhiệt độ nước", state.coolantTempC?.toString(), "°C", accent, Modifier.weight(1f), "obd_coolant")
        Metric("Bướm ga", state.throttlePercent?.roundToInt()?.toString(), "%", accent, Modifier.weight(1f), "obd_throttle")
    }
    Metric("Điện áp bộ đọc OBD", state.batteryVoltage, "", accent, Modifier.fillMaxWidth(), "obd_voltage")
}

@Composable
private fun GpsMetrics(state: RealHudState, accent: Color) {
    val gps = state.gps
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Metric("Hướng di chuyển", gps?.bearingDegrees?.roundToInt()?.toString(), "°", accent, Modifier.weight(1f), "gps_bearing")
        Metric("Cao độ", gps?.altitudeMeters?.roundToInt()?.toString(), "m", accent, Modifier.weight(1f), "gps_altitude")
    }
    Metric("Độ chính xác vị trí", gps?.accuracyMeters?.roundToInt()?.let { "±$it" }, "m", accent, Modifier.fillMaxWidth(), "gps_accuracy")
    Metric("Vĩ độ", gps?.latitude?.let { String.format(Locale.US, "%.6f", it) }, "", accent, Modifier.fillMaxWidth(), "gps_latitude")
    Metric("Kinh độ", gps?.longitude?.let { String.format(Locale.US, "%.6f", it) }, "", accent, Modifier.fillMaxWidth(), "gps_longitude")
}

@Composable
private fun Metric(label: String, value: String?, unit: String, accent: Color, modifier: Modifier, tag: String) {
    val display = value?.let { "$it $unit".trim() } ?: "—"
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)
        .semantics(mergeDescendants = true) { }.testTag(tag), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(display, color = if (value == null) Muted else accent, fontSize = 24.sp,
            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { if (value == null) contentDescription = "Chưa có dữ liệu" })
    }
}
