package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    tertiary = NeonCyan,
    background = PureBlack,
    onBackground = Color.White,
    surface = DeepDarkGray,
    onSurface = Color.White,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    surfaceVariant = GlassCardBg,
    onSurfaceVariant = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6366F1), // Bright Modern Purple/Indigo
    secondary = Color(0xFF3B82F6), // Blue
    tertiary = Color(0xFF06B6D4), // Cyan
    background = Color(0xFFF8FAFC), // Modern sleek light slate background
    onBackground = Color(0xFF0F172A), // Deep Slate
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFFF1F5F9), // Light card frame background
    onSurfaceVariant = Color(0xFF334155)
  )

@Composable
fun MyApplicationTheme(
  themeSetting: String = "System",
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeSetting) {
    "Light Theme" -> false
    "Dark Theme" -> true
    "Future Neon (Amoled)" -> true
    else -> isSystemInDarkTheme() // "System Default"
  }

  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
