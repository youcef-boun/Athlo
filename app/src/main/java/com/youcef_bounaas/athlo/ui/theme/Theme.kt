package com.youcef_bounaas.athlo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val AthloDarkColorScheme = darkColorScheme(
    primary = AthloGreen,
    onPrimary = Color.Black, // Changed to black for better contrast on green
    primaryContainer = AthloDarkGray,
    onPrimaryContainer = Color.White,
    secondary = AthloLightGray,
    onSecondary = Color.Black,
    secondaryContainer = AthloDarkGray,
    onSecondaryContainer = Color.White,
    background = AthloDarkGray,
    onBackground = Color.White,
    surface = AthloDarkGray,
    onSurface = Color.White,
    error = AthloError,
    onError = Color.White
)

private val AthloLightColorScheme = lightColorScheme(
    primary = AthloGreen,
    onPrimary = Color.Black, // Changed to black for better contrast on green
    primaryContainer = Color.White,  // Changed from AthloDarkGray
    onPrimaryContainer = Color.Black, // Changed to black for light theme
    secondary = AthloLightGray,
    onSecondary = Color.Black,
    secondaryContainer = Color.White, // Changed from AthloDarkGray
    onSecondaryContainer = Color.Black,
    background = AthloBackground,
    onBackground = Color.Black,
    surface = AthloSurface,
    onSurface = Color.Black,
    error = AthloError,
    onError = Color.White
)
@Composable
fun AthloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        AthloDarkColorScheme
    } else {
        AthloLightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AthloTypography,
        shapes = AthloShapes,
        content = content
    )
}
