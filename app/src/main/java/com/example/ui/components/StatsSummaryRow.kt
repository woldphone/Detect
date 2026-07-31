package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SmartLoggingStats
import com.example.ui.theme.*

@Composable
fun StatsSummaryRow(
    stats: SmartLoggingStats
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "DEVICES",
            value = "${stats.totalDevices}",
            subtitle = "Active Scans",
            icon = Icons.Default.Sensors,
            iconTint = SentinelPurplePrimary,
            modifier = Modifier.weight(1f).testTag("stat_card_devices")
        )

        StatCard(
            title = "LOGGED",
            value = "${stats.totalEvents}",
            subtitle = "GPS Encounters",
            icon = Icons.Default.PinDrop,
            iconTint = SentinelPurplePrimary,
            modifier = Modifier.weight(1f).testTag("stat_card_logged")
        )

        StatCard(
            title = "SUPPRESSED",
            value = "${stats.suppressedPings}",
            subtitle = "Saved Pings",
            icon = Icons.Default.BatteryChargingFull,
            iconTint = SentinelSuccessGreen,
            modifier = Modifier.weight(1f).testTag("stat_card_suppressed")
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentinelTextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = SentinelTextMuted
            )
        }
    }
}
