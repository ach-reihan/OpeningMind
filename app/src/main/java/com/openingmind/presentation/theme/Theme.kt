package com.openingmind.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MochaColorScheme = darkColorScheme(
    primary = MochaLavender,
    onPrimary = MochaBase,
    primaryContainer = MochaSurface0,
    onPrimaryContainer = MochaLavender,
    secondary = MochaBlue,
    onSecondary = MochaBase,
    secondaryContainer = MochaSurface0,
    onSecondaryContainer = MochaBlue,
    tertiary = MochaMauve,
    onTertiary = MochaBase,
    background = MochaBase,
    onBackground = MochaText,
    surface = MochaBase,
    onSurface = MochaText,
    surfaceVariant = MochaSurface0,
    onSurfaceVariant = MochaText,
    error = MochaRed,
    onError = MochaBase,
    outline = MochaOverlay0
)

private val LatteColorScheme = lightColorScheme(
    primary = LatteLavender,
    onPrimary = LatteBase,
    primaryContainer = LatteSurface0,
    onPrimaryContainer = LatteLavender,
    secondary = LatteBlue,
    onSecondary = LatteBase,
    secondaryContainer = LatteSurface0,
    onSecondaryContainer = LatteBlue,
    tertiary = LatteMauve,
    onTertiary = LatteBase,
    background = LatteBase,
    onBackground = LatteText,
    surface = LatteBase,
    onSurface = LatteText,
    surfaceVariant = LatteSurface0,
    onSurfaceVariant = LatteText,
    error = LatteRed,
    onError = LatteBase,
    outline = LatteOverlay0
)

@Composable
fun OpeningMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> MochaColorScheme
        else -> LatteColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Status Bar
            window.statusBarColor = colorScheme.surface.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
            
            // Navigation Bar
            window.navigationBarColor = colorScheme.surfaceVariant.toArgb()
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
