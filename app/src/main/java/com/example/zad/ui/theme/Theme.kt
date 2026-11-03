package com.example.zad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = MintAccent,
    tertiary = MintAccent,
    background = AppBackground,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
    outlineVariant = CardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = MintAccent,
    onPrimary = Color(0xFF042F2E),
    secondary = Emerald,
    tertiary = MintAccent,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    onBackground = Color(0xFFF8FAFC),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun ZADTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}