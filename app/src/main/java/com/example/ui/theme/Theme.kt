package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = SentinelPurplePrimary,
    onPrimary = SentinelPurpleDark,
    primaryContainer = SentinelPurpleContainer,
    onPrimaryContainer = SentinelPurpleDark,
    secondary = SentinelChipBackground,
    onSecondary = SentinelTextPrimary,
    background = SentinelBackground,
    onBackground = SentinelTextPrimary,
    surface = SentinelSurface,
    onSurface = SentinelTextPrimary,
    surfaceVariant = SentinelCardBackground,
    onSurfaceVariant = SentinelTextMuted,
    error = SentinelAlertRedAccent,
    onError = SentinelAlertRedTextDark,
    errorContainer = SentinelAlertRedBg,
    onErrorContainer = SentinelAlertRedAccent,
    outline = SentinelTextMuted
)

@Composable
fun TrackerGuardTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Sophisticated Dark Theme First
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SentinelBackground.toArgb()
            window.navigationBarColor = SentinelSurface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
