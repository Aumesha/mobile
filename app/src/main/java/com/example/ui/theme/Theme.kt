package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF0A0F1D),
    primaryContainer = AmberGoldContainer,
    onPrimaryContainer = AmberGoldLight,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF0A0F1D),
    secondaryContainer = ElectricCyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = EmeraldReady,
    onTertiary = Color(0xFF0A0F1D),
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldReady,
    background = StudioDeepBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = CoralRecord,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
