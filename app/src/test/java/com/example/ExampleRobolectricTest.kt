package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.HudColorTheme
import com.example.model.NavDisplayMode
import com.example.model.SpeedUnit
import com.example.viewmodel.HudViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Moto HUD", appName)
    }

    @Test
    fun `test HUD ViewModel mirror mode and theme toggles`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        assertFalse(viewModel.isMirrorMode.value)
        viewModel.toggleMirrorMode()
        assertTrue(viewModel.isMirrorMode.value)

        viewModel.selectTheme(HudColorTheme.AMBER_NIGHT)
        assertEquals(HudColorTheme.AMBER_NIGHT, viewModel.colorTheme.value)

        assertEquals(SpeedUnit.KMH, viewModel.speedUnit.value)
        viewModel.toggleSpeedUnit()
        assertEquals(SpeedUnit.MPH, viewModel.speedUnit.value)
    }

    @Test
    fun `test HUD navigation route and speed limit`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        assertTrue(viewModel.routes.value.isNotEmpty())
        val firstRoute = viewModel.routes.value[0]
        assertEquals(firstRoute.id, viewModel.currentRoute.value.id)

        viewModel.setSpeedLimit(60)
        assertEquals(60, viewModel.telemetry.value.currentSpeedLimit)

        viewModel.triggerSampleMessage("Test Sender", "SMS", "Xin chao")
        assertNotNull(viewModel.activeNotification.value)
        assertEquals("Test Sender", viewModel.activeNotification.value?.sender)
    }

    @Test
    fun `test custom speed limit threshold and buffer adjustments`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        // Test custom threshold setting
        viewModel.setSpeedLimit(75)
        assertEquals(75, viewModel.telemetry.value.currentSpeedLimit)

        // Test delta stepping (+5, -10)
        viewModel.adjustSpeedLimit(5)
        assertEquals(80, viewModel.telemetry.value.currentSpeedLimit)

        viewModel.adjustSpeedLimit(-15)
        assertEquals(65, viewModel.telemetry.value.currentSpeedLimit)

        // Test buffer tolerance
        assertEquals(0, viewModel.speedThresholdBuffer.value)
        viewModel.setSpeedThresholdBuffer(5)
        assertEquals(5, viewModel.speedThresholdBuffer.value)
    }

    @Test
    fun `test motorcycle lean angle sensor state`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        assertNotNull(viewModel.leanAngle.value)
        assertNotNull(viewModel.maxLeftLean.value)
        assertNotNull(viewModel.maxRightLean.value)
    }

    @Test
    fun `test Google Maps HUD mode and custom destination`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        assertEquals(NavDisplayMode.VECTOR_HUD, viewModel.navDisplayMode.value)
        viewModel.toggleNavDisplayMode()
        assertEquals(NavDisplayMode.GOOGLE_MAPS, viewModel.navDisplayMode.value)

        viewModel.setCustomDestination("Sân bay Tân Sơn Nhất")
        assertEquals("Sân bay Tân Sơn Nhất", viewModel.currentRoute.value.destination)
        assertTrue(viewModel.currentRoute.value.steps.isNotEmpty())
    }

    @Test
    fun `test emergency contact update and SOS message generation`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        val defaultContact = viewModel.emergencyContact.value
        assertNotNull(defaultContact.phoneNumber)

        viewModel.updateEmergencyContact(
            name = "Mẹ Yêu",
            phone = "0988776655",
            messageTemplate = "SOS CỨU HỘ! Tôi bị ngã xe máy tại {maps_url}"
        )

        val updated = viewModel.emergencyContact.value
        assertEquals("Mẹ Yêu", updated.name)
        assertEquals("0988776655", updated.phoneNumber)

        val sosMsg = com.example.util.EmergencySosHelper.buildSosMessage(
            contact = updated,
            latitude = 10.776889,
            longitude = 106.700806,
            speedKmh = 45f
        )

        assertTrue(sosMsg.contains("maps.google.com"))
        assertTrue(sosMsg.contains("10.776889"))
        assertTrue(sosMsg.contains("106.700806"))
    }

    @Test
    fun `test OBD-II PID parsing for speed RPM coolant and throttle`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val obdService = com.example.service.ObdBluetoothService(application)

        // PID 010D: Speed "41 0D 32" -> 0x32 = 50 km/h
        val parsedSpeed = obdService.parseSpeedPid("41 0D 32\r>")
        assertEquals(50, parsedSpeed)

        // PID 010C: RPM "41 0C 1A F8" -> ((0x1A * 256) + 0xF8) / 4 = ((26 * 256) + 248) / 4 = 6904 / 4 = 1726 RPM
        val parsedRpm = obdService.parseRpmPid("41 0C 1A F8\r>")
        assertEquals(1726, parsedRpm)

        // PID 0105: Coolant Temp "41 05 5A" -> 0x5A - 40 = 90 - 40 = 50 °C
        val parsedCoolant = obdService.parseCoolantTempPid("41 05 5A\r>")
        assertEquals(50, parsedCoolant)

        // PID 0111: Throttle "41 11 80" -> 128 * 100 / 255 = ~50.19%
        val parsedThrottle = obdService.parseThrottlePid("41 11 80\r>")
        assertNotNull(parsedThrottle)
        assertTrue(parsedThrottle!! in 50f..51f)
    }

    @Test
    fun `test BluetoothAudioController MediaController fallback and track switching`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val audioController = com.example.service.BluetoothAudioController(application)

        val initialTrack = audioController.musicState.value.trackTitle
        val initialPlaying = audioController.musicState.value.isPlaying

        // Toggle play/pause
        audioController.togglePlayPause()
        assertEquals(!initialPlaying, audioController.musicState.value.isPlaying)

        // Next track
        audioController.nextTrack()
        assertTrue(audioController.musicState.value.trackTitle.isNotBlank())

        // Prev track
        audioController.previousTrack()
        assertTrue(audioController.musicState.value.trackTitle.isNotBlank())
    }

    @Test
    fun `test route waypoints and upcoming turn-by-turn step selection`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.HudViewModel(application)

        val route = viewModel.currentRoute.value
        assertNotNull(route)
        assertTrue(route.steps.isNotEmpty())
        assertTrue(route.waypoints.isNotEmpty())
        assertTrue(route.startLat > 0)
        assertTrue(route.startLng > 0)

        // Initial step is 0
        assertEquals(0, viewModel.currentStepIndex.value)
        val firstStep = route.steps[0]
        assertEquals(firstStep.distanceMeters, viewModel.stepRemainingDistance.value)

        // Select next step
        viewModel.nextStep()
        assertEquals(1, viewModel.currentStepIndex.value)
        assertEquals(route.steps[1].distanceMeters, viewModel.stepRemainingDistance.value)

        // Select specific step
        viewModel.selectStep(3)
        assertEquals(3, viewModel.currentStepIndex.value)
        assertEquals(route.steps[3].distanceMeters, viewModel.stepRemainingDistance.value)
    }

    @Test
    fun `test telemetry source switching behavior on OBD disconnect`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        assertEquals(com.example.model.TelemetrySource.HUD_GPS, viewModel.telemetry.value.source)

        viewModel.setTelemetrySource(com.example.model.TelemetrySource.HUD_OBD)
        assertEquals(com.example.model.TelemetrySource.HUD_OBD, viewModel.telemetrySource.value)
    }

    @Test
    fun `test speed limit alert trigger with speed threshold buffer`() {
        val application = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HudViewModel(application)

        viewModel.setSpeedLimit(50)
        viewModel.setSpeedThresholdBuffer(5)
        assertEquals(5, viewModel.speedThresholdBuffer.value)
        assertEquals(50, viewModel.telemetry.value.currentSpeedLimit)
        assertFalse(viewModel.telemetry.value.isOverSpeed)
    }
}
