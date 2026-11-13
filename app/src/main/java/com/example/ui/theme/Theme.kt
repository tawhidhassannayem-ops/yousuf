package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = DeepNavy,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueSoft,
    onPrimaryContainer = DeepNavy,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE8FF),
    onSecondaryContainer = DeepNavy,
    tertiary = GoldenYellow,
    onTertiary = DeepNavyDark,
    tertiaryContainer = GoldenYellowSoft,
    onTertiaryContainer = GoldenAmberText,
    background = CoolGrayBg,
    onBackground = DarkNavyText,
    surface = CardWhite,
    onSurface = DarkNavyText,
    surfaceVariant = Color(0xFFE9EFF8),
    onSurfaceVariant = MutedSlateText,
    outline = SoftOutline,
    error = StatusOfflineRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = ElectricBlueBright,
    onPrimary = Color.White,
    primaryContainer = DeepNavyLight,
    onPrimaryContainer = Color.White,
    secondary = GoldenYellow,
    onSecondary = DeepNavyDark,
    secondaryContainer = Color(0xFF3A2D00),
    onSecondaryContainer = GoldenYellowSoft,
    tertiary = GoldenYellow,
    onTertiary = DeepNavyDark,
    background = DeepNavyDark,
    onBackground = Color(0xFFF1F5FB),
    surface = DeepNavy,
    onSurface = Color(0xFFF1F5FB),
    surfaceVariant = DeepNavyLight,
    onSurfaceVariant = Color(0xFFB5C4DC),
    outline = Color(0xFF284673),
    error = Color(0xFFF87171),
    onError = DeepNavyDark
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun YusufElectronicsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
