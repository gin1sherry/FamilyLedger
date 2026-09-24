package com.example.familyledger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 澎湃 OS 风格设计令牌（视觉向，非官方 SDK） */
object HyperColors {
    val Orange = Color(0xFFFF6A00)
    val OrangeDeep = Color(0xFFE85D00)
    val OrangeSoft = Color(0xFFFFF1E6)
    val Bg = Color(0xFFF4F4F6)
    val BgDark = Color(0xFF0F0F10)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1C1C1E)
    val Surface2 = Color(0xFFF0F0F3)
    val Surface2Dark = Color(0xFF2C2C2E)
    val OnSurface = Color(0xFF1A1A1A)
    val OnSurfaceDark = Color(0xFFF2F2F2)
    val OnSurface2 = Color(0xFF8E8E93)
    val OnSurface2Dark = Color(0xFF98989D)
    val Ok = Color(0xFF30D158)
    val Warn = Color(0xFFFF9F0A)
    val Danger = Color(0xFFFF453A)
    val OkDark = Color(0xFF32D74B)
    val WarnDark = Color(0xFFFFD60A)
    val DangerDark = Color(0xFFFF453A)
    val Outline = Color(0x14000000)
    val OutlineDark = Color(0x33FFFFFF)
}

@Immutable
data class HyperShapes(
    val card: RoundedCornerShape = RoundedCornerShape(24.dp),
    val cardSm: RoundedCornerShape = RoundedCornerShape(18.dp),
    val cardLg: RoundedCornerShape = RoundedCornerShape(28.dp),
    val input: RoundedCornerShape = RoundedCornerShape(16.dp),
    val button: RoundedCornerShape = RoundedCornerShape(28.dp),
    val chip: RoundedCornerShape = RoundedCornerShape(20.dp),
    val bottomBar: RoundedCornerShape = RoundedCornerShape(0.dp)
)

val LocalHyperShapes = staticCompositionLocalOf { HyperShapes() }

val HyperShapeDefaults = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val HyperTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)

@Composable
fun ProvideHyperShapes(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalHyperShapes provides HyperShapes(), content = content)
}

@Composable
@ReadOnlyComposable
fun hyperShapes(): HyperShapes = LocalHyperShapes.current
