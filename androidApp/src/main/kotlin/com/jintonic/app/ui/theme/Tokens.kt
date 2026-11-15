package com.jintonic.app.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.jintonic.app.R
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel

// Black-and-white palette: neutrals only, with a single red reserved for danger.
object AppColors {
    val Background = Color(0xFF000000)
    val OnBackground = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF84848C)
    val Primary = Color(0xFFFFFFFF)
    val AccentSoft = Color(0xFF84848C)
    val AccentStrong = Color(0xFFFF3B30)
    val LevelHigh = Color(0xFFFFFFFF)
    val LevelMid = Color(0xFF84848C)
    val LevelLow = Color(0xFFFF3B30)
    val Error = Color(0xFFFF3B30)
}

// Dimensions Material does not provide out of the box.
object AppSizes {
    val Ring = 92.dp
    val RingStroke = 8.dp
    val Stimulus = 72.dp
    val StimulusGlow = 20.dp
}

val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

fun Status.color(): Color = when (this) {
    Status.GREEN -> AppColors.LevelHigh
    Status.YELLOW -> AppColors.LevelMid
    Status.RED -> AppColors.Error
}

fun AlertLevel.color(): Color = when (this) {
    AlertLevel.NOMINAL -> AppColors.LevelHigh
    AlertLevel.WARNING -> AppColors.LevelMid
    AlertLevel.CRITICAL -> AppColors.LevelLow
}

@StringRes
fun AlertLevel.labelRes(): Int = when (this) {
    AlertLevel.NOMINAL -> R.string.level_nominal
    AlertLevel.WARNING -> R.string.level_warning
    AlertLevel.CRITICAL -> R.string.level_critical
}

// Monochrome scheme so default components (cards, buttons, switches) read black-and-white.
private val AppColorScheme = ColorScheme(
    primary = Color(0xFFFFFFFF),
    primaryDim = Color(0xFFE5E5EA),
    primaryContainer = Color(0xFF2C2C2E),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF84848C),
    secondaryDim = Color(0xFF6E6E73),
    secondaryContainer = Color(0xFF2C2C2E),
    onSecondary = Color(0xFF000000),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFAEAEB2),
    tertiaryDim = Color(0xFF84848C),
    tertiaryContainer = Color(0xFF2C2C2E),
    onTertiary = Color(0xFF000000),
    onTertiaryContainer = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF6E6E73),
    outlineVariant = Color(0xFF48484A),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography(defaultFontFamily = GoogleSansFlex),
        content = content,
    )
}
