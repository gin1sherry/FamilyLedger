package com.example.familyledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 澎湃 OS 视觉向主题：稳定品牌橙 + 高对比中性色，关闭动态取色以保持一致观感。
 */
private val LightScheme = lightColorScheme(
    primary = HyperColors.Orange,
    onPrimary = Color.White,
    primaryContainer = HyperColors.OrangeSoft,
    onPrimaryContainer = HyperColors.OrangeDeep,
    secondary = HyperColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = HyperColors.OrangeSoft,
    onSecondaryContainer = HyperColors.OrangeDeep,
    tertiary = HyperColors.Ok,
    onTertiary = Color.White,
    background = HyperColors.Bg,
    onBackground = HyperColors.OnSurface,
    surface = HyperColors.Surface,
    onSurface = HyperColors.OnSurface,
    surfaceVariant = HyperColors.Surface2,
    onSurfaceVariant = HyperColors.OnSurface2,
    outline = HyperColors.Outline,
    outlineVariant = HyperColors.Outline,
    error = HyperColors.Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E3),
    onErrorContainer = Color(0xFF8C1D18),
    inverseSurface = HyperColors.OnSurface,
    inverseOnSurface = HyperColors.Surface
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFF8A3D),
    onPrimary = Color(0xFF3A1500),
    primaryContainer = Color(0xFF7A3200),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFFF8A3D),
    onSecondary = Color(0xFF3A1500),
    secondaryContainer = Color(0xFF7A3200),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = HyperColors.OkDark,
    onTertiary = Color.Black,
    background = HyperColors.BgDark,
    onBackground = HyperColors.OnSurfaceDark,
    surface = HyperColors.SurfaceDark,
    onSurface = HyperColors.OnSurfaceDark,
    surfaceVariant = HyperColors.Surface2Dark,
    onSurfaceVariant = HyperColors.OnSurface2Dark,
    outline = HyperColors.OutlineDark,
    outlineVariant = HyperColors.OutlineDark,
    error = HyperColors.DangerDark,
    onError = Color.Black,
    errorContainer = Color(0x33FF453A),
    onErrorContainer = Color(0xFFFFB4AB),
    inverseSurface = HyperColors.OnSurfaceDark,
    inverseOnSurface = HyperColors.SurfaceDark
)

@Composable
fun FamilyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** 澎湃风格固定品牌色，不随壁纸动态变色 */
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION")
    val unusedBuild = if (Build.VERSION.SDK_INT >= 31) true else false

    val colorScheme = if (darkTheme) DarkScheme else LightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HyperTypography,
        shapes = HyperShapeDefaults,
        content = {
            ProvideHyperShapes(content = content)
        }
    )
}
