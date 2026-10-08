package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NavigationRepository
import com.example.model.BluetoothDeviceInfo
import com.example.model.BluetoothMusicState
import com.example.model.EmergencyContact
import com.example.model.HudColorTheme
import com.example.model.MessageNotification
import com.example.model.NavDisplayMode
import com.example.model.NavStep
import com.example.model.ObdConnectionState
import com.example.model.ObdTelemetry
import com.example.model.RideTelemetry
import com.example.model.RoutePreset
import com.example.model.SpeedUnit
import com.example.model.TelemetrySource
import com.example.model.TurnType
import com.example.service.BluetoothAudioController
import com.example.service.LeanAngleSensor
import com.example.service.LocationSpeedService
import com.example.service.ObdBluetoothService
import com.example.util.EmergencySosHelper
import com.example.util.HapticHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class HudViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothController = BluetoothAudioController(application)
    private val locationSpeedService = LocationSpeedService(application)
    private val leanAngleSensor = LeanAngleSensor(application)
    private val obdService = ObdBluetoothService(application)

    val musicState: StateFlow<BluetoothMusicState> = bluetoothController.musicState
    val isGpsActive: StateFlow<Boolean> = locationSpeedService.isGpsActive
    val gpsAccuracy: StateFlow<Float> = locationSpeedService.gpsAccuracy
    val leanAngle: StateFlow<Float> = leanAngleSensor.leanAngleDegrees
    val maxLeftLean: StateFlow<Float> = leanAngleSensor.maxLeftLean
    val maxRightLean: StateFlow<Float> = leanAngleSensor.maxRightLean

    // Real OBD-II Diagnostic Telemetry from Bluetooth ELM327
    val obdTelemetry: StateFlow<ObdTelemetry> = obdService.obdTelemetry

    // Telemetry Source: HUD_GPS vs HUD_OBD vs AUTO
    private val _telemetrySource = MutableStateFlow(TelemetrySource.AUTO)
    val telemetrySource: StateFlow<TelemetrySource> = _telemetrySource.asStateFlow()

    fun getCurrentCoordinates(): Pair<Double, Double> = locationSpeedService.getCurrentCoordinates()

    private val _telemetry = MutableStateFlow(RideTelemetry(currentSpeedLimit = 50, source = TelemetrySource.HUD_GPS))
    val telemetry: StateFlow<RideTelemetry> = _telemetry.asStateFlow()

    private val _colorTheme = MutableStateFlow(HudColorTheme.NEON_CYAN)
    val colorTheme: StateFlow<HudColorTheme> = _colorTheme.asStateFlow()

    // Mirror mode: Flips horizontally so reflection on the windshield appears correctly
    private val _isMirrorMode = MutableStateFlow(false)
    val isMirrorMode: StateFlow<Boolean> = _isMirrorMode.asStateFlow()

    // Night mode: true = ultra high contrast minimal dark; false = normal HUD daylight boost
    private val _isNightMode = MutableStateFlow(true)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.KMH)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    // Simulation mode: Defaults to FALSE (100% REAL HARDWARE GPS and REAL OBD DATA)
    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private val _routes = MutableStateFlow(NavigationRepository.routes)
    val routes: StateFlow<List<RoutePreset>> = _routes.asStateFlow()

    private val _navDisplayMode = MutableStateFlow(NavDisplayMode.VECTOR_HUD)
    val navDisplayMode: StateFlow<NavDisplayMode> = _navDisplayMode.asStateFlow()

    private val _currentRoute = MutableStateFlow(NavigationRepository.routes[0])
    val currentRoute: StateFlow<RoutePreset> = _currentRoute.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _stepRemainingDistance = MutableStateFlow(NavigationRepository.routes[0].steps[0].distanceMeters)
    val stepRemainingDistance: StateFlow<Int> = _stepRemainingDistance.asStateFlow()

    // Message notifications
    private val _activeNotification = MutableStateFlow<MessageNotification?>(null)
    val activeNotification: StateFlow<MessageNotification?> = _activeNotification.asStateFlow()

    private val _notificationHistory = MutableStateFlow<List<MessageNotification>>(emptyList())
    val notificationHistory: StateFlow<List<MessageNotification>> = _notificationHistory.asStateFlow()

    private val _autoReplyEnabled = MutableStateFlow(true)
    val autoReplyEnabled: StateFlow<Boolean> = _autoReplyEnabled.asStateFlow()

    private val _speedWarningEnabled = MutableStateFlow(true)
    val speedWarningEnabled: StateFlow<Boolean> = _speedWarningEnabled.asStateFlow()

    // Threshold buffer before triggering warning (0 km/h: immediately, 2 km/h, 5 km/h)
    private val _speedThresholdBuffer = MutableStateFlow(0)
    val speedThresholdBuffer: StateFlow<Int> = _speedThresholdBuffer.asStateFlow()

    private var lastOverSpeedState = false

    private var simulationJob: Job? = null
    private var telemetryTimerJob: Job? = null
    private var messageDismissJob: Job? = null

    // Simulation physics state (only used if user explicitly toggles Test/Lab Mode)
    private var simTick = 0f
    private var targetSpeed = 48f
    private var currentSimSpeed = 0f

    init {
        startTelemetryLoop()
        startRealGpsTracking()
        listenToObdUpdates()

        // Welcome motorcycle HUD notification
        viewModelScope.launch {
            delay(1500)
            triggerSampleMessage(
                sender = "Moto HUD System",
                appSource = "Hệ Thống",
                content = "Đang kích hoạt GPS vệ tinh thực tế & sẵn sàng kết nối cổng OBD-II!"
            )
        }
    }

    /**
     * Starts continuous GPS hardware tracking using FusedLocationProviderClient.
     */
    fun startRealGpsTracking() {
        leanAngleSensor.startListening()
        locationSpeedService.startGpsUpdates { gpsSpeedKmh, bearing, alt, accuracy ->
            if (!_isSimulationMode.value) {
                val isObdActive = obdTelemetry.value.connectionState == ObdConnectionState.CONNECTED &&
                        (_telemetrySource.value == TelemetrySource.HUD_OBD || _telemetrySource.value == TelemetrySource.AUTO)

                val effectiveSpeed = if (isObdActive) obdTelemetry.value.speedKmh.toFloat() else gpsSpeedKmh
                val limit = _telemetry.value.currentSpeedLimit
                val buffer = _speedThresholdBuffer.value
                val isOver = effectiveSpeed > (limit + buffer)

                if (isOver && !lastOverSpeedState && _speedWarningEnabled.value) {
                    HapticHelper.vibrateSpeedWarning(getApplication())
                }
                lastOverSpeedState = isOver

                val gear = calculateGear(effectiveSpeed)
                val rpm = if (isObdActive) obdTelemetry.value.rpm else calculateRpm(effectiveSpeed, gear)

                val metersDelta = (effectiveSpeed * (1000f / 3600f) * 0.4f).toInt()
                advanceNavMeters(metersDelta)

                _telemetry.update { current ->
                    current.copy(
                        speedKmh = effectiveSpeed.coerceAtLeast(0f),
                        compassHeadingDegrees = bearing,
                        altitudeMeters = alt,
                        gear = gear,
                        rpm = rpm,
                        isOverSpeed = isOver,
                        source = if (isObdActive) TelemetrySource.HUD_OBD else TelemetrySource.HUD_GPS,
                        gpsAccuracyMeters = accuracy
                    )
                }
            }
        }
    }

    /**
     * Listens to live vehicle ECU data received via Bluetooth OBD-II adapter.
     */
    private fun listenToObdUpdates() {
        viewModelScope.launch {
            obdService.obdTelemetry.collect { obd ->
                if (!_isSimulationMode.value && obd.connectionState == ObdConnectionState.CONNECTED &&
                    (_telemetrySource.value == TelemetrySource.HUD_OBD || _telemetrySource.value == TelemetrySource.AUTO)
                ) {
                    val speed = obd.speedKmh.toFloat()
                    val limit = _telemetry.value.currentSpeedLimit
                    val buffer = _speedThresholdBuffer.value
                    val isOver = speed > (limit + buffer)

                    if (isOver && !lastOverSpeedState && _speedWarningEnabled.value) {
                        HapticHelper.vibrateSpeedWarning(getApplication())
                    }
                    lastOverSpeedState = isOver

                    val gear = calculateGear(speed)
                    _telemetry.update {
                        it.copy(
                            speedKmh = speed,
                            rpm = obd.rpm,
                            gear = gear,
                            coolantTempC = obd.coolantTempC,
                            batteryVoltage = obd.batteryVoltage,
                            throttlePercent = obd.throttlePercent,
                            isOverSpeed = isOver,
                            source = TelemetrySource.HUD_OBD
                        )
                    }
                }
            }
        }
    }

    private fun startTelemetryLoop() {
        telemetryTimerJob?.cancel()
        telemetryTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                bluetoothController.updateProgress(1)
                _telemetry.update { current ->
                    val newDuration = current.rideDurationSeconds + 1
                    val distAdd = (current.speedKmh / 3600f) // km per second
                    val newTripDist = current.tripDistanceKm + distAdd
                    val newAvg = if (newDuration > 0) ((newTripDist / (newDuration / 3600f)).coerceAtLeast(0f)) else 0f
                    val newMax = maxOf(current.maxSpeedKmh, current.speedKmh)
                    current.copy(
                        rideDurationSeconds = newDuration,
                        tripDistanceKm = newTripDist,
                        averageSpeedKmh = newAvg,
                        maxSpeedKmh = newMax
                    )
                }
            }
        }
    }

    /**
     * Optional lab/demo simulation physics, run only when user explicitly toggles Test Mode.
     */
    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(100) // 10 updates per second
                if (_isSimulationMode.value) {
                    simTick += 0.1f
                    val cycle = (simTick % 60f)
                    targetSpeed = when {
                        cycle < 5f -> 0f
                        cycle < 12f -> 32f
                        cycle < 35f -> 52f + 6f * sin(simTick * 0.5f)
                        cycle < 45f -> 68f + 4f * sin(simTick * 0.3f)
                        cycle < 50f -> 45f
                        cycle < 55f -> 25f
                        else -> 0f
                    }

                    val diff = targetSpeed - currentSimSpeed
                    currentSimSpeed += diff * 0.08f

                    val calculatedGear = calculateGear(currentSimSpeed)
                    val calculatedRpm = calculateRpm(currentSimSpeed, calculatedGear)
                    val heading = (simTick * 12f) % 360f

                    val simulatedLean = if (currentSimSpeed > 8f) {
                        (sin(simTick * 0.35f) * 26f + sin(simTick * 0.12f) * 12f)
                    } else 0f
                    leanAngleSensor.setSimulatedLean(simulatedLean)

                    val metersDelta = (currentSimSpeed * (1000f / 3600f) * 0.1f).toInt()
                    advanceNavMeters(metersDelta)

                    val limit = _telemetry.value.currentSpeedLimit
                    val buffer = _speedThresholdBuffer.value
                    val isOver = currentSimSpeed > (limit + buffer)
                    if (isOver && !lastOverSpeedState && _speedWarningEnabled.value) {
                        HapticHelper.vibrateSpeedWarning(getApplication())
                    }
                    lastOverSpeedState = isOver

                    _telemetry.update {
                        it.copy(
                            speedKmh = currentSimSpeed.coerceAtLeast(0f),
                            rpm = calculatedRpm,
                            gear = calculatedGear,
                            compassHeadingDegrees = heading,
                            isOverSpeed = isOver
                        )
                    }
                }
            }
        }
    }

    private fun advanceNavMeters(meters: Int) {
        if (meters <= 0) return
        val currentStep = _currentStepIndex.value
        val route = _currentRoute.value
        val steps = route.steps
        if (currentStep >= steps.size) return

        val remaining = _stepRemainingDistance.value - meters
        if (remaining <= 0) {
            val nextStep = currentStep + 1
            if (nextStep < steps.size) {
                _currentStepIndex.value = nextStep
                _stepRemainingDistance.value = steps[nextStep].distanceMeters
            } else {
                _currentStepIndex.value = 0
                _stepRemainingDistance.value = steps[0].distanceMeters
            }
        } else {
            _stepRemainingDistance.value = remaining
        }
    }

    fun calculateGear(speed: Float): String {
        return when {
            speed < 1f -> "N"
            speed < 18f -> "1"
            speed < 32f -> "2"
            speed < 48f -> "3"
            speed < 62f -> "4"
            speed < 80f -> "5"
            else -> "6"
        }
    }

    fun calculateRpm(speed: Float, gear: String): Int {
        if (gear == "N" || speed < 1f) return 1200 + (sin(simTick * 2f) * 80).toInt()
        val gearFactor = when (gear) {
            "1" -> 280
            "2" -> 200
            "3" -> 150
            "4" -> 120
            "5" -> 100
            "6" -> 85
            else -> 100
        }
        return (speed * gearFactor).toInt().coerceIn(1200, 11500)
    }

    // OBD Management
    fun setTelemetrySource(source: TelemetrySource) {
        _telemetrySource.value = source
        _telemetry.update { it.copy(source = source) }
    }

    fun getPairedObdDevices(): List<BluetoothDeviceInfo> = obdService.getPairedDevices()

    fun connectObd(address: String, name: String) = obdService.connectToDevice(address, name)

    fun disconnectObd() = obdService.disconnect()

    // Toggle mirror mode (flip horizontally for windshield reflection)
    fun toggleMirrorMode() {
        _isMirrorMode.update { !it }
    }

    fun setMirrorMode(enabled: Boolean) {
        _isMirrorMode.value = enabled
    }

    // Toggle night mode
    fun toggleNightMode() {
        _isNightMode.update { !it }
    }

    // Change HUD Color Theme
    fun selectTheme(theme: HudColorTheme) {
        _colorTheme.value = theme
    }

    // Toggle Speed Unit (km/h vs mph)
    fun toggleSpeedUnit() {
        _speedUnit.update { if (it == SpeedUnit.KMH) SpeedUnit.MPH else SpeedUnit.KMH }
    }

    // Toggle Simulation Mode vs Real Hardware Sensors
    fun toggleSimulationMode() {
        val newSim = !_isSimulationMode.value
        _isSimulationMode.value = newSim
        if (!newSim) {
            simulationJob?.cancel()
            startRealGpsTracking()
        } else {
            startSimulation()
        }
    }

    fun setSpeedLimit(limit: Int) {
        val clamped = limit.coerceIn(20, 160)
        val buffer = _speedThresholdBuffer.value
        _telemetry.update {
            it.copy(
                currentSpeedLimit = clamped,
                isOverSpeed = it.speedKmh > (clamped + buffer)
            )
        }
    }

    fun adjustSpeedLimit(delta: Int) {
        val current = _telemetry.value.currentSpeedLimit
        setSpeedLimit(current + delta)
    }

    fun setSpeedThresholdBuffer(buffer: Int) {
        _speedThresholdBuffer.value = buffer
        val limit = _telemetry.value.currentSpeedLimit
        _telemetry.update {
            it.copy(isOverSpeed = it.speedKmh > (limit + buffer))
        }
    }

    fun selectRoute(route: RoutePreset) {
        _currentRoute.value = route
        _currentStepIndex.value = 0
        _stepRemainingDistance.value = route.steps.firstOrNull()?.distanceMeters ?: 500
    }

    fun toggleNavDisplayMode() {
        _navDisplayMode.update {
            if (it == NavDisplayMode.VECTOR_HUD) NavDisplayMode.GOOGLE_MAPS
            else NavDisplayMode.VECTOR_HUD
        }
    }

    fun setNavDisplayMode(mode: NavDisplayMode) {
        _navDisplayMode.value = mode
    }

    fun setCustomDestination(destName: String) {
        val customRoute = RoutePreset(
            id = "custom_${System.currentTimeMillis()}",
            name = "Điểm Đến ➔ $destName",
            destination = destName,
            totalDistanceKm = 8.5f,
            estimatedTimeMinutes = 20,
            steps = listOf(
                NavStep("Bắt đầu di chuyển tới $destName", "Tuyến đường Google Maps", 450, TurnType.STRAIGHT, "Làn hỗn hợp xe máy"),
                NavStep("Rẽ theo chỉ dẫn đường tới $destName", destName, 2100, TurnType.SLIGHT_RIGHT, "Làn xe máy"),
                NavStep("Tiếp tục đi thẳng 3.5 km", "Đại lộ chính", 3500, TurnType.STRAIGHT, "Tốc độ 50 km/h"),
                NavStep("Đến đích: $destName", destName, 0, TurnType.DESTINATION, "Đã tới điểm đến an toàn")
            )
        )
        _currentRoute.value = customRoute
        _currentStepIndex.value = 0
        _stepRemainingDistance.value = customRoute.steps.first().distanceMeters
    }

    // Reset Trip
    fun resetTrip() {
        leanAngleSensor.resetMaxLean()
        _telemetry.update {
            it.copy(
                tripDistanceKm = 0f,
                rideDurationSeconds = 0,
                maxSpeedKmh = 0f,
                averageSpeedKmh = 0f
            )
        }
    }

    // Messages and Notifications
    fun triggerSampleMessage(
        sender: String = "Mẹ",
        appSource: String = "Zalo",
        content: String = "Con chạy xe cẩn thận nhé, về ăn cơm sớm!"
    ) {
        val notification = MessageNotification(
            id = System.currentTimeMillis().toString(),
            sender = sender,
            appSource = appSource,
            content = content
        )
        _activeNotification.value = notification
        _notificationHistory.update { listOf(notification) + it }

        messageDismissJob?.cancel()
        messageDismissJob = viewModelScope.launch {
            delay(7000)
            if (_activeNotification.value?.id == notification.id) {
                _activeNotification.value = null
            }
        }
    }

    fun dismissActiveNotification() {
        messageDismissJob?.cancel()
        _activeNotification.value = null
    }

    fun sendQuickReply(replyText: String = "Đang lái xe máy, tôi sẽ gọi lại sau!") {
        _activeNotification.update {
            it?.copy(content = "✓ Đã tự động trả lời: \"$replyText\"")
        }
        viewModelScope.launch {
            delay(2500)
            _activeNotification.value = null
        }
    }

    fun toggleAutoReply() {
        _autoReplyEnabled.update { !it }
    }

    fun toggleSpeedWarning() {
        _speedWarningEnabled.update { !it }
    }

    // Emergency Contact & SOS functionality
    private val _emergencyContact = MutableStateFlow(EmergencyContact())
    val emergencyContact: StateFlow<EmergencyContact> = _emergencyContact.asStateFlow()

    fun updateEmergencyContact(name: String, phone: String, messageTemplate: String) {
        _emergencyContact.value = EmergencyContact(
            name = name.ifBlank { "Người thân" },
            phoneNumber = phone,
            customMessageTemplate = messageTemplate.ifBlank {
                "CẦN TRỢ GIÚP KHẨN CẤP! Tôi đang gặp sự cố khi chạy xe máy. Vị trí GPS của tôi: {maps_url}"
            }
        )
    }

    fun triggerEmergencySos(context: android.content.Context, onResult: (Boolean, String) -> Unit) {
        val coords = locationSpeedService.getCurrentCoordinates()
        val contact = _emergencyContact.value
        val speedKmh = _telemetry.value.speedKmh

        val message = EmergencySosHelper.buildSosMessage(
            contact = contact,
            latitude = coords.first,
            longitude = coords.second,
            speedKmh = speedKmh
        )

        HapticHelper.vibrateSpeedWarning(context)

        EmergencySosHelper.sendEmergencySms(
            context = context,
            phoneNumber = contact.phoneNumber,
            message = message
        ) { success, msg ->
            if (success) {
                triggerSampleMessage(
                    sender = "HỆ THỐNG SOS KHẨN CẤP",
                    appSource = "CỨU HỘ",
                    content = "Đã phát tọa độ GPS (${coords.first}, ${coords.second}) tới ${contact.name} (${contact.phoneNumber})!"
                )
            }
            onResult(success, msg)
        }
    }

    // Bluetooth Audio delegates
    fun togglePlayPause() = bluetoothController.togglePlayPause()
    fun nextTrack() = bluetoothController.nextTrack()
    fun previousTrack() = bluetoothController.previousTrack()
    fun adjustVolume(increase: Boolean) = bluetoothController.adjustVolume(increase)
    fun cycleHelmetDevice() = bluetoothController.cycleHelmetDevice()
    fun toggleBluetoothConnection() = bluetoothController.toggleBluetoothConnection()

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        telemetryTimerJob?.cancel()
        messageDismissJob?.cancel()
        locationSpeedService.stopGpsUpdates()
        leanAngleSensor.stopListening()
        obdService.disconnect()
    }
}
