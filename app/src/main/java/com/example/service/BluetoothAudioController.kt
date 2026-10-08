package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import com.example.model.BluetoothMusicState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BluetoothAudioController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val sampleTracks = listOf(
        Pair("Waiting For You", "MONO"),
        Pair("Nơi Này Có Anh", "Sơn Tùng M-TP"),
        Pair("See Tình", "Hoàng Thùy Linh"),
        Pair("Cắt Đôi Nỗi Sầu", "Tăng Duy Tân"),
        Pair("Đi Để Trở Về", "Soobin Hoàng Sơn"),
        Pair("Riding On The Highway", "Moto Chill Beats")
    )

    private val helmetDevices = listOf(
        Pair("Cardo Packtalk Edge (Helmet BT)", 88),
        Pair("Sena 50S Mesh Intercom", 92),
        Pair("AirPods Pro 2", 75),
        Pair("Sony WH-1000XM5", 80),
        Pair("Bose Frames Moto Visor", 65)
    )

    private var currentTrackIndex = 1
    private var currentDeviceIndex = 0

    private val _musicState = MutableStateFlow(
        BluetoothMusicState(
            isConnected = true,
            deviceName = helmetDevices[0].first,
            batteryPercent = helmetDevices[0].second,
            isPlaying = true,
            trackTitle = sampleTracks[1].first,
            artist = sampleTracks[1].second,
            volumeLevel = getCurrentSystemVolume()
        )
    )
    val musicState: StateFlow<BluetoothMusicState> = _musicState.asStateFlow()

    private fun getCurrentSystemVolume(): Float {
        return try {
            audioManager?.let { am ->
                val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (max > 0) current.toFloat() / max.toFloat() else 0.7f
            } ?: 0.7f
        } catch (_: Exception) {
            0.7f
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        try {
            audioManager?.let { am ->
                val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
                val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
                am.dispatchMediaKeyEvent(downEvent)
                am.dispatchMediaKeyEvent(upEvent)
            }
        } catch (_: Exception) {
            // fallback
        }
    }

    fun togglePlayPause() {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        _musicState.update { current ->
            current.copy(isPlaying = !current.isPlaying)
        }
    }

    fun nextTrack() {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        currentTrackIndex = (currentTrackIndex + 1) % sampleTracks.size
        val next = sampleTracks[currentTrackIndex]
        _musicState.update {
            it.copy(
                trackTitle = next.first,
                artist = next.second,
                isPlaying = true,
                progressMs = 0
            )
        }
    }

    fun previousTrack() {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        currentTrackIndex = if (currentTrackIndex - 1 < 0) sampleTracks.size - 1 else currentTrackIndex - 1
        val prev = sampleTracks[currentTrackIndex]
        _musicState.update {
            it.copy(
                trackTitle = prev.first,
                artist = prev.second,
                isPlaying = true,
                progressMs = 0
            )
        }
    }

    fun adjustVolume(increase: Boolean) {
        try {
            audioManager?.let { am ->
                val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
                val updatedVol = getCurrentSystemVolume()
                _musicState.update { it.copy(volumeLevel = updatedVol) }
            }
        } catch (_: Exception) {
            _musicState.update {
                val newVol = if (increase) (it.volumeLevel + 0.1f).coerceAtMost(1f)
                else (it.volumeLevel - 0.1f).coerceAtLeast(0f)
                it.copy(volumeLevel = newVol)
            }
        }
    }

    fun toggleBluetoothConnection() {
        _musicState.update {
            val willBeConnected = !it.isConnected
            it.copy(isConnected = willBeConnected)
        }
    }

    fun cycleHelmetDevice() {
        currentDeviceIndex = (currentDeviceIndex + 1) % helmetDevices.size
        val dev = helmetDevices[currentDeviceIndex]
        _musicState.update {
            it.copy(
                deviceName = dev.first,
                batteryPercent = dev.second,
                isConnected = true
            )
        }
    }

    fun updateProgress(deltaSeconds: Int) {
        _musicState.update { state ->
            if (!state.isPlaying) return@update state
            val nextProg = (state.progressMs + deltaSeconds * 1000L)
            if (nextProg >= state.durationMs) {
                // Loop to next track
                currentTrackIndex = (currentTrackIndex + 1) % sampleTracks.size
                val next = sampleTracks[currentTrackIndex]
                state.copy(
                    trackTitle = next.first,
                    artist = next.second,
                    progressMs = 0
                )
            } else {
                state.copy(progressMs = nextProg)
            }
        }
    }
}
