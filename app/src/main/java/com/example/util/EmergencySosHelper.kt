package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.model.EmergencyContact
import java.util.Locale

object EmergencySosHelper {

    /**
     * Builds emergency SOS message with live GPS coordinates, Google Maps URL, speed and timestamp.
     */
    fun buildSosMessage(
        contact: EmergencyContact,
        latitude: Double,
        longitude: Double,
        speedKmh: Float
    ): String {
        val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
        val formattedLat = String.format(Locale.US, "%.5f", latitude)
        val formattedLng = String.format(Locale.US, "%.5f", longitude)
        val speedStr = "${speedKmh.toInt()} km/h"

        return if (contact.customMessageTemplate.contains("{maps_url}")) {
            contact.customMessageTemplate
                .replace("{maps_url}", mapsUrl)
                .replace("{lat}", formattedLat)
                .replace("{lng}", formattedLng)
                .replace("{speed}", speedStr)
        } else {
            "${contact.customMessageTemplate}\nVị trí của tôi: $mapsUrl (Tọa độ: $formattedLat, $formattedLng - Vận tốc: $speedStr)"
        }
    }

    /**
     * Dispatches emergency SMS to the configured contact.
     * Uses direct SmsManager if permission granted, or opens SMS messenger with prefilled body.
     */
    fun sendEmergencySms(
        context: Context,
        phoneNumber: String,
        message: String,
        onSuccess: (Boolean, String) -> Unit
    ) {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "Vui lòng cài đặt số điện thoại khẩn cấp!", Toast.LENGTH_SHORT).show()
            onSuccess(false, "Chưa cài đặt số điện thoại")
            return
        }

        val hasSendSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSendSmsPermission) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanNumber, null, message, null, null)
                }
                Toast.makeText(context, "Đã gửi SMS khẩn cấp tới $cleanNumber", Toast.LENGTH_LONG).show()
                onSuccess(true, "Đã gửi trực tiếp qua SMS")
                return
            } catch (e: Exception) {
                // If direct sending fails, fallback to ACTION_SENDTO intent
            }
        }

        // Standard ACTION_SENDTO fallback intent
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            onSuccess(true, "Đã mở tin nhắn khẩn cấp")
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi gửi SMS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            onSuccess(false, "Lỗi: ${e.localizedMessage}")
        }
    }
}
