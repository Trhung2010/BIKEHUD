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
}
