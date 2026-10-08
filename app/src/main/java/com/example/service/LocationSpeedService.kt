package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationSpeedService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _realLocation = MutableStateFlow<Location?>(null)
    val realLocation: StateFlow<Location?> = _realLocation.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private val _gpsAccuracy = MutableStateFlow(0f)
    val gpsAccuracy: StateFlow<Float> = _gpsAccuracy.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startGpsUpdates(onLocation: (speedKmh: Float, bearingDegrees: Float, alt: Float, accuracyMeters: Float) -> Unit) {
        stopGpsUpdates()
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(400L)
            .setMinUpdateDistanceMeters(0.2f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                _realLocation.value = loc
                _isGpsActive.value = true
                _gpsAccuracy.value = loc.accuracy

                // Location.speed is in meters/second. 1 m/s = 3.6 km/h
                val speedKmh = if (loc.hasSpeed()) (loc.speed * 3.6f) else 0f
                val bearing = if (loc.hasBearing()) loc.bearing else 0f
                val altitude = if (loc.hasAltitude()) loc.altitude.toFloat() else 15f
                val accuracy = loc.accuracy

                onLocation(speedKmh, bearing, altitude, accuracy)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isGpsActive.value = true
        } catch (_: SecurityException) {
            _isGpsActive.value = false
        } catch (_: Exception) {
            _isGpsActive.value = false
        }
    }

    fun stopGpsUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        _isGpsActive.value = false
    }

    fun getCurrentCoordinates(defaultLat: Double = 10.7769, defaultLng: Double = 106.7009): Pair<Double, Double> {
        val last = _realLocation.value
        return if (last != null) {
            Pair(last.latitude, last.longitude)
        } else {
            Pair(defaultLat, defaultLng)
        }
    }
}
