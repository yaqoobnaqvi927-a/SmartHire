package com.cs22.example.smarthire.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.darkColorScheme

private val DarkThemeColorScheme = darkColorScheme(
    primary = SmartHirePrimary,
    secondary = SmartHireSecondary,
    background = SmartHireBackground,
    surface = SmartHireSurface,
    surfaceVariant = SmartHireSurfaceContainer,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = SmartHireOnSurface,
    onSurface = SmartHireOnSurface,
    onSurfaceVariant = SmartHireOnSurfaceVariant,
    outline = SmartHireOutline,
    error = SmartHireError
)

@Composable
fun SmartHireTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkThemeColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SmartHireBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            window.navigationBarColor = SmartHireBackground.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
