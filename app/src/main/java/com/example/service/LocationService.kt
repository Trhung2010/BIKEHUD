package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data model representing real-time telemetry from GPS hardware
 * designed specifically for motorcycle speedometer and HUD display.
 */
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedKmh: Float = 0f,
    val speedMps: Float = 0f,
    val speedMph: Float = 0f,
    val bearingDegrees: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val hasValidSpeed: Boolean = false,
    val rawLocation: Location? = null
)

/**
 * LocationService uses Google Play Services [FusedLocationProviderClient]
 * to deliver high-precision, low-latency GPS speed and coordinates for the speedometer.
 *
 * Features:
 * - High-frequency updates (up to 2-3 Hz) optimal for high-speed motorcycle HUDs.
 * - Hardware speed conversion (m/s to km/h and mph).
 * - Fallback speed estimation from consecutive location deltas if hardware hasSpeed() is absent.
 * - Jitter deadband filtering when stationary at traffic stops to prevent phantom speed drift.
 * - Reactive StateFlow streams for Jetpack Compose UI.
 */
open class LocationService(protected val context: Context) {

    private val tag = "LocationService"

    protected val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Reactive StateFlows for HUD Composables & ViewModel
    private val _realLocation = MutableStateFlow<Location?>(null)
    val realLocation: StateFlow<Location?> = _realLocation.asStateFlow()

    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(0f)
    val currentSpeedKmh: StateFlow<Float> = _currentSpeedKmh.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private val _gpsAccuracy = MutableStateFlow(0f)
    val gpsAccuracy: StateFlow<Float> = _gpsAccuracy.asStateFlow()

    private val _totalDistanceTraveledKm = MutableStateFlow(0.0)
    val totalDistanceTraveledKm: StateFlow<Double> = _totalDistanceTraveledKm.asStateFlow()

    private var locationCallback: LocationCallback? = null

    // For calculating fallback speed if device GPS chip omits hasSpeed()
    private var previousLocation: Location? = null
    private var previousTimestamp: Long = 0L

