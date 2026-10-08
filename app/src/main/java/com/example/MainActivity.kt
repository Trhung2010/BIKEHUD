package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HudMainScreen
import com.example.ui.theme.MotoHudTheme
import com.example.viewmodel.HudViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HudViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen on continuously for motorcycle HUD projection onto windshield
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()

            // Request GPS Location & Bluetooth permissions for real speedometer and helmet intercom
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                if (fineLocationGranted || coarseLocationGranted) {
                    // Location granted, user can switch between Live GPS and Simulation mode
                }
            }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                    permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                }

                val needed = permissionsToRequest.filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (needed.isNotEmpty()) {
                    permissionLauncher.launch(needed.toTypedArray())
                }
            }

            MotoHudTheme(activeHudTheme = colorTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    HudMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
