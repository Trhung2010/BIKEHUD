package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

class LeanAngleSensor(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _leanAngleDegrees = MutableStateFlow(0f) // Negative = Lean Left, Positive = Lean Right
    val leanAngleDegrees: StateFlow<Float> = _leanAngleDegrees.asStateFlow()

    private val _maxLeftLean = MutableStateFlow(0f)
    val maxLeftLean: StateFlow<Float> = _maxLeftLean.asStateFlow()

    private val _maxRightLean = MutableStateFlow(0f)
    val maxRightLean: StateFlow<Float> = _maxRightLean.asStateFlow()

    private var isRegistered = false

    fun startListening() {
        if (!isRegistered && accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            sensorManager?.unregisterListener(this)
            isRegistered = false
        }
    }

    fun setSimulatedLean(angle: Float) {
        val clamped = angle.coerceIn(-55f, 55f)
        _leanAngleDegrees.value = clamped
        updateMaxAngles(clamped)
    }

    private fun updateMaxAngles(angle: Float) {
        if (angle < 0) {
            val leftMag = -angle
            if (leftMag > _maxLeftLean.value) _maxLeftLean.value = leftMag
        } else if (angle > 0) {
            if (angle > _maxRightLean.value) _maxRightLean.value = angle
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            // In landscape phone orientation, roll is computed from x and y axes
            val angleRad = atan2(x.toDouble(), y.toDouble())
            val angleDeg = Math.toDegrees(angleRad).toFloat()
            val filteredAngle = (angleDeg * 0.2f + _leanAngleDegrees.value * 0.8f).coerceIn(-60f, 60f)

            _leanAngleDegrees.value = filteredAngle
            updateMaxAngles(filteredAngle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun resetMaxLean() {
        _maxLeftLean.value = 0f
        _maxRightLean.value = 0f
    }
}
