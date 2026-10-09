package com.example.model

import org.junit.Assert.*
import org.junit.Test

class RealHudStateTest {
    private val now = 100_000L
    private val gps = GpsReading(receivedAtMs = now, speedKmh = 48f, latitude = 10.7, longitude = 106.6)
    private val connected = ObdTelemetry(connectionState = ObdConnectionState.CONNECTED)

    @Test fun defaultsAreNotMeasurements() {
        val state = realHudState(TelemetrySource.HUD_OBD, connected, gps, now)
        assertNull(state.speedKmh)
        assertNull(state.rpm)
        assertNull(state.coolantTempC)
        assertNull(state.batteryVoltage)
        assertNull(state.throttlePercent)
    }

    @Test fun gpsDoesNotInventEngineMetrics() {
        val state = realHudState(TelemetrySource.HUD_GPS,
            connected.copy(rpm = 4500, rpmUpdatedAtMs = now), gps, now)
        assertEquals(48f, state.speedKmh!!, 0f)
        assertNull(state.rpm)
        assertNull(state.coolantTempC)
        assertNull(state.batteryVoltage)
    }

    @Test fun obdDoesNotFallBackToGpsWhenDisconnected() {
        val state = realHudState(TelemetrySource.HUD_OBD, ObdTelemetry(), gps, now)
        assertEquals(TelemetrySource.HUD_OBD, state.mode)
        assertNull(state.speedKmh)
        assertNull(state.gps)
    }

    @Test fun zeroSpeedIsAValidMeasurement() {
        val state = realHudState(TelemetrySource.HUD_OBD,
            connected.copy(speedKmh = 0, speedUpdatedAtMs = now), gps, now)
        assertEquals(0f, state.speedKmh!!, 0f)
    }

    @Test fun eachPidExpiresIndependently() {
        val obd = connected.copy(speedKmh = 60, speedUpdatedAtMs = now,
            rpm = 5000, rpmUpdatedAtMs = now - 5_001,
            coolantTempC = 92, coolantUpdatedAtMs = now - 29_000,
            throttlePercent = 12f, throttleUpdatedAtMs = now - 30_001,
            batteryVoltage = "13.8V", voltageUpdatedAtMs = now - 60_001)
        val state = realHudState(TelemetrySource.HUD_OBD, obd, gps, now)
        assertEquals(60f, state.speedKmh!!, 0f)
        assertNull(state.rpm)
        assertEquals(92, state.coolantTempC)
        assertNull(state.throttlePercent)
        assertNull(state.batteryVoltage)
    }

    @Test fun disconnectHidesEvenFreshCachedPids() {
        val state = realHudState(TelemetrySource.HUD_OBD,
            connected.copy(connectionState = ObdConnectionState.DISCONNECTED,
                speedUpdatedAtMs = now, rpmUpdatedAtMs = now), gps, now)
        assertNull(state.speedKmh)
        assertNull(state.rpm)
    }

    @Test fun staleGpsHidesSpeedAndCoordinates() {
        val state = realHudState(TelemetrySource.HUD_GPS, connected,
            gps.copy(receivedAtMs = now - 5_001), now)
        assertNull(state.speedKmh)
        assertNull(state.gps)
    }

    @Test fun fixWithoutSpeedDoesNotMeanStationary() {
        val state = realHudState(TelemetrySource.HUD_GPS, connected, gps.copy(speedKmh = null), now)
        assertNull(state.speedKmh)
        assertNotNull(state.gps)
    }

    @Test fun futureAndNeverReceivedTimestampsAreRejected() {
        assertFalse(isFreshReading(0L, now))
        assertFalse(isFreshReading(now + 1, now))
        assertTrue(isFreshReading(now - 5_000, now))
    }

    @Test fun autoRequiresAFreshObdSpeed() {
        assertEquals(TelemetrySource.HUD_GPS,
            realHudState(TelemetrySource.AUTO, connected, gps, now).mode)
        assertEquals(TelemetrySource.HUD_OBD,
            realHudState(TelemetrySource.AUTO, connected.copy(speedUpdatedAtMs = now), gps, now).mode)
    }
}
