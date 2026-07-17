package com.example.eugene.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PredictionAccent
import com.example.domain.model.PredictionCategory

object EugeneShapes {
    val card = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(999.dp)
    val sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
}

object EugeneColors {
    // DESIGN_SYSTEM-1.md §1.1 Base palette (Light Mode)
    val LightBackgroundBase = Color(0xFFFBF7F2)
    val LightBackgroundSurface = Color(0xFFFFFFFF)
    val LightBackgroundSurfaceSunken = Color(0xFFF3EDE6)
    val LightTextPrimary = Color(0xFF1C1A17)
    val LightTextSecondary = Color(0xFF6B655C)
    val LightTextTertiary = Color(0xFF9C948A)
    val LightBorderSubtle = Color(0xFFEAE2D8)
    val LightSurfaceInverse = Color(0xFF111111)
    val LightTextOnInverse = Color(0xFFFFFFFF)

    // DESIGN_SYSTEM-1.md §1.4 Dark Mode
    val DarkBackgroundBase = Color(0xFF17140F)
    val DarkBackgroundSurface = Color(0xFF211D17)
    val DarkBackgroundSurfaceSunken = Color(0xFF2A251D)
    val DarkTextPrimary = Color(0xFFF5F0E8)
    val DarkTextSecondary = Color(0xFFA69C8C)
    val DarkTextTertiary = Color(0xFF7A7166)
    val DarkBorderSubtle = Color(0xFF3A342A)
    val DarkSurfaceInverse = Color(0xFFEDEAE4)
    val DarkTextOnInverse = Color(0xFF111111)

    // Backward compatibility with Status Sage/Orange/Amber per §1.3 and §1.4
    val LightSage = Color(0xFFA8C8A0)
    val DarkSage = Color(0xFF87AD7E)
    
    val LightOrange = Color(0xFFF4A97E)
    val DarkOrange = Color(0xFFE8935F)
    
    val LightAmber = Color(0xFFF2D88F)
    val DarkAmber = Color(0xFFD9BE6E)

    // Option Accent Mapping per DESIGN_SYSTEM-1.md §1.2 and §1.4
    fun getAccentColor(accent: PredictionAccent, isDark: Boolean): Color {
        return if (isDark) {
            when (accent) {
                PredictionAccent.SAGE -> Color(0xFF87AD7E)
                PredictionAccent.ORANGE -> Color(0xFFE8935F)
                PredictionAccent.AMBER -> Color(0xFFD9BE6E)
                PredictionAccent.BLUE -> Color(0xFF7FA8CE)
                PredictionAccent.PURPLE -> Color(0xFFA894D4)
                PredictionAccent.TEAL -> Color(0xFF6BA39F)
            }
        } else {
            when (accent) {
                PredictionAccent.SAGE -> Color(0xFFA8C8A0)
                PredictionAccent.ORANGE -> Color(0xFFF4A97E)
                PredictionAccent.AMBER -> Color(0xFFF2D88F)
                PredictionAccent.BLUE -> Color(0xFFA9CBE8)
                PredictionAccent.PURPLE -> Color(0xFFC9B8E8)
                PredictionAccent.TEAL -> Color(0xFF8FC4C0)
            }
        }
    }

    // Category Color Mapping (8 categories per DESIGN_SYSTEM-1.md §7)
    fun getCategoryColor(category: PredictionCategory, isDark: Boolean): Color {
        val accent = when (category) {
            PredictionCategory.POLITICS -> PredictionAccent.BLUE
            PredictionCategory.SPORTS -> PredictionAccent.ORANGE
            PredictionCategory.ECONOMY -> PredictionAccent.SAGE
            PredictionCategory.CULTURE -> PredictionAccent.PURPLE
            PredictionCategory.TECHNOLOGY -> PredictionAccent.TEAL
            PredictionCategory.BUSINESS -> PredictionAccent.AMBER
            PredictionCategory.ENTERTAINMENT -> PredictionAccent.PURPLE
            PredictionCategory.SCIENCE -> PredictionAccent.TEAL
        }
        return getAccentColor(accent, isDark)
    }
}

private val LightColorScheme = lightColorScheme(
    primary = EugeneColors.LightTextPrimary,
    onPrimary = EugeneColors.LightBackgroundSurface,
    primaryContainer = EugeneColors.LightBackgroundSurfaceSunken,
    onPrimaryContainer = EugeneColors.LightTextPrimary,
    secondary = EugeneColors.LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = EugeneColors.LightBackgroundSurfaceSunken,
    onSecondaryContainer = EugeneColors.LightTextPrimary,
    background = EugeneColors.LightBackgroundBase,
    onBackground = EugeneColors.LightTextPrimary,
    surface = EugeneColors.LightBackgroundSurface,
    onSurface = EugeneColors.LightTextPrimary,
    surfaceVariant = EugeneColors.LightBackgroundSurfaceSunken,
    onSurfaceVariant = EugeneColors.LightTextSecondary,
    outline = EugeneColors.LightBorderSubtle,
    outlineVariant = EugeneColors.LightBorderSubtle,
    inverseSurface = EugeneColors.LightSurfaceInverse,
    inverseOnSurface = EugeneColors.LightTextOnInverse
)

private val DarkColorScheme = darkColorScheme(
    primary = EugeneColors.DarkTextPrimary,
    onPrimary = EugeneColors.DarkBackgroundSurface,
    primaryContainer = EugeneColors.DarkBackgroundSurfaceSunken,
    onPrimaryContainer = EugeneColors.DarkTextPrimary,
    secondary = EugeneColors.DarkTextSecondary,
    onSecondary = EugeneColors.DarkTextPrimary,
    secondaryContainer = EugeneColors.DarkBackgroundSurfaceSunken,
    onSecondaryContainer = EugeneColors.DarkTextPrimary,
    background = EugeneColors.DarkBackgroundBase,
    onBackground = EugeneColors.DarkTextPrimary,
    surface = EugeneColors.DarkBackgroundSurface,
    onSurface = EugeneColors.DarkTextPrimary,
    surfaceVariant = EugeneColors.DarkBackgroundSurfaceSunken,
    onSurfaceVariant = EugeneColors.DarkTextSecondary,
    outline = EugeneColors.DarkBorderSubtle,
    outlineVariant = EugeneColors.DarkBorderSubtle,
    inverseSurface = EugeneColors.DarkSurfaceInverse,
    inverseOnSurface = EugeneColors.DarkTextOnInverse
)

val EugeneTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.72.sp // 12 * 0.06
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.32.sp // 11 * 0.12
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun EugeneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EugeneTypography,
        content = content
    )
}
