package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import java.io.File

class MotoHudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Pre-create Chromium cache directories so simple_file_enumerator readdir finds them
            val baseCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            File(baseCache, "wasm").mkdirs()
            File(baseCache, "js").mkdirs()
            File(cacheDir, "WebView/Default/Code Cache/wasm").mkdirs()
            File(cacheDir, "WebView/Default/Code Cache/js").mkdirs()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName()
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            }
        } catch (_: Exception) {}
    }
}
