package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProximityEventEntity
import com.example.ui.TrackerViewModel
import com.example.ui.theme.*

@Composable
fun MapScreen(
    viewModel: TrackerViewModel
) {
    val events by viewModel.allEvents.collectAsState()
    val devices by viewModel.allDevices.collectAsState()
    val threats by viewModel.activeThreats.collectAsState()
    val selectedMac by viewModel.selectedDeviceForMap.collectAsState()

    val filteredEvents = remember(events, selectedMac) {
        if (selectedMac == null) events else events.filter { it.macAddress == selectedMac }
    }

    val selectedDeviceName = remember(devices, selectedMac) {
        devices.firstOrNull { it.macAddress == selectedMac }?.name ?: "All Scanned Routes"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Map Filter Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GPS PROXIMITY MAPPING",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SentinelTextMuted,
                        letterSpacing = 1.2.sp
                    )
                )
                Text(
                    text = if (selectedMac != null) "Focus: $selectedDeviceName" else "Showing all proximity encounters",
                    style = MaterialTheme.typography.labelSmall.copy(color = SentinelPurplePrimary)
                )
            }

            if (selectedMac != null) {
                TextButton(onClick = { viewModel.setSelectedDeviceForMap(null) }) {
                    Text("Clear Filter", color = SentinelTextMuted, fontSize = 12.sp)
                }
            }
        }

        // Threat Quick Select Pills
        if (threats.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = threats,
                    key = { it.macAddress }
                ) { threat ->
                    Surface(
                        color = if (selectedMac == threat.macAddress) SentinelAlertRedBorder else SentinelAlertCardBg,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable {
                                if (selectedMac == threat.macAddress) {
                                    viewModel.setSelectedDeviceForMap(null)
                                } else {
                                    viewModel.setSelectedDeviceForMap(threat.macAddress)
                                }
                            }
                            .testTag("map_filter_pill_${threat.macAddress}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SentinelAlertText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = threat.name ?: "Unknown BLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Tactical Map Canvas Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SentinelCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("gps_map_canvas_card")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                TacticalGpsMapCanvas(events = filteredEvents)

                // Map Legend Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SentinelSuccessGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("User GPS", color = Color.White, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SentinelAlertRedBorder))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Encounter Pin", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Timeline Summary Footer
        if (filteredEvents.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SentinelCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL PLOTTED POINTS: ${filteredEvents.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SentinelTextPrimary
                            )
                        )
                        Text(
                            text = "Filtered points on interactive grid",
                            style = MaterialTheme.typography.labelSmall.copy(color = SentinelTextMuted)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = SentinelPurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TacticalGpsMapCanvas(events: List<ProximityEventEntity>) {
    val gridLineColor = SentinelPurplePrimary.copy(alpha = 0.15f)
    val pathColor = SentinelPurplePrimary.copy(alpha = 0.6f)
    val pointColor = SentinelAlertRedBorder
    val userColor = SentinelSuccessGreen

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw tactical grid lines
        val step = 60.dp.toPx()
        var x = 0f
        while (x < width) {
            drawLine(
                color = gridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < height) {
            drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }

        if (events.isEmpty()) return@Canvas

        // Normalize coordinates onto canvas bounds
        val minLat = events.minOf { it.latitude }
        val maxLat = events.maxOf { it.latitude }
        val minLng = events.minOf { it.longitude }
        val maxLng = events.maxOf { it.longitude }

        val latSpan = if (maxLat - minLat == 0.0) 0.001 else (maxLat - minLat)
        val lngSpan = if (maxLng - minLng == 0.0) 0.001 else (maxLng - minLng)

        val padding = 80f
        val mapWidth = width - (padding * 2)
        val mapHeight = height - (padding * 2)

        val points = events.map { event ->
            val px = padding + ((event.longitude - minLng) / lngSpan * mapWidth).toFloat()
            val py = padding + ((maxLat - event.latitude) / latSpan * mapHeight).toFloat()
            Offset(px, py)
        }

        // Connect proximity points with route path
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = pathColor,
                style = Stroke(width = 4f)
            )
        }

        // Draw encounter pins
        points.forEach { pt ->
            drawCircle(
                color = pointColor.copy(alpha = 0.3f),
                radius = 16f,
                center = pt
            )
            drawCircle(
                color = pointColor,
                radius = 8f,
                center = pt
            )
        }

        // Draw current user center location
        val userPt = Offset(width / 2f, height / 2f)
        drawCircle(
            color = userColor.copy(alpha = 0.25f),
            radius = 28f,
            center = userPt
        )
        drawCircle(
            color = userColor,
            radius = 10f,
            center = userPt
        )
    }
}
