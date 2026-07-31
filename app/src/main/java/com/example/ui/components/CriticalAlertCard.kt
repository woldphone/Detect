package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BleDeviceEntity
import com.example.ui.theme.*

@Composable
fun CriticalAlertCard(
    threat: BleDeviceEntity,
    onViewMap: (BleDeviceEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SentinelAlertText.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("stalker_alert_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelAlertCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert Hazard Icon",
                    tint = SentinelAlertText,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "CRITICAL TRACKING DETECTION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentinelAlertText,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = threat.name ?: "Unknown BLE Signature",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "MAC Signature: ${threat.macAddress}",
                fontSize = 11.sp,
                color = SentinelAlertText.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RISK FACTOR LEVEL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelAlertText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${threat.stalkerRiskScore}% DANGER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SentinelAlertText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DISTINCT ENCOUNTERS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelAlertText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${threat.totalSightingsCount} Clusters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onViewMap(threat) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelAlertText,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_threat_on_map_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LOCATE & VIEW TRAJECTORY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
