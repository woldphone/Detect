package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LeakAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BleDeviceEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProximityMonitorCard(
    devices: List<BleDeviceEntity>,
    onDeviceClick: (BleDeviceEntity) -> Unit,
    onToggleIgnore: (BleDeviceEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SURVEILLANCE REGISTER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SentinelTextMuted,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tracked BLE Proximity Signatures",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SentinelTextPrimary,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Searching for signals... Turn on active scan above.",
                        fontSize = 11.sp,
                        color = SentinelTextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    devices.forEach { device ->
                        DeviceRowItem(
                            device = device,
                            onClick = { onDeviceClick(device) },
                            onToggleIgnore = { onToggleIgnore(device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceRowItem(
    device: BleDeviceEntity,
    onClick: () -> Unit,
    onToggleIgnore: () -> Unit
) {
    val isStalker = device.isStalkerAlert
    val borderColor = if (isStalker) SentinelAlertText.copy(alpha = 0.3f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("device_row_${device.macAddress}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Signal Icon Category Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isStalker) SentinelAlertText.copy(alpha = 0.15f)
                            else SentinelPurplePrimary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (device.deviceType) {
                        "AirTag / Smart Tag" -> Icons.Default.LeakAdd
                        "Smartwatch" -> Icons.Default.Watch
                        "Audio Device" -> Icons.Default.Hearing
                        "BLE Beacon" -> Icons.Default.SettingsCell
                        else -> Icons.Default.DeviceUnknown
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isStalker) SentinelAlertText else SentinelPurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = device.name ?: "Unknown BLE Beacon",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelTextPrimary
                        )

                        if (isStalker) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SentinelAlertText.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "THREAT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SentinelAlertText
                                )
                            }
                        }
                    }

                    Text(
                        text = device.macAddress,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SentinelTextMuted
                    )

                    Text(
                        text = "Seen: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(device.lastSeen))} • Signal: ${device.rssi} dBm",
                        fontSize = 10.sp,
                        color = SentinelTextMuted
                    )
                }
            }

            IconButton(
                onClick = onToggleIgnore,
                modifier = Modifier.testTag("device_ignore_button_${device.macAddress}")
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Ignore / Whitelist",
                    tint = if (device.isIgnored) SentinelSuccessGreen else SentinelTextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
