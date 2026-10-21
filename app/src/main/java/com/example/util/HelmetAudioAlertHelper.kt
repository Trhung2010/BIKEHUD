package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Helmet audio and voice co-pilot alerts for motorcyclists.
 * Sends high-priority audio beeps and voice synthesis to Bluetooth helmet headsets (Cardo/Sena/AirPods)
 * and phone speakers.
 */
class HelmetAudioAlertHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (_: Exception) {
            toneGenerator = null
        }

        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech?.setLanguage(Locale("vi", "VN"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.language = Locale.ENGLISH
                    }
                    isTtsReady = true
                }
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    fun playOverSpeedBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 350)
        } catch (_: Exception) {}
    }

    fun playLaunchBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
        } catch (_: Exception) {}
    }

    fun playFinishChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 400)
        } catch (_: Exception) {}
    }

    fun speak(text: String) {
        if (!isTtsReady || textToSpeech == null) return
        mainHandler.post {
            try {
                textToSpeech?.speak(text, TextToSpeech.QUEUE_ADD, null, "MOTO_HUD_VOICE")
            } catch (_: Exception) {}
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isTtsReady = false
        } catch (_: Exception) {}
    }
}
