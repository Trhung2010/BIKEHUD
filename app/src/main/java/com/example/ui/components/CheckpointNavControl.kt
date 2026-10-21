package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.AssistantPhoto
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudColorTheme
import com.example.model.NavStep
import com.example.model.TurnType

/**
 * Clean, glove-friendly HUD Checkpoint Navigation Controller.
 * Kept completely outside the Google Map so map viewport remains 100% clean and unobstructed.
 */
@Composable
fun CheckpointNavControl(
    currentStep: NavStep,
    stepIndex: Int,
    totalSteps: Int,
    remainingMeters: Int,
    colorTheme: HudColorTheme,
    onNextCheckpoint: () -> Unit,
    onPrevCheckpoint: () -> Unit,
    onSelectCheckpoint: ((Int) -> Unit)? = null,
    allSteps: List<NavStep> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showAllCheckpoints by remember { mutableStateOf(false) }

    val formattedDistance = if (remainingMeters >= 1000) {
        String.format(java.util.Locale.US, "%.1f km", remainingMeters / 1000f)
    } else {
        "$remainingMeters m"
    }

    val isLastCheckpoint = stepIndex >= totalSteps - 1
    val progress = ((stepIndex + 1).toFloat() / totalSteps.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090E1A))
            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .testTag("checkpoint_nav_control")
            .padding(10.dp)
    ) {
        // Top Header: Checkpoint index + Remaining meters countdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorTheme.primaryColor.copy(alpha = 0.15f))
                    .clickable { showAllCheckpoints = !showAllCheckpoints }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("checkpoint_badge_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "CHECKPOINT ${stepIndex + 1}/$totalSteps",
                    color = colorTheme.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Distance Countdown Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, colorTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = formattedDistance,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Turn Instruction Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Turn Direction Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorTheme.primaryColor.copy(alpha = 0.18f))
                    .border(1.2.dp, colorTheme.primaryColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNavTurnIcon(currentStep.turnType),
                    contentDescription = currentStep.instruction,
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentStep.instruction,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${currentStep.streetName} • ${currentStep.laneInfo}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Checkpoint Actions: "Lùi Checkpoint" & "Tiếp tục Checkpoint"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Checkpoint Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (stepIndex > 0) Color(0xFF1E293B) else Color(0xFF111827))
                    .border(
                        1.dp,
                        if (stepIndex > 0) Color(0xFF475569) else Color(0xFF1F2937),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = stepIndex > 0) { onPrevCheckpoint() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("prev_checkpoint_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = if (stepIndex > 0) Color(0xFFCBD5E1) else Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "LÙI",
                        color = if (stepIndex > 0) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Continue to Next Checkpoint Button (Prominent Glove-Friendly Button)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isLastCheckpoint) Color(0xFF059669)
                        else colorTheme.primaryColor
                    )
                    .clickable { onNextCheckpoint() }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .testTag("continue_checkpoint_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLastCheckpoint) "ĐÃ TỚI ĐÍCH (KHỞI ĐỘNG LẠI)" else "TIẾP TỤC CHECKPOINT",
                        color = if (isLastCheckpoint) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isLastCheckpoint) Color.White else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar for entire route checkpoints
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colorTheme.primaryColor,
            trackColor = Color(0xFF1E293B)
        )

        // Expandable Quick Checkpoint List
        AnimatedVisibility(visible = showAllCheckpoints && allSteps.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "DANH SÁCH CHECKPOINT CỦA LỘ TRÌNH:",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                allSteps.forEachIndexed { idx, step ->
                    val isCurrent = idx == stepIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isCurrent) colorTheme.primaryColor.copy(alpha = 0.2f)
                                else Color(0xFF0F172A)
                            )
                            .clickable {
                                onSelectCheckpoint?.invoke(idx)
                                showAllCheckpoints = false
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "#${idx + 1}",
                                color = if (isCurrent) colorTheme.primaryColor else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.instruction,
                                color = if (isCurrent) Color.White else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${step.distanceMeters} m",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun getNavTurnIcon(type: TurnType): ImageVector {
    return when (type) {
        TurnType.STRAIGHT -> Icons.Default.Navigation
        TurnType.SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
        TurnType.TURN_RIGHT -> Icons.Default.TurnRight
        TurnType.SHARP_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        TurnType.SLIGHT_LEFT -> Icons.Default.TurnSlightLeft
        TurnType.TURN_LEFT -> Icons.Default.TurnLeft
        TurnType.SHARP_LEFT -> Icons.AutoMirrored.Filled.TrendingFlat
        TurnType.U_TURN -> Icons.Default.RotateLeft
        TurnType.ROUNDABOUT -> Icons.Default.NearMe
        TurnType.DESTINATION -> Icons.Default.AssistantPhoto
    }
}
