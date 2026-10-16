package com.example.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * MotoNotificationListenerService allows MotoHUD to:
 * 1. Discover active Android MediaSessions (Spotify, Apple Music, YouTube Music, etc.)
 *    and provide MediaController instances to control playback from the HUD.
 * 2. Forward incoming ride-safety notifications (SMS, Zalo, Messaging) to the HUD display.
 */
class MotoNotificationListenerService : NotificationListenerService() {

    companion object {
        var onMediaControllerFound: ((MediaController) -> Unit)? = null
        var onIncomingMessageNotification: ((sender: String, app: String, text: String) -> Unit)? = null

        @Volatile
        var isNotificationServiceConnected: Boolean = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isNotificationServiceConnected = true
        discoverActiveMediaSessions()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isNotificationServiceConnected = false
    }

    /**
     * Inspects active system media sessions using MediaSessionManager and binds the active controller.
     */
    fun discoverActiveMediaSessions() {
        try {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            val component = ComponentName(this, MotoNotificationListenerService::class.java)
            val controllers = mediaSessionManager.getActiveSessions(component)
            if (controllers.isNotEmpty()) {
                val primary = controllers.firstOrNull {
                    it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
                } ?: controllers.first()
                onMediaControllerFound?.invoke(primary)
            }

            mediaSessionManager.addOnActiveSessionsChangedListener({ activeControllers ->
                if (!activeControllers.isNullOrEmpty()) {
                    val active = activeControllers.firstOrNull {
                        it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
                    } ?: activeControllers.first()
                    onMediaControllerFound?.invoke(active)
                }
            }, component)
        } catch (e: Exception) {
            Log.d("MotoHUD", "MediaSessionManager query notice: ${e.message}")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val pkg = sbn.packageName ?: ""

        val appSource = when {
            pkg.contains("zalo") -> "Zalo"
            pkg.contains("messaging") || pkg.contains("sms") || pkg.contains("mms") -> "SMS"
            pkg.contains("whatsapp") -> "WhatsApp"
            pkg.contains("facebook.orca") || pkg.contains("messenger") -> "Messenger"
            pkg.contains("telegram") -> "Telegram"
            pkg.contains("viber") -> "Viber"
            else -> "Thông Báo"
        }

        if (title.isNotBlank() && text.isNotBlank() && !pkg.contains("android.dialer") && !pkg.contains("systemui")) {
            onIncomingMessageNotification?.invoke(title, appSource, text)
        }
    }
}
