package com.example.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.SystemClock
import com.example.model.BluetoothDeviceInfo
import com.example.model.ObdConnectionState
import com.example.model.ObdTelemetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Service managing real Bluetooth connection and ELM327 OBD-II diagnostic protocol.
 * Connects directly to OBD-II dongles via Bluetooth SPP (Serial Port Profile).
 */
class ObdBluetoothService(private val context: Context) {

    companion object {
        // Standard Bluetooth Serial Port Profile (SPP) UUID used by all ELM327 and OBD adapters
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private val _obdTelemetry = MutableStateFlow(ObdTelemetry())
    val obdTelemetry: StateFlow<ObdTelemetry> = _obdTelemetry.asStateFlow()

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var communicationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    /**
     * Lists paired Bluetooth devices on the Android device, flagging OBD adapters.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        val adapter = bluetoothAdapter ?: return emptyList()
        return try {
            adapter.bondedDevices.map { device ->
                val name = device.name ?: "Thiết bị không tên"
                val address = device.address ?: ""
                val isObd = isLikelyObdDongle(name)
                BluetoothDeviceInfo(name = name, address = address, isObdLikely = isObd)
            }.sortedByDescending { it.isObdLikely }
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun isLikelyObdDongle(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("obd") || lower.contains("elm") || lower.contains("vlinker") ||
                lower.contains("viecar") || lower.contains("konnwei") || lower.contains("ecu") ||
                lower.contains("car") || lower.contains("auto") || lower.contains("scanner")
    }

    /**
     * Connects to a specific Bluetooth OBD-II device and starts querying real ECU PIDs.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String, deviceName: String = "OBD-II Adapter") {
        disconnect()

        _obdTelemetry.update {
            it.copy(
                connectionState = ObdConnectionState.CONNECTING,
                connectedDeviceName = deviceName,
                connectedDeviceAddress = deviceAddress,
                errorMessage = ""
            )
        }

        communicationJob = scope.launch {
            try {
                val adapter = bluetoothAdapter ?: throw IllegalStateException("Bluetooth không khả dụng")
                val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)

                // Cancel discovery prior to connecting as discovery drastically slows connection
                try {
                    adapter.cancelDiscovery()
                } catch (_: Exception) {}

                val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket = btSocket
                btSocket.connect()

                inputStream = btSocket.inputStream
                outputStream = btSocket.outputStream

                _obdTelemetry.update {
                    it.copy(connectionState = ObdConnectionState.INITIALIZING)
                }

                // Perform ELM327 initialization protocol
                initializeElm327()

                _obdTelemetry.update {
                    it.copy(connectionState = ObdConnectionState.CONNECTED)
                }

                // Continuous real ECU PID polling loop
                pollEcuLoop()

            } catch (e: CancellationException) {
                // Cancelled cleanly
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Không thể kết nối với thiết bị OBD"
                _obdTelemetry.update {
                    it.copy(
                        connectionState = ObdConnectionState.ERROR,
                        errorMessage = errorMsg
                    )
                }
                closeStreams()
            }
        }
    }

    /**
     * Sends standard AT initialization commands to the ELM327 chip.
     */
    private suspend fun initializeElm327() {
        sendCommand("ATZ") // Reset ELM327
        delay(800)
        sendCommand("ATE0") // Echo off
        delay(200)
        sendCommand("ATL0") // Linefeeds off
        delay(150)
        sendCommand("ATH0") // Headers off
        delay(150)
        sendCommand("ATSP0") // Auto detect vehicle protocol
        delay(300)
        val voltageRaw = sendCommand("ATRV") // Read real battery voltage
        val cleanVoltage = parseVoltage(voltageRaw)
        if (cleanVoltage != null) {
            _obdTelemetry.update { it.copy(batteryVoltage = cleanVoltage, voltageUpdatedAtMs = SystemClock.elapsedRealtime()) }
        }
    }

    /**
     * Loops through real OBD-II Mode 01 PIDs: Speed, RPM, Coolant Temp, Throttle, Voltage.
     */
    private suspend fun pollEcuLoop() {
        var tickCounter = 0
        while (currentCoroutineContext().isActive) {
            // Publish each valid PID immediately. Invalid replies do not renew its freshness.
            parseSpeedPid(sendCommand("010D"))?.let { value ->
                _obdTelemetry.update { it.copy(speedKmh = value, speedUpdatedAtMs = SystemClock.elapsedRealtime(), lastUpdateMs = System.currentTimeMillis()) }
            }
            parseRpmPid(sendCommand("010C"))?.let { value ->
                _obdTelemetry.update { it.copy(rpm = value, rpmUpdatedAtMs = SystemClock.elapsedRealtime()) }
            }
            tickCounter++
            if (tickCounter % 5 == 0) {
                parseCoolantTempPid(sendCommand("0105"))?.let { value ->
                    _obdTelemetry.update { it.copy(coolantTempC = value, coolantUpdatedAtMs = SystemClock.elapsedRealtime()) }
                }
                parseThrottlePid(sendCommand("0111"))?.let { value ->
                    _obdTelemetry.update { it.copy(throttlePercent = value, throttleUpdatedAtMs = SystemClock.elapsedRealtime()) }
                }
            }
            if (tickCounter % 15 == 0) {
                parseVoltage(sendCommand("ATRV"))?.let { value ->
                    _obdTelemetry.update { it.copy(batteryVoltage = value, voltageUpdatedAtMs = SystemClock.elapsedRealtime()) }
                }
            }
            delay(150)
        }
    }

    private fun parseVoltage(raw: String): String? =
        Regex("""^(\d{1,2}(?:\.\d+)?)\s*[Vv]$""").matchEntire(raw.trim())
            ?.groupValues?.get(1)?.toFloatOrNull()?.takeIf { it in 0f..30f }?.let { "${it}V" }

    /**
     * Sends an AT or OBD command string and waits for response terminating in '>'
     */
    private suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        val out = outputStream ?: return@withContext ""
        val inStream = inputStream ?: return@withContext ""

        try {
            out.write("$command\r".toByteArray())
            out.flush()

            val buffer = StringBuilder()
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 1500) { // 1.5s timeout per command
                if (inStream.available() > 0) {
                    val c = inStream.read().toChar()
                    if (c == '>') {
                        break
                    }
                    buffer.append(c)
                } else {
                    delay(15)
                }
            }
            currentCoroutineContext().ensureActive()
            buffer.toString().trim()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ""
        }
    }

    // --- OBD-II PID Parsers ---

    /**
     * PID 010D (Vehicle Speed): Response format "41 0D xx" -> Speed in km/h = xx (hex to int)
     */
    fun parseSpeedPid(raw: String): Int? {
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val index = clean.indexOf("410D")
        if (index != -1 && clean.length >= index + 6) {
            val hex = clean.substring(index + 4, index + 6)
            return hex.toIntOrNull(16)
        }
        return null
    }

    /**
     * PID 010C (Engine RPM): Response format "41 0C xx yy" -> RPM = ((xx * 256) + yy) / 4
     */
    fun parseRpmPid(raw: String): Int? {
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val index = clean.indexOf("410C")
        if (index != -1 && clean.length >= index + 8) {
            val aHex = clean.substring(index + 4, index + 6)
            val bHex = clean.substring(index + 6, index + 8)
            val a = aHex.toIntOrNull(16) ?: return null
            val b = bHex.toIntOrNull(16) ?: return null
            return ((a * 256) + b) / 4
        }
        return null
    }

    /**
     * PID 0105 (Coolant Temp): Response format "41 05 xx" -> Temp = xx - 40 (°C)
     */
    fun parseCoolantTempPid(raw: String): Int? {
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val index = clean.indexOf("4105")
        if (index != -1 && clean.length >= index + 6) {
            val hex = clean.substring(index + 4, index + 6)
            val a = hex.toIntOrNull(16) ?: return null
            return a - 40
        }
        return null
    }

    /**
     * PID 0111 (Throttle Position): Response format "41 11 xx" -> (xx * 100) / 255 (%)
     */
    fun parseThrottlePid(raw: String): Float? {
        val clean = raw.replace(" ", "").replace("\r", "").replace("\n", "").uppercase()
        val index = clean.indexOf("4111")
        if (index != -1 && clean.length >= index + 6) {
            val hex = clean.substring(index + 4, index + 6)
            val a = hex.toIntOrNull(16) ?: return null
            return (a * 100f / 255f)
        }
        return null
    }

    fun disconnect() {
        communicationJob?.cancel()
        communicationJob = null
        closeStreams()
        _obdTelemetry.value = ObdTelemetry()
    }

    private fun closeStreams() {
        try { inputStream?.close() } catch (_: Exception) {}
        try { outputStream?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        inputStream = null
        outputStream = null
        socket = null
    }
}
