package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LiquidGlassDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF031525),
    primaryContainer = Color(0xFF00364F),
    onPrimaryContainer = Color(0xFFBCE9FF),

    secondary = EmeraldSavings,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF8CF8AC),

    tertiary = NeonAmber,
    onTertiary = Color(0xFF452B00),

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = BackgroundSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BackgroundSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,

    outline = GlassBorderDark,
    outlineVariant = Color(0x1AFFFFFF),

    error = NeonRed,
    onError = Color.White
)

private val LiquidGlassLightColorScheme = lightColorScheme(
    primary = ElectricCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),

    secondary = EmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),

    tertiary = NeonAmber,
    onTertiary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = BackgroundSurfaceElevatedLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BackgroundSurfaceLightMode,
    onSurfaceVariant = TextSecondaryLight,

    outline = GlassBorderLight,
    outlineVariant = Color(0x150F172A),

    error = NeonRed,
    onError = Color.White
)

val LiquidGlassShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ShopyPulseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LiquidGlassDarkColorScheme else LiquidGlassLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LiquidGlassShapes,
        content = content
    )
}

// Backward compatibility aliases
@Composable
fun ShopyPelseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    ShopyPulseTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun SoukSmartTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    ShopyPulseTheme(darkTheme = darkTheme, content = content)
}
