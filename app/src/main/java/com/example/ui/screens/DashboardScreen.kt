package com.example.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.BleDeviceEntity
import com.example.ui.SmartLoggingStats
import com.example.ui.components.AppHeader
import com.example.ui.components.CriticalAlertCard
import com.example.ui.components.ProximityMonitorCard
import com.example.ui.components.StatsSummaryRow
import com.example.ui.theme.SentinelBg

@Composable
fun DashboardScreen(
    isScanning: Boolean,
    threats: List<BleDeviceEntity>,
    devices: List<BleDeviceEntity>,
    stats: SmartLoggingStats,
    onToggleScan: () -> Unit,
    onOpenSettings: () -> Unit,
    onDeviceClick: (BleDeviceEntity) -> Unit,
    onToggleIgnore: (BleDeviceEntity) -> Unit,
    onViewThreatOnMap: (BleDeviceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBg)
            .testTag("dashboard_screen")
    ) {
        AppHeader(
            isScanning = isScanning,
            onToggleScan = onToggleScan,
            onOpenSettings = onOpenSettings
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
                        onViewMap = onViewThreatOnMap
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
                onDeviceClick = onDeviceClick,
                onToggleIgnore = onToggleIgnore
            )
        }
    }
}
