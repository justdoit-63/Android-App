package com.example.fitness.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricNeonBlue,
    secondary = DeepBlueSecondaryDark,
    tertiary = DeepBlueTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBluePrimary,
    secondary = DeepBlueSecondary,
    tertiary = DeepBlueTertiary,
    background = LightBackground,
    surface = LightSurface
)

@Composable
fun FitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
