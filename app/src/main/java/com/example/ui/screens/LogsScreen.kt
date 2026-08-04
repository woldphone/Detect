package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProximityEventEntity
import com.example.ui.SmartLoggingStats
import com.example.ui.theme.SentinelAlertText
import com.example.ui.theme.SentinelBg
import com.example.ui.theme.SentinelCardBg
import com.example.ui.theme.SentinelPillBg
import com.example.ui.theme.SentinelPurplePrimary
import com.example.ui.theme.SentinelTextMuted
import com.example.ui.theme.SentinelTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun LogsScreen(
    events: List<ProximityEventEntity>,
    stats: SmartLoggingStats,
    onPurgeOldLogs: () -> Unit,
    onClearDatabase: () -> Unit,
    readDiagnostics: () -> String = { "No diagnostics available." },
    clearDiagnostics: () -> Unit = {}
) {
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBg)
            .padding(20.dp)
            .testTag("logs_screen")
    ) {
        Text(
            text = "Proximity Event Database",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelTextPrimary
        )
        Text(
            text = "Adaptive log records with GPS coordinates & signal timestamps",
            fontSize = 12.sp,
            color = SentinelTextMuted,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Database Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPurgeOldLogs,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelPillBg,
                    contentColor = SentinelTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("purge_logs_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Purge >7D", fontSize = 10.sp)
            }

            Button(
                onClick = { showDiagnosticsDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SentinelPurplePrimary.copy(alpha = 0.2f),
                    contentColor = SentinelPurplePrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("diagnostics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Diagnostics", fontSize = 10.sp)
            }

            OutlinedButton(
                onClick = onClearDatabase,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("clear_db_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = SentinelAlertText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset All", fontSize = 10.sp, color = SentinelAlertText)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SentinelCardBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SentinelTextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Event Log Database Empty",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelTextPrimary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(events) { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SentinelCardBg)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MAC: ${event.macAddress}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SentinelTextPrimary
                            )
                            Text(
                                text = String.format("%.4f° N, %.4f° W", event.latitude, abs(event.longitude)),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SentinelTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = SimpleDateFormat("HH:mm:ss • MMM d", Locale.getDefault()).format(Date(event.timestamp)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SentinelPurplePrimary
                            )
                            Text(
                                text = "${event.rssi} dBm",
                                fontSize = 10.sp,
                                color = SentinelTextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Diagnostics System Dialog Console Overlay
    if (showDiagnosticsDialog) {
        val diagnosticsText = readDiagnostics()
        AlertDialog(
            onDismissRequest = { showDiagnosticsDialog = false },
            containerColor = SentinelCardBg,
            icon = {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = SentinelPurplePrimary
                )
            },
            title = {
                Text(
                    text = "Sentinel Diagnostics Console",
                    color = SentinelTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Uncaught crashes and active trace logs are stored here dynamically.",
                        color = SentinelTextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SentinelBg)
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = diagnosticsText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = SentinelTextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(diagnosticsText))
                        }
                    ) {
                        Text("Copy", color = SentinelPurplePrimary, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = {
                            clearDiagnostics()
                            showDiagnosticsDialog = false
                        }
                    ) {
                        Text("Clear", color = SentinelAlertText, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiagnosticsDialog = false }) {
                    Text("Close", color = SentinelTextPrimary)
                }
            }
        )
    }
}
