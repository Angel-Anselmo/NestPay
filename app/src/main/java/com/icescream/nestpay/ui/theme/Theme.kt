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
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to maintain brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}