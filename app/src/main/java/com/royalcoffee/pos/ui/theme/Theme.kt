package com.royalcoffee.pos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoyalCoffeeLightColorScheme = lightColorScheme(
    primary = RoyalGreen,
    onPrimary = Color.White,

    secondary = RoyalGold,
    onSecondary = CoffeeBrown,

    tertiary = RoyalGreenDark,
    onTertiary = Color.White,

    background = SoftCream,
    onBackground = CoffeeBrown,

    surface = Color.White,
    onSurface = CoffeeBrown,

    surfaceVariant = LightGoldBackground,
    onSurfaceVariant = CoffeeBrown,

    error = ErrorRed,
    onError = Color.White
)

@Composable
fun RoyalCoffeePOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = RoyalCoffeeLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        window.statusBarColor = RoyalGreenDark.toArgb()
        window.navigationBarColor = SoftCream.toArgb()

        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}