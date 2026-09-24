package com.example.familyledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Orange = Color(0xFFFF6900)
private val OrangeContainer = Color(0xFFFFE0CC)

private val LightColors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = Color(0xFF331200),
    background = Color(0xFFF7F6F4),
    surface = Color(0xFFFCFCFB),
    surfaceVariant = Color(0xFFF0EFE8)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB77A),
    onPrimary = Color(0xFF542100),
    primaryContainer = Color(0xFF7A3200),
    onPrimaryContainer = Color(0xFFFFDBC8),
    background = Color(0xFF1A1B1A),
    surface = Color(0xFF121312),
    surfaceVariant = Color(0xFF444741)
)

@Composable
fun FamilyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
