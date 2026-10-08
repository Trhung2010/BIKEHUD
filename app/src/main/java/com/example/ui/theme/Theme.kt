package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.HudColorTheme

private val DarkColorScheme = darkColorScheme(
    primary = HudNeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003840),
    onPrimaryContainer = HudNeonCyan,
    secondary = HudNeonGreen,
    onSecondary = Color.Black,
    tertiary = HudNeonAmber,
    background = HudPureBlack,
    onBackground = HudTextPrimary,
    surface = HudSurfaceDark,
    onSurface = HudTextPrimary,
    surfaceVariant = HudCardDark,
    onSurfaceVariant = HudTextSecondary,
    outline = HudBorderDark,
    error = HudNeonRed,
    onError = Color.Black
)

@Composable
fun MotoHudTheme(
    activeHudTheme: HudColorTheme = HudColorTheme.NEON_CYAN,
    content: @Composable () -> Unit
) {
    val dynamicScheme = DarkColorScheme.copy(
        primary = activeHudTheme.primaryColor,
        secondary = activeHudTheme.secondaryColor,
        tertiary = activeHudTheme.accentColor,
        error = activeHudTheme.warningColor,
        background = HudPureBlack,
        surface = HudSurfaceDark
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}
