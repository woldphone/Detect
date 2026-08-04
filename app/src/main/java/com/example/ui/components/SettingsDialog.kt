package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.TrackerViewModel
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    viewModel: TrackerViewModel,
    isScanning: Boolean,
    motionState: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, SentinelPurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SentinelCardBg)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column {
                    Text(
                        text = "SENTINEL SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelPurplePrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Surveillance Configuration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SentinelTextPrimary
                    )
                }

                Divider(color = SentinelBg, thickness = 1.dp)

                // 1. Scanning State Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BLE Surveillance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelTextPrimary
                        )
                        Text(
                            text = "Activate mock Bluetooth tracking engine.",
                            fontSize = 10.sp,
                            color = SentinelTextMuted
                        )
                    }
                    Switch(
                        checked = isScanning,
                        onCheckedChange = { viewModel.toggleScan() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SentinelSuccessGreen,
                            checkedTrackColor = SentinelSuccessGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = SentinelTextMuted,
                            uncheckedTrackColor = SentinelBg
                        )
                    )
                }

                // 2. Scan Throttling Mode
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Context Throttling Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelTextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setMotionState("STILL") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (motionState == "STILL") SentinelPurplePrimary.copy(alpha = 0.25f) else SentinelBg,
                                contentColor = if (motionState == "STILL") SentinelPurplePrimary else SentinelTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Still (10s delay)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.setMotionState("WALKING") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (motionState == "WALKING") SentinelPurplePrimary.copy(alpha = 0.25f) else SentinelBg,
                                contentColor = if (motionState == "WALKING") SentinelPurplePrimary else SentinelTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Moving (3s delay)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. Database Maintenance Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Database & Log Actions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelTextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Purge older than 7 days
                        Button(
                            onClick = {
                                viewModel.purgeOldLogs()
                                Toast.makeText(context, "Old log history purged", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SentinelBg, contentColor = SentinelPurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Purge >7 Days", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Clear Database
                        Button(
                            onClick = {
                                viewModel.clearDatabase()
                                Toast.makeText(context, "Database cleared entirely", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SentinelBg, contentColor = SentinelAlertText),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("Clear DB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Explanation Info Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SentinelPurplePrimary.copy(alpha = 0.05f))
                        .padding(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = SentinelPurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BLE signals are simulated on background thread loops. This allows thorough verification of real-time multi-signature alert logic, tracking telemetry, and automated updater integrity locally without physical transmitters.",
                            fontSize = 9.sp,
                            color = SentinelTextMuted,
                            lineHeight = 12.sp
                        )
                    }
                }

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SentinelPurplePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "CLOSE SETTINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
