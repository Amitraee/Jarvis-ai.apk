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

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisPrimaryNeon,
    secondary = JarvisSecondaryNeon,
    tertiary = JarvisAccentGlow,
    background = JarvisDarkBg,
    surface = JarvisSurface,
    onPrimary = JarvisDarkBg,
    onSecondary = JarvisDarkBg,
    onTertiary = JarvisDarkBg,
    onBackground = JarvisTextPrimary,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisCardBg,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default for JARVIS futuristic aesthetic
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve Stark neon styling
  content: @Composable () -> Unit,
) {
  val colorScheme = JarvisColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
