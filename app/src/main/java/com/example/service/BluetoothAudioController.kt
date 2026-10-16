package com.example.service

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.example.model.BluetoothMusicState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Controller managing motorcycle audio playback and Bluetooth helmet intercom integration.
 * Integrates the Android MediaController API (MediaController.TransportControls) for real-time
 * bidirectional control (play, pause, skip track) with active music players (Spotify, YouTube Music, Zing MP3).
 * Seamlessly falls back to AudioManager media key events when no direct session is attached.
 */
class BluetoothAudioController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var activeMediaController: MediaController? = null

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

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            state ?: return
            val isPlaying = state.state == PlaybackState.STATE_PLAYING ||
                    state.state == PlaybackState.STATE_FAST_FORWARDING ||
                    state.state == PlaybackState.STATE_SKIPPING_TO_NEXT
            val pos = state.position
            _musicState.update {
                it.copy(
                    isPlaying = isPlaying,
                    progressMs = if (pos >= 0) pos else it.progressMs
                )
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            metadata ?: return
            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

            _musicState.update { current ->
                current.copy(
                    trackTitle = if (!title.isNullOrBlank()) title else current.trackTitle,
                    artist = if (!artist.isNullOrBlank()) artist else current.artist,
                    durationMs = if (duration > 0) duration else current.durationMs
                )
            }
        }

        override fun onSessionDestroyed() {
            detachMediaController()
        }
    }

    init {
        // Wire notification listener callback
        MotoNotificationListenerService.onMediaControllerFound = { controller ->
            mainHandler.post {
                attachMediaController(controller)
            }
        }

        // Attempt direct discovery if notification listener component is bound
        tryDiscoverActiveSessions()
    }

    /**
     * Tries to find existing active media controllers on the device.
     */
    fun tryDiscoverActiveSessions() {
        try {
            val component = ComponentName(context, MotoNotificationListenerService::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            if (!controllers.isNullOrEmpty()) {
                val primary = controllers.firstOrNull {
                    it.playbackState?.state == PlaybackState.STATE_PLAYING
                } ?: controllers.first()
                attachMediaController(primary)
            }
        } catch (_: SecurityException) {
            // Notification listener permission not granted yet by user; media key fallback is used
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    /**
     * Attaches an active Android MediaController, syncing metadata and playback status.
     */
    fun attachMediaController(controller: MediaController) {
        if (activeMediaController == controller) return

        try {
            activeMediaController?.unregisterCallback(mediaControllerCallback)
        } catch (_: Exception) {}

        activeMediaController = controller
        try {
            controller.registerCallback(mediaControllerCallback)

            val pb = controller.playbackState
            val isPlaying = pb?.state == PlaybackState.STATE_PLAYING
            val position = pb?.position ?: 0L

            val meta = controller.metadata
            val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            val duration = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 240000L

            _musicState.update {
                it.copy(
                    isMediaControllerActive = true,
                    mediaPackageName = controller.packageName ?: "",
                    isPlaying = isPlaying,
                    progressMs = if (position >= 0) position else it.progressMs,
                    trackTitle = if (!title.isNullOrBlank()) title else it.trackTitle,
                    artist = if (!artist.isNullOrBlank()) artist else it.artist,
                    durationMs = if (duration > 0) duration else it.durationMs
                )
            }
        } catch (_: Exception) {}
    }

    fun detachMediaController() {
        try {
            activeMediaController?.unregisterCallback(mediaControllerCallback)
        } catch (_: Exception) {}
        activeMediaController = null
        _musicState.update { it.copy(isMediaControllerActive = false, mediaPackageName = "") }
    }

    fun getActiveMediaController(): MediaController? = activeMediaController

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

    /**
     * Toggles playback using MediaController TransportControls if available, or media key event.
     */
    fun togglePlayPause() {
        val controller = activeMediaController
        if (controller != null) {
            try {
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
                return
            } catch (_: Exception) {
                // fallback to key event below
            }
        }

        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        _musicState.update { current ->
            current.copy(isPlaying = !current.isPlaying)
        }
    }

    /**
     * Skips to the next track using MediaController.transportControls.skipToNext() or media key event.
     */
    fun nextTrack() {
        val controller = activeMediaController
        if (controller != null) {
            try {
                controller.transportControls.skipToNext()
                return
            } catch (_: Exception) {
                // fallback to key event below
            }
        }

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

    /**
     * Skips to previous track using MediaController.transportControls.skipToPrevious() or media key event.
     */
    fun previousTrack() {
        val controller = activeMediaController
        if (controller != null) {
            try {
                controller.transportControls.skipToPrevious()
                return
            } catch (_: Exception) {
                // fallback to key event below
            }
        }

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
                if (state.isMediaControllerActive) {
                    state.copy(progressMs = 0)
                } else {
                    currentTrackIndex = (currentTrackIndex + 1) % sampleTracks.size
                    val next = sampleTracks[currentTrackIndex]
                    state.copy(
                        trackTitle = next.first,
                        artist = next.second,
                        progressMs = 0
                    )
                }
            } else {
                state.copy(progressMs = nextProg)
            }
        }
    }
}
