package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.TrackerViewModel

@Composable
fun IncidentsScreen(
    viewModel: TrackerViewModel
) {
    val events by viewModel.allEvents.collectAsState()
    val stats by viewModel.smartLoggingStats.collectAsState()

    LogsScreen(
        events = events,
        stats = stats,
        onPurgeOldLogs = { viewModel.purgeOldLogs() },
        onClearDatabase = { viewModel.clearDatabase() }
    )
}
