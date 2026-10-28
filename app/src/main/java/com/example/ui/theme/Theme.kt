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

private val DarkColorScheme = darkColorScheme(
    primary = ElectricAqua,
    onPrimary = NavyDark,
    primaryContainer = OceanBlueDark,
    onPrimaryContainer = OceanBlueLight,
    secondary = MintFoamSecondary,
    onSecondary = CleanWhite,
    secondaryContainer = MintFoamDark,
    onSecondaryContainer = MintFoamLight,
    tertiary = CoralExpress,
    onTertiary = CleanWhite,
    background = NavyDark,
    onBackground = CleanWhite,
    surface = NavySurface,
    onSurface = CleanWhite,
    surfaceVariant = NavyElevated,
    onSurfaceVariant = OceanBlueLight
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = CleanWhite,
    primaryContainer = OceanBlueLight,
    onPrimaryContainer = OceanBlueDark,
    secondary = MintFoamSecondary,
    onSecondary = CleanWhite,
    secondaryContainer = MintFoamLight,
    onSecondaryContainer = MintFoamDark,
    tertiary = CoralExpress,
    onTertiary = CleanWhite,
    background = CleanCanvas,
    onBackground = SlateTextPrimary,
    surface = CleanWhite,
    onSurface = SlateTextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary
)

@Composable
fun LaundryMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to keep cohesive brand styling
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
