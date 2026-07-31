package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.theme.*

@Composable
fun AppHeader(
    isScanning: Boolean,
    onToggleScan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isScanning) SentinelSuccessGreen.copy(alpha = 0.15f) else SentinelTextMuted.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar Shield Icon",
                        tint = if (isScanning) SentinelSuccessGreen else SentinelTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "SENTINEL GUARD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SentinelTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) SentinelSuccessGreen else Color.Gray)
                        )
                        Text(
                            text = if (isScanning) "ACTIVE SURVEILLANCE ON" else "SURVEILLANCE STOPPED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScanning) SentinelSuccessGreen else SentinelTextMuted
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = isScanning,
                    onCheckedChange = { onToggleScan() },
                    modifier = Modifier.testTag("scan_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SentinelSuccessGreen,
                        checkedTrackColor = SentinelSuccessGreen.copy(alpha = 0.3f),
                        uncheckedThumbColor = SentinelTextMuted,
                        uncheckedTrackColor = SentinelBg
                    )
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SentinelTextMuted
                    )
                }
            }
        }
    }
}
