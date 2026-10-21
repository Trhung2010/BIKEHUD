package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.service.LocationData
import com.example.service.LocationService
import com.example.service.LocationSpeedService
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
class LocationServiceTest {

    @Test
    fun `test LocationService initial state`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val locationService = LocationService(application)

        assertFalse(locationService.isGpsActive.value)
        assertEquals(0f, locationService.currentSpeedKmh.value, 0.01f)
        assertEquals(0.0, locationService.totalDistanceTraveledKm.value, 0.001)

        val coords = locationService.getCurrentCoordinates()
        assertEquals(10.7769, coords.first, 0.0001)
        assertEquals(106.7009, coords.second, 0.0001)
    }

    @Test
    fun `test LocationData model values and conversions`() {
        val data = LocationData(
            latitude = 10.7769,
            longitude = 106.7009,
            speedKmh = 60f,
            speedMps = 16.67f,
            speedMph = 37.28f,
            bearingDegrees = 180f,
            altitudeMeters = 25.0,
            accuracyMeters = 4.5f,
            hasValidSpeed = true
        )

        assertEquals(10.7769, data.latitude, 0.0001)
        assertEquals(106.7009, data.longitude, 0.0001)
        assertEquals(60f, data.speedKmh, 0.01f)
        assertEquals(37.28f, data.speedMph, 0.01f)
        assertTrue(data.hasValidSpeed)
    }

    @Test
    fun `test LocationSpeedService backward compatibility subclass`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val legacyService = LocationSpeedService(application)

        assertNotNull(legacyService)
        assertFalse(legacyService.isGpsActive.value)

        val coords = legacyService.getCurrentCoordinates(21.0285, 105.8542)
        assertEquals(21.0285, coords.first, 0.0001)
        assertEquals(105.8542, coords.second, 0.0001)
    }

    @Test
    fun `test LocationService resetTripDistance`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val locationService = LocationService(application)

        locationService.resetTripDistance()
        assertEquals(0.0, locationService.totalDistanceTraveledKm.value, 0.0001)
    }
}
