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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudColorTheme
import com.example.model.RoutePreset
import com.example.util.GoogleMapsHelper
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GoogleMapHudView(
    route: RoutePreset,
    compassHeading: Float,
    speedKmh: Float,
    colorTheme: HudColorTheme,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Pulse animation for bike position beam
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .testTag("google_map_hud_container")
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF060A14))
            .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Top Header: Google Maps Logo Badge + Destination Info + Search Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A73E8).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFF1A73E8), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Google Maps",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "GOOGLE MAPS • HUD TỐI",
                        color = Color(0xFF64B5F6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = route.destination,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            // Search destination shortcut
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .testTag("map_search_dest_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm điểm đến",
                    tint = colorTheme.accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Vector Night Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w * 0.45f
                val cy = h * 0.58f

                // Stylized dark city grid roads
                val roadGridColor = Color(0xFF111827)
                for (i in -4..5) {
                    val yLine = (cy + i * 42.dp.toPx() * zoomLevel).toFloat()
                    if (yLine in 0f..h) {
                        drawLine(
                            color = roadGridColor,
                            start = Offset(0f, yLine),
                            end = Offset(w, yLine),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                for (j in -4..6) {
                    val xLine = (cx + j * 54.dp.toPx() * zoomLevel).toFloat()
                    if (xLine in 0f..w) {
                        drawLine(
                            color = roadGridColor,
                            start = Offset(xLine, 0f),
                            end = Offset(xLine, h),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Curving River / Highway vector
                val riverPath = Path().apply {
                    moveTo(0f, h * 0.2f)
                    cubicTo(
                        w * 0.3f, h * 0.1f,
                        w * 0.6f, h * 0.5f,
                        w, h * 0.35f
                    )
                }
                drawPath(
                    path = riverPath,
                    color = Color(0xFF0D2538),
                    style = Stroke(width = 12.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
                )

                // Main Arterial Road (Highway)
                val highwayPath = Path().apply {
                    moveTo(0f, h * 0.85f)
                    cubicTo(
                        w * 0.35f, h * 0.75f,
                        w * 0.55f, h * 0.3f,
                        w, h * 0.15f
                    )
                }
                drawPath(
                    path = highwayPath,
                    color = Color(0xFF1E293B),
                    style = Stroke(width = 6.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
                )

                // ACTIVE MOTORCYCLE ROUTE POLYLINE (Glowing HUD Track)
                val routePath = Path().apply {
                    moveTo(cx, cy) // Bike position
                    lineTo(cx + 35.dp.toPx() * zoomLevel, cy - 25.dp.toPx() * zoomLevel)
                    lineTo(cx + 80.dp.toPx() * zoomLevel, cy - 40.dp.toPx() * zoomLevel)
                    lineTo(cx + 120.dp.toPx() * zoomLevel, cy - 90.dp.toPx() * zoomLevel)
                    lineTo(cx + 150.dp.toPx() * zoomLevel, cy - 120.dp.toPx() * zoomLevel)
                }

                // Route glow shadow
                drawPath(
                    path = routePath,
                    color = colorTheme.primaryColor.copy(alpha = 0.3f),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Route main line
                drawPath(
                    path = routePath,
                    color = colorTheme.primaryColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Destination Pin Marker
                val destX = cx + 150.dp.toPx() * zoomLevel
                val destY = cy - 120.dp.toPx() * zoomLevel
                drawCircle(
                    color = Color(0xFFFF1744),
                    radius = 7.dp.toPx(),
                    center = Offset(destX, destY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(destX, destY)
                )

                // Live Motorcycle Position Marker with Pulsing Radar
                drawCircle(
                    color = colorTheme.accentColor.copy(alpha = pulseAlpha),
                    radius = pulseRadius.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = colorTheme.primaryColor,
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(cx, cy)
                )

                // Heading beam wedge
                val beamRad = Math.toRadians((compassHeading - 90).toDouble())
                val beamLength = 28.dp.toPx()
                val beamEnd = Offset(
                    (cx + beamLength * cos(beamRad)).toFloat(),
                    (cy + beamLength * sin(beamRad)).toFloat()
                )
                drawLine(
                    color = colorTheme.accentColor,
                    start = Offset(cx, cy),
                    end = beamEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Map Overlay Controls (Zoom + / -, Recenter)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.0f) },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Phóng to", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.6f) },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Thu nhỏ", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Bottom-left Map metrics: Distance & ETA
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF000000).copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${route.totalDistanceKm} km • ~${route.estimatedTimeMinutes}p",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Direct Launch Button: Opens official Google Maps Navigation in Motorcycle / Two-Wheeler mode
        ElevatedButton(
            onClick = {
                GoogleMapsHelper.startGoogleMapsNavigation(
                    context = context,
                    destination = route.destination,
                    isTwoWheeler = true
                )
            },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color(0xFF1A73E8),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("launch_google_maps_nav_button")
        ) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = "Chỉ đường Google Maps",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "MỞ GOOGLE MAPS XE MÁY",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
