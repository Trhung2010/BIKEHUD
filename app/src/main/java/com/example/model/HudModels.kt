package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Color themes optimized for HUD reflection on windshields and visor glass.
 * High-contrast OLED dark background with luminous phosphor/neon tones.
 */
enum class HudColorTheme(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val warningColor: Color
) {
    NEON_CYAN(
        title = "Neon Cyan (Băng Lam)",
        primaryColor = Color(0xFF00F5FF),
        secondaryColor = Color(0xFF0099FF),
        accentColor = Color(0xFF70FFFF),
        warningColor = Color(0xFFFF3B30)
    ),
    AMBER_NIGHT(
        title = "Night Amber (Hổ Phách Đêm)",
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFFFF8F00),
        accentColor = Color(0xFFFFE082),
        warningColor = Color(0xFFFF1744)
    ),
    EMERALD_AERO(
        title = "Aero Green (Lục Bảo Phi Cơ)",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF00B0FF),
        accentColor = Color(0xFF69F0AE),
        warningColor = Color(0xFFFF5252)
    ),
    RACING_RED(
        title = "Racing Crimson (Đỏ Đua)",
        primaryColor = Color(0xFFFF1744),
        secondaryColor = Color(0xFFFF5252),
        accentColor = Color(0xFFFF8A80),
        warningColor = Color(0xFFFFD600)
    ),
    ICE_WHITE(
        title = "Ghost White (Trắng Tuyết)",
        primaryColor = Color(0xFFF0F4F8),
        secondaryColor = Color(0xFF90A4AE),
        accentColor = Color(0xFFFFFFFF),
        warningColor = Color(0xFFFF3D00)
    )
}

enum class SpeedUnit(val label: String, val factor: Float) {
    KMH("km/h", 1f),
    MPH("mph", 0.621371f)
}

enum class NavDisplayMode(val label: String) {
    VECTOR_HUD("HUD Chỉ Đường"),
    GOOGLE_MAPS("Google Maps HUD")
}

enum class TurnType {
    STRAIGHT,
    SLIGHT_RIGHT,
    TURN_RIGHT,
    SHARP_RIGHT,
    SLIGHT_LEFT,
    TURN_LEFT,
    SHARP_LEFT,
    U_TURN,
    ROUNDABOUT,
    DESTINATION
}

data class NavStep(
    val instruction: String,
    val streetName: String,
    val distanceMeters: Int,
    val turnType: TurnType,
    val laneInfo: String = "Làn xe máy / hỗn hợp"
)

data class RoutePreset(
    val id: String,
    val name: String,
    val destination: String,
    val totalDistanceKm: Float,
    val estimatedTimeMinutes: Int,
    val steps: List<NavStep>
)

data class MessageNotification(
    val id: String,
    val sender: String,
    val appSource: String, // SMS, Zalo, WhatsApp, Messenger, etc.
    val content: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class BluetoothMusicState(
    val isConnected: Boolean = true,
    val deviceName: String = "Cardo Packtalk Edge (Helmet)",
    val batteryPercent: Int = 85,
    val isPlaying: Boolean = true,
    val trackTitle: String = "Nơi Này Có Anh",
    val artist: String = "Sơn Tùng M-TP",
    val volumeLevel: Float = 0.75f, // 0.0 to 1.0
    val progressMs: Long = 65000,
    val durationMs: Long = 238000
)

data class RideTelemetry(
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val averageSpeedKmh: Float = 0f,
    val rpm: Int = 0,
    val gear: String = "N",
    val tripDistanceKm: Float = 0f,
    val rideDurationSeconds: Long = 0,
    val compassHeadingDegrees: Float = 0f,
    val currentSpeedLimit: Int = 50,
    val altitudeMeters: Float = 18f,
    val isOverSpeed: Boolean = false,
    val source: TelemetrySource = TelemetrySource.HUD_GPS,
    val coolantTempC: Int = 85,
    val batteryVoltage: String = "12.6V",
    val throttlePercent: Float = 0f,
    val gpsAccuracyMeters: Float = 0f,
    val satellitesCount: Int = 0
)

enum class TelemetrySource(val label: String, val badgeText: String) {
    HUD_GPS("GPS Vệ Tinh Trực Tiếp", "GPS LIVE"),
    HUD_OBD("Cổng OBD-II Trực Tiếp", "OBD-II LIVE"),
    AUTO("Tự Động (Ưu Tiên OBD)", "AUTO LIVE")
}

enum class ObdConnectionState(val title: String) {
    DISCONNECTED("Chưa kết nối"),
    CONNECTING("Đang kết nối Bluetooth..."),
    INITIALIZING("Khởi tạo ELM327..."),
    CONNECTED("Đã kết nối ECU (Trực tiếp)"),
    ERROR("Lỗi kết nối OBD")
}

data class ObdTelemetry(
    val speedKmh: Int = 0,
    val rpm: Int = 0,
    val coolantTempC: Int = 85,
    val throttlePercent: Float = 0f,
    val fuelPercent: Float = 68f,
    val batteryVoltage: String = "12.6V",
    val engineLoadPercent: Float = 15f,
    val connectionState: ObdConnectionState = ObdConnectionState.DISCONNECTED,
    val connectedDeviceName: String = "",
    val connectedDeviceAddress: String = "",
    val protocol: String = "ISO 15765-4 CAN",
    val errorMessage: String = "",
    val lastUpdateMs: Long = 0L
)

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isObdLikely: Boolean = false
)

data class EmergencyContact(
    val name: String = "Người thân (Cứu hộ)",
    val phoneNumber: String = "0912345678",
    val customMessageTemplate: String = "CẦN TRỢ GIÚP KHẨN CẤP! Tôi đang gặp sự cố khi chạy xe máy. Vị trí GPS của tôi: {maps_url}"
)

