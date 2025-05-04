package com.example.analyzer.ui.theme

import android.app.Activity
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom color palette
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)
val AccentBlue = Color(0xFF2962FF)
val ErrorRed = Color(0xFFD50000)
val SuccessGreen = Color(0xFF00C853)
val WarningYellow = Color(0xFFFFD600)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray300,
    onSecondary = Black,
    tertiary = AccentBlue,
    background = Black,
    onBackground = White,
    surface = Gray900,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Gray800,
    onSecondary = White,
    tertiary = AccentBlue,
    background = White,
    onBackground = Black,
    surface = Gray100,
    onSurface = Black,
    error = ErrorRed,
    onError = White
)

@Composable
fun AnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Custom typography and shapes are omitted for brevity
// but would be defined here in a production app