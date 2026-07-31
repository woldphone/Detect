package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BleDeviceEntity
import com.example.ui.TrackerTab
import com.example.ui.TrackerViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.CriticalAlertCard
import com.example.ui.components.ProximityMonitorCard
import com.example.ui.components.StatsSummaryRow
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: TrackerViewModel
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val threats by viewModel.activeThreats.collectAsState()
    val devices by viewModel.allDevices.collectAsState()
    val stats by viewModel.smartLoggingStats.collectAsState()
    val motionState by viewModel.motionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBg)
            .testTag("dashboard_screen")
    ) {
        AppHeader(
            isScanning = isScanning,
            onToggleScan = { viewModel.toggleScan() },
            onOpenSettings = {
                Toast.makeText(context, "Sentinel Guard Settings Open", Toast.LENGTH_SHORT).show()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Context Engine (Scan Throttling Selector)
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("context_engine_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CONTEXT SCAN THROTTLING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // "STILL" State Button
                            Button(
                                onClick = { viewModel.setMotionState("STILL") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (motionState == "STILL") SentinelPurplePrimary.copy(alpha = 0.25f) else SentinelBg,
                                    contentColor = if (motionState == "STILL") SentinelPurplePrimary else SentinelTextMuted
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("motion_still_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Still (Passive)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // "WALKING" State Button
                            Button(
                                onClick = { viewModel.setMotionState("WALKING") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (motionState == "WALKING") SentinelPurplePrimary.copy(alpha = 0.25f) else SentinelBg,
                                    contentColor = if (motionState == "WALKING") SentinelPurplePrimary else SentinelTextMuted
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("motion_walking_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Moving (Active)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Display Critical Stalker Alert Card if active threat exists
                val primaryThreat = threats.firstOrNull()
                if (primaryThreat != null) {
                    CriticalAlertCard(
                        threat = primaryThreat,
                        onViewMap = { threat ->
                            viewModel.setSelectedDeviceForMap(threat.macAddress)
                            viewModel.setTab(TrackerTab.MAP)
                        }
                    )
                }

                // Smart Logging Stats Summary Row
                StatsSummaryRow(stats = stats)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proximity Devices List
            Box(modifier = Modifier.weight(1f)) {
                ProximityMonitorCard(
                    devices = devices,
                    onDeviceClick = { device ->
                        Toast.makeText(context, "Focus: ${device.name ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                        viewModel.setSelectedDeviceForMap(device.macAddress)
                        viewModel.setTab(TrackerTab.MAP)
                    },
                    onToggleIgnore = { device ->
                        viewModel.toggleIgnoreDevice(device)
                    }
                )
            }
        }
    }
}
