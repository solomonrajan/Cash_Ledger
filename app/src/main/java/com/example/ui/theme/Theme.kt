package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFA0C8FF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    background = DarkBackground,
    onBackground = Color(0xFFE2E2E6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceContainer = Color(0xFF252525),
    surfaceContainerHigh = Color(0xFF2D2D2D),
    surfaceContainerHighest = Color(0xFF383838),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    secondaryContainer = Color(0xFF2C3E50),
    onSecondaryContainer = Color(0xFFD1E4FF),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF44474E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AppPrimary,
    background = AppBackground,
    surface = AppBackground,
    onBackground = AppOnBackground,
    onSurface = AppOnBackground,
    primaryContainer = AppPrimaryContainer,
    onPrimaryContainer = AppOnPrimaryContainer,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppOnSurfaceVariant,
  )

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode && view.context is Activity) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = android.graphics.Color.TRANSPARENT
      window.navigationBarColor = android.graphics.Color.TRANSPARENT
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
