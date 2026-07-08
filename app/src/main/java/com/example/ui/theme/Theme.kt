package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintGreenPrimary,
    secondary = MintGreenSecondary,
    tertiary = SoftBrownTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFF11140E),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFE3E4DC),
    onSurface = Color(0xFFE3E4DC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC5C8BA),
    outline = Color(0xFF44483E)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,            // NaturalPrimary: Color(0xFF4B6333)
    secondary = ForestGreenSecondary,        // NaturalSecondary: Color(0xFFD8E6CC)
    tertiary = WarmBrownTertiary,            // NaturalTertiary: Color(0xFF45483D)
    background = LightBackground,            // NaturalBackground: Color(0xFFFBFDF7)
    surface = LightSurface,                  // NaturalSurface: Color(0xFFFFFFFF)
    onPrimary = Color.White,
    onSecondary = NaturalDeepText,           // Dark Forest text on Sage Header: Color(0xFF111F0E)
    onBackground = NaturalBodyText,          // Primary dark body text: Color(0xFF191D16)
    onSurface = NaturalBodyText,
    surfaceVariant = LightSurfaceVariant,    // NaturalSurfaceVariant: Color(0xFFF2F5E9)
    onSurfaceVariant = NaturalMutedText,     // Secondary muted text: Color(0xFF75796C)
    outline = NaturalBorder                  // Border/outline: Color(0xFFE2E4D8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to strictly preserve our gorgeous custom organic theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
