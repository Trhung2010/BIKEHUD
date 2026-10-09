package com.example.model

/** A hardware fix. Null means the sensor did not supply this measurement. */
data class GpsReading(
    val receivedAtMs: Long = 0L,
    val speedKmh: Float? = null,
    val bearingDegrees: Float? = null,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class RealHudState(
    val mode: TelemetrySource = TelemetrySource.HUD_GPS,
    val status: String = "Đang chờ vị trí GPS",
    val speedKmh: Float? = null,
    val rpm: Int? = null,
    val coolantTempC: Int? = null,
    val throttlePercent: Float? = null,
    val batteryVoltage: String? = null,
    val gps: GpsReading? = null,
    val deviceName: String = ""
)

// Use elapsedRealtime for age checks so changes to the device clock cannot revive old data.
fun isFreshReading(timestampMs: Long, nowMs: Long, maxAgeMs: Long = 5_000L): Boolean =
    timestampMs > 0L && nowMs >= timestampMs && nowMs - timestampMs <= maxAgeMs

fun realHudState(source: TelemetrySource, obd: ObdTelemetry, gps: GpsReading, nowMs: Long): RealHudState {
    val mode = if (source == TelemetrySource.AUTO) {
        if (obd.connectionState == ObdConnectionState.CONNECTED &&
            isFreshReading(obd.speedUpdatedAtMs, nowMs)) TelemetrySource.HUD_OBD else TelemetrySource.HUD_GPS
    } else source
    val freshGps = gps.takeIf { isFreshReading(it.receivedAtMs, nowMs) }
    if (mode == TelemetrySource.HUD_GPS) {
        return RealHudState(
            mode = mode,
            status = when {
                freshGps?.speedKmh != null -> "GPS • Đang nhận dữ liệu"
                freshGps != null -> "GPS • Chưa có dữ liệu tốc độ"
                gps.receivedAtMs > 0L -> "GPS • Mất tín hiệu / dữ liệu đã cũ"
                else -> "GPS • Đang chờ vị trí / quyền truy cập"
            },
            speedKmh = freshGps?.speedKmh?.takeIf { it.isFinite() && it >= 0f },
            gps = freshGps
        )
    }
    val connected = obd.connectionState == ObdConnectionState.CONNECTED
    fun fresh(time: Long, age: Long = 5_000L) = connected && isFreshReading(time, nowMs, age)
    val speed = obd.speedKmh.toFloat().takeIf { fresh(obd.speedUpdatedAtMs) }
    return RealHudState(
        mode = TelemetrySource.HUD_OBD,
        status = when {
            !connected -> "OBD • ${obd.connectionState.title}"
            speed == null -> "OBD • Chưa có tốc độ ECU mới"
            else -> "OBD • Đang nhận dữ liệu ECU"
        },
        speedKmh = speed,
        rpm = obd.rpm.takeIf { fresh(obd.rpmUpdatedAtMs) },
        coolantTempC = obd.coolantTempC.takeIf { fresh(obd.coolantUpdatedAtMs, 30_000L) },
        throttlePercent = obd.throttlePercent.takeIf { fresh(obd.throttleUpdatedAtMs, 30_000L) },
        batteryVoltage = obd.batteryVoltage.takeIf { fresh(obd.voltageUpdatedAtMs, 60_000L) },
        deviceName = obd.connectedDeviceName
    )
}
