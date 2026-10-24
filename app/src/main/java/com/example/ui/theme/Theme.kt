package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IslamicLightColorScheme = lightColorScheme(
    primary = IslamicEmerald,
    onPrimary = CleanWhite,
    primaryContainer = Color(0xFFD1E7DD),
    onPrimaryContainer = IslamicEmeraldDark,
    secondary = DesertGold,
    onSecondary = Color(0xFF2C2200),
    secondaryContainer = DesertSand,
    onSecondaryContainer = Color(0xFF4A3E00),
    tertiary = OasisTeal,
    onTertiary = CleanWhite,
    tertiaryContainer = Color(0xFFCCEBE3),
    onTertiaryContainer = Color(0xFF00382B),
    background = BrightBackground,
    onBackground = TextPrimary,
    surface = CleanWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFC4D1C7)
)

private val IslamicDarkColorScheme = darkColorScheme(
    primary = IslamicEmeraldLight,
    onPrimary = Color(0xFF003920),
    primaryContainer = IslamicEmeraldDark,
    onPrimaryContainer = Color(0xFFA3F0C5),
    secondary = DesertGoldLight,
    onSecondary = Color(0xFF3B3000),
    secondaryContainer = Color(0xFF554600),
    onSecondaryContainer = Color(0xFFFFE174),
    tertiary = OasisAqua,
    onTertiary = Color(0xFF00382C),
    background = Color(0xFF111713),
    onBackground = Color(0xFFE1E5E1),
    surface = Color(0xFF181F1A),
    onSurface = Color(0xFFE1E5E1),
    surfaceVariant = Color(0xFF2A342D),
    onSurfaceVariant = Color(0xFFC0CCC2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // prioritize our custom Islamic aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) IslamicDarkColorScheme else IslamicLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
