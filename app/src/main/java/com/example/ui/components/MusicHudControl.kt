package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothMusicState
import com.example.model.HudColorTheme

@Composable
fun MusicHudControl(
    musicState: BluetoothMusicState,
    colorTheme: HudColorTheme,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onAdjustVolume: (Boolean) -> Unit,
    onCycleDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Equalizer bar animations
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val wave4 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )

    Column(
        modifier = modifier
            .testTag("music_hud_controller")
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF080D1A).copy(alpha = 0.9f))
            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Bluetooth Device Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clickable to cycle helmet intercom devices
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onCycleDevice() }
                    .padding(4.dp)
                    .testTag("bluetooth_device_tag")
            ) {
                Icon(
                    imageVector = if (musicState.isConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                    contentDescription = "Bluetooth Status",
                    tint = if (musicState.isConnected) colorTheme.primaryColor else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (musicState.isConnected) musicState.deviceName else "Chưa kết nối Bluetooth",
                    color = if (musicState.isConnected) colorTheme.primaryColor else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            // Battery and Volume indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (musicState.isConnected) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Pin tai nghe",
                        tint = colorTheme.secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${musicState.batteryPercent}%",
                        color = colorTheme.secondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Track title & Artist with live Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = musicState.trackTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = musicState.artist,
                    color = colorTheme.accentColor.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            // Visualizer bars
            if (musicState.isPlaying) {
                Canvas(
                    modifier = Modifier
                        .size(width = 32.dp, height = 18.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    val barWidth = 4.dp.toPx()
                    val spacing = 3.dp.toPx()
                    val h = size.height

                    val heights = listOf(wave1, wave2, wave3, wave4)
                    for (i in heights.indices) {
                        val barH = h * heights[i]
                        val x = i * (barWidth + spacing)
                        val y = h - barH
                        drawRoundRect(
                            color = colorTheme.primaryColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Large Rider-Friendly Media Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume Down
            IconButton(
                onClick = { onAdjustVolume(false) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .testTag("music_vol_down")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = "Giảm âm lượng",
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Previous
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .testTag("music_prev")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Bài trước",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Play / Pause (Big Primary Button)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colorTheme.primaryColor)
                    .clickable { onTogglePlayPause() }
                    .testTag("music_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (musicState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (musicState.isPlaying) "Tạm dừng" else "Phát nhạc",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Next
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .testTag("music_next")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Bài tiếp theo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Volume Up
            IconButton(
                onClick = { onAdjustVolume(true) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .testTag("music_vol_up")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Tăng âm lượng",
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
