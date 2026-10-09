package com.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.RealHudDashboard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RealHudDashboardTest {
    @get:Rule val compose = createComposeRule()

    private fun render(state: RealHudState, unit: SpeedUnit = SpeedUnit.KMH,
                       onSelect: (TelemetrySource) -> Unit = {}) {
        compose.setContent {
            MaterialTheme {
                RealHudDashboard(state, unit, 50, 0, true, false,
                    onSelectMode = onSelect, onToggleUnit = {}, onToggleMirror = {},
                    onOpenLimit = {}, onOpenConnection = {}, modifier = Modifier.width(360.dp))
            }
        }
    }

    @Test fun gpsUsesConvertedMphAndDoesNotShowEngineRpm() {
        render(RealHudState(speedKmh = 50f, gps = GpsReading(speedKmh = 50f)), SpeedUnit.MPH)
        compose.onNodeWithContentDescription("Tốc độ: 31 mph").assertExists()
        compose.onNodeWithTag("gps_hud_tab").assertIsSelected()
        compose.onNodeWithTag("obd_rpm").assertDoesNotExist()
    }

    @Test fun unknownSpeedIsAnnouncedAsUnavailable() {
        render(RealHudState())
        compose.onNodeWithContentDescription("Tốc độ: chưa có dữ liệu").assertExists()
    }

    @Test fun obdTabDispatchesAnExplicitSource() {
        var selected: TelemetrySource? = null
        render(RealHudState(), onSelect = { selected = it })
        compose.onNodeWithTag("obd_hud_tab").performClick()
        org.junit.Assert.assertEquals(TelemetrySource.HUD_OBD, selected)
    }
}
