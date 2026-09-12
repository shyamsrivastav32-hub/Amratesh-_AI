package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = CalcOperatorKey,
    onPrimaryContainer = Color.White,
    secondary = TertiarySky,
    onSecondary = Color.Black,
    background = CalcBgDark,
    onBackground = Color.White,
    surface = CalcSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = CalcNumberKey,
    onSurfaceVariant = CalcNumberKeyText,
  )

private val LightColorScheme = DarkColorScheme // Default to sleek obsidian calculator theme for optimal contrast

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve crafted high-contrast calculator palette
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content,
  )
}

