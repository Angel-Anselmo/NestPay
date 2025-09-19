package com.icescream.nestpay.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NestPayPrimary,
    secondary = NestPaySecondary,
    tertiary = NestPaySecondaryVariant,
    background = NestPayDark,
    surface = NestPayDark,
    onPrimary = NestPayOnPrimary,
    onSecondary = NestPayOnPrimary,
    onTertiary = NestPayOnPrimary,
    onBackground = NestPayOnPrimary,
    onSurface = NestPayOnPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = NestPayPrimary,
    secondary = NestPaySecondary,
    tertiary = NestPaySecondaryVariant,
    background = NestPayBackground,
    surface = NestPaySurface,
    onPrimary = NestPayOnPrimary,
    onSecondary = NestPayOnPrimary,
    onTertiary = NestPayOnPrimary,
    onBackground = NestPayOnBackground,
    onSurface = NestPayOnSurface,
    error = NestPayError
)

@Composable
fun NestPayTheme(
    darkTheme: Boolean = false,
    // Dynamic color is disabled to maintain brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use light color scheme
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Always use light status bar icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}