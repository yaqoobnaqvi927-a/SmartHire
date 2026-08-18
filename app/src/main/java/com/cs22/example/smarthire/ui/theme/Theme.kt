package com.cs22.example.smarthire.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
// Stitch Light Color Scheme (Primary UI theme)
// ─────────────────────────────────────────────────────────────────────────────
private val StitchLightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,
    inversePrimary       = InversePrimary,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = Tertiary,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    background           = Background,
    onBackground         = OnBackground,

    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,
    surfaceTint          = SurfaceTint,
    inverseSurface       = InverseSurface,
    inverseOnSurface     = InverseOnSurface,

    outline              = Outline,
    outlineVariant       = OutlineVariant,

    error                = Error,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,

    scrim                = OnSurface.copy(alpha = 0.32f)
)

// ─────────────────────────────────────────────────────────────────────────────
// Stitch Dark Color Scheme
// ─────────────────────────────────────────────────────────────────────────────
private val StitchDarkColorScheme = darkColorScheme(
    primary              = PrimaryFixedDim,
    onPrimary            = Color(0xFF001A43),
    primaryContainer     = Primary,
    onPrimaryContainer   = PrimaryFixed,
    inversePrimary       = Primary,

    secondary            = SecondaryFixedDim,
    onSecondary          = Color(0xFF001946),
    secondaryContainer   = Secondary,
    onSecondaryContainer = SecondaryFixed,

    tertiary             = TertiaryFixedDim,
    onTertiary           = Color(0xFF321200),
    tertiaryContainer    = Tertiary,
    onTertiaryContainer  = TertiaryFixed,

    background           = DarkBackground,
    onBackground         = DarkOnSurface,

    surface              = DarkSurface,
    onSurface            = DarkOnSurface,
    surfaceVariant       = DarkSurfaceVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    surfaceTint          = PrimaryFixedDim,
    inverseSurface       = SurfaceContainerHighest,
    inverseOnSurface     = OnSurface,

    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,

    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),

    scrim                = Color.Black.copy(alpha = 0.32f)
)


// ─────────────────────────────────────────────────────────────────────────────
// SmartHire App Theme
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SmartHireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StitchDarkColorScheme else StitchLightColorScheme
    val statusBarDark = darkTheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = (
                if (darkTheme) DarkBackground else Background
            ).value.toInt()
            window.navigationBarColor = (
                if (darkTheme) DarkSurface else SurfaceContainerLowest
            ).value.toInt()
            insetsController.isAppearanceLightStatusBars = !statusBarDark
            insetsController.isAppearanceLightNavigationBars = !statusBarDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = StitchTypography,
        content     = content
    )
}
