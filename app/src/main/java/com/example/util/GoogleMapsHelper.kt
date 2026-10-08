package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object GoogleMapsHelper {

    /**
     * Opens Google Maps Turn-by-Turn Navigation in Two-Wheeler / Motorcycle mode.
     * Mode "b" = bicycle/two-wheeler/motorcycle, "d" = driving.
     */
    fun startGoogleMapsNavigation(context: Context, destination: String, isTwoWheeler: Boolean = true) {
        val mode = if (isTwoWheeler) "b" else "d"
        val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}&mode=$mode")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to web browser or generic geo intent
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}&travelmode=${if (isTwoWheeler) "bicycling" else "driving"}")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        } catch (_: Exception) {
            try {
                val genericGeo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(destination)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(genericGeo)
            } catch (e: Exception) {
                Toast.makeText(context, "Không thể mở Google Maps: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps pinned at coordinates.
     */
    fun openLocationOnGoogleMaps(context: Context, lat: Double, lng: Double, label: String = "Điểm đến") {
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Không thể mở Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}
