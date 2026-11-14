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
import com.jintonic.app.design.ColorRoles
import com.jintonic.app.design.Palette
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel

// Colour roles come from shared (com.jintonic.app.design), the single source for Wear OS and watchOS.
object FfdColors {
    val Background = Color(ColorRoles.BACKGROUND)
    val OnBackground = Color(ColorRoles.ON_BACKGROUND)
    val LevelHigh = Color(ColorRoles.LEVEL_HIGH)
    val LevelMid = Color(ColorRoles.LEVEL_MID)
    val LevelLow = Color(ColorRoles.LEVEL_LOW)
    val Primary = Color(ColorRoles.PRIMARY)
    val AccentSoft = Color(ColorRoles.ACCENT_SOFT)
    val AccentStrong = Color(ColorRoles.ACCENT_STRONG)
    val TextSecondary = Color(ColorRoles.TEXT_SECONDARY)
}

// Shared dimensions that Material does not provide out of the box.
object FfdSizes {
    val Ring = 92.dp
    val RingStroke = 8.dp
    val Stimulus = 72.dp
    val StimulusGlow = 20.dp
}

// Google Sans Flex (OFL), applied to the whole typography through the theme.
val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

fun Status.color(): Color = when (this) {
    Status.GREEN -> FfdColors.LevelHigh
    Status.YELLOW -> FfdColors.LevelMid
    Status.RED -> FfdColors.LevelLow
}

fun AlertLevel.color(): Color = when (this) {
    AlertLevel.NOMINAL -> FfdColors.LevelHigh
    AlertLevel.WARNING -> FfdColors.LevelMid
    AlertLevel.CRITICAL -> FfdColors.LevelLow
}

@StringRes
fun AlertLevel.labelRes(): Int = when (this) {
    AlertLevel.NOMINAL -> R.string.level_nominal
    AlertLevel.WARNING -> R.string.level_warning
    AlertLevel.CRITICAL -> R.string.level_critical
}

// Wear Material scheme rebuilt on the shared palette, so default components (cards, buttons, switches) match.
private val FfdColorScheme = ColorScheme(
    primary = Color(ColorRoles.PRIMARY),
    primaryDim = Color(Palette.Blue.B600),
    primaryContainer = Color(Palette.Blue.B800),
    onPrimary = Color(ColorRoles.ON_PRIMARY),
    onPrimaryContainer = Color(Palette.Blue.B100),
    secondary = Color(ColorRoles.SECONDARY),
    secondaryDim = Color(Palette.Orange.O600),
    secondaryContainer = Color(Palette.Orange.O900),
    onSecondary = Color(ColorRoles.ON_SECONDARY),
    onSecondaryContainer = Color(Palette.Orange.O100),
    tertiary = Color(Palette.Blue.B300),
    tertiaryDim = Color(Palette.Blue.B500),
    tertiaryContainer = Color(Palette.Blue.B900),
    onTertiary = Color(Palette.Neutral.N800),
    onTertiaryContainer = Color(Palette.Blue.B100),
    surfaceContainerLow = Color(Palette.Neutral.N800),
    surfaceContainer = Color(Palette.Neutral.N800),
    surfaceContainerHigh = Color(Palette.Neutral.N700),
    onSurface = Color(Palette.Neutral.N0),
    onSurfaceVariant = Color(Palette.Neutral.N300),
    outline = Color(Palette.Neutral.N500),
    outlineVariant = Color(Palette.Neutral.N600),
    background = Color(ColorRoles.BACKGROUND),
    onBackground = Color(ColorRoles.ON_BACKGROUND),
    error = Color(Palette.Semantic.ERROR),
    onError = Color(Palette.Neutral.N0),
)

@Composable
fun FfdTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FfdColorScheme,
        typography = Typography(defaultFontFamily = GoogleSansFlex),
        content = content,
    )
}