    /**
     * Attempts to immediately fetch the last known location from the provider
     * to populate coordinates on the HUD while waiting for the first live GPS satellite fix.
     */
    @SuppressLint("MissingPermission")
    fun requestLastKnownLocation(onResult: ((Location?) -> Unit)? = null) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    _realLocation.value = loc
                    _gpsAccuracy.value = loc.accuracy
                    val data = parseLocation(loc)
                    _locationData.value = data
                    _currentSpeedKmh.value = data.speedKmh
                    onResult?.invoke(loc)
                } else {
                    onResult?.invoke(null)
                }
            }.addOnFailureListener { e ->
                Log.w(tag, "Failed to retrieve last known location", e)
                onResult?.invoke(null)
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Location permission not granted for lastKnownLocation", e)
            onResult?.invoke(null)
        } catch (e: Exception) {
            Log.e(tag, "Error accessing fusedLocationClient lastLocation", e)
            onResult?.invoke(null)
        }
    }

    /**
     * Starts continuous GPS tracking with high accuracy and high refresh rate
     * suited for motorcycle riding and windshield HUD projection.
     */
    @SuppressLint("MissingPermission")
    fun startTracking(
        updateIntervalMillis: Long = 1000L,
        minIntervalMillis: Long = 400L,
        minDistanceMeters: Float = 0.2f,
        onLocationUpdate: ((speedKmh: Float, bearingDegrees: Float, altitude: Float, accuracyMeters: Float, location: Location) -> Unit)? = null
    ) {
        stopTracking()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, updateIntervalMillis)
            .setMinUpdateIntervalMillis(minIntervalMillis)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                val data = parseLocation(loc)

                _realLocation.value = loc
                _locationData.value = data
                _currentSpeedKmh.value = data.speedKmh
                _gpsAccuracy.value = data.accuracyMeters
                _isGpsActive.value = true

                // Update cumulative trip odometer if moving
                val prev = previousLocation
                if (prev != null && data.speedKmh > 1.0f) {
                    val deltaMeters = prev.distanceTo(loc)
                    if (deltaMeters in 0.5f..500f) { // Reject teleport anomalies
                        _totalDistanceTraveledKm.value += (deltaMeters / 1000.0)
                    }
                }
                previousLocation = loc
                previousTimestamp = loc.time

                onLocationUpdate?.invoke(
                    data.speedKmh,
                    data.bearingDegrees,
                    data.altitudeMeters.toFloat(),
                    data.accuracyMeters,
                    loc
                )
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isGpsActive.value = true
        } catch (e: SecurityException) {
            Log.e(tag, "SecurityException: Location permission denied", e)
            _isGpsActive.value = false
        } catch (e: Exception) {
            Log.e(tag, "Exception during requestLocationUpdates", e)
            _isGpsActive.value = false
        }
    }

    /**
     * Backward-compatible delegation for existing ViewModel and HUD calls.
     */
    @SuppressLint("MissingPermission")
    open fun startGpsUpdates(
        onLocation: (speedKmh: Float, bearingDegrees: Float, alt: Float, accuracyMeters: Float) -> Unit
    ) {
        startTracking { speedKmh, bearing, alt, accuracy, _ ->
            onLocation(speedKmh, bearing, alt, accuracy)
        }
    }

    /**
     * Stops continuous location updates to preserve battery when parked or simulation mode active.
     */
    open fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        previousLocation = null
        previousTimestamp = 0L
        _isGpsActive.value = false
    }

    open fun stopGpsUpdates() {
        stopTracking()
    }

    /**
     * Parses an Android [Location] instance into our structured [LocationData],
     * with fallback speed calculation and zero-drift thresholding.
     */
    private fun parseLocation(loc: Location): LocationData {
        var speedMps = 0f
        var hasSpeed = false

        if (loc.hasSpeed() && loc.speed >= 0f) {
            speedMps = loc.speed
            hasSpeed = true
        } else {
            // Delta fallback calculation between consecutive pings
            val prev = previousLocation
            val prevTime = previousTimestamp
            val nowTime = if (loc.time > 0L) loc.time else System.currentTimeMillis()

            if (prev != null && prevTime > 0L && nowTime > prevTime) {
                val dtSec = (nowTime - prevTime) / 1000.0
                if (dtSec in 0.1..5.0) {
                    val distMeters = prev.distanceTo(loc)
                    speedMps = (distMeters / dtSec).toFloat().coerceIn(0f, 100f) // 100 m/s ~ 360 km/h cap
                    hasSpeed = true
                }
            }
        }

        var speedKmh = speedMps * 3.6f

        // Stationary deadband filter: eliminate GPS satellite noise drift when bike is parked/stopped
        if (speedKmh < 1.2f && loc.accuracy > 10f) {
            speedKmh = 0f
            speedMps = 0f
        }

        val speedMph = speedKmh * 0.621371f
        val bearing = if (loc.hasBearing()) loc.bearing else 0f
        val altitude = if (loc.hasAltitude()) loc.altitude else 15.0

        return LocationData(
            latitude = loc.latitude,
            longitude = loc.longitude,
            speedKmh = speedKmh.coerceAtLeast(0f),
            speedMps = speedMps.coerceAtLeast(0f),
            speedMph = speedMph.coerceAtLeast(0f),
            bearingDegrees = bearing,
            altitudeMeters = altitude,
            accuracyMeters = loc.accuracy,
            timestamp = loc.time,
            hasValidSpeed = hasSpeed,
            rawLocation = loc
        )
    }

    /**
     * Returns the current (lat, lng) coordinates or a reliable default in Vietnam.
     */
    open fun getCurrentCoordinates(
        defaultLat: Double = 10.7769,
        defaultLng: Double = 106.7009
    ): Pair<Double, Double> {
        val last = _realLocation.value
        return if (last != null) {
            Pair(last.latitude, last.longitude)
        } else {
            Pair(defaultLat, defaultLng)
        }
    }

    /**
     * Resets the cumulative GPS odometer.
     */
    fun resetTripDistance() {
        _totalDistanceTraveledKm.value = 0.0
    }
}
