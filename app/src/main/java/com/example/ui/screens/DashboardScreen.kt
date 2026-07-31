package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.BleDeviceEntity
import com.example.ui.TrackerTab
import com.example.ui.TrackerViewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.CriticalAlertCard
import com.example.ui.components.ProximityMonitorCard
import com.example.ui.components.StatsSummaryRow
import com.example.ui.theme.SentinelBg

@Composable
fun DashboardScreen(
    viewModel: TrackerViewModel
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val threats by viewModel.activeThreats.collectAsState()
    val devices by viewModel.allDevices.collectAsState()
    val stats by viewModel.smartLoggingStats.collectAsState()

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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Display Critical Stalker Alert Card if active threat exists
                val primaryThreat = threats.firstOrNull()
                if (primaryThreat != null) {
                    CriticalAlertCard(
                        threat = primaryThreat,
                        onViewMap = { threat ->
                            // Switch focus to this threat on map
                            viewModel.setSelectedDeviceForMap(threat.macAddress)
                            viewModel.setTab(TrackerTab.MAP)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Smart Logging Stats Summary Row
                StatsSummaryRow(stats = stats)

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Proximity Devices List
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
