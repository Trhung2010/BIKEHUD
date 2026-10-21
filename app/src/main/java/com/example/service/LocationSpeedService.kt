package com.example.service

import android.content.Context

/**
 * Backward-compatible subclass of [LocationService] preserving existing call sites.
 * For all new features, prefer injecting and using [LocationService] directly.
 */
class LocationSpeedService(context: Context) : LocationService(context)
