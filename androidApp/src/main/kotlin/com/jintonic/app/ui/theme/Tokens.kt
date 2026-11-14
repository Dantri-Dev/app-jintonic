package com.jintonic.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.jintonic.app.R
import com.jintonic.app.design.ColorRoles
import com.jintonic.app.design.Palette
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.HealthParameter

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
    val LabelTop = Color(ColorRoles.LABEL)
    val LabelBottom = Color(ColorRoles.TEXT_SECONDARY)
    val PillContainer = Color(ColorRoles.PILL_CONTAINER)
    val PillContent = Color(ColorRoles.PILL_CONTENT)
    val Surface = Color(ColorRoles.SURFACE)
}

// Google Sans Flex (OFL), static weights from shared/design/res/font.
val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

object FfdSizes {
    // CSS says 100px, but on the round 192dp face it collides with the top label: 92dp (deliberate delta).
    val RingDiameter = 92.dp
    val RingStroke = 6.dp
    val StimulusDiameter = 70.dp
    val StimulusGlow = 20.dp
    val PillRadius = 20.dp
    val PillPaddingH = 15.dp
    val PillPaddingV = 6.dp
    val ScreenGap = 8.dp // CSS gap 10px, tightened for the round face
    val PillHeight = 28.dp // .btn: 6px + 11px*1.3 + 6px; M3 default would be 52dp
}

object FfdType {
    val RingNumber = 32.sp // .ring .n, weight 700
    val RingLabel = 8.5.sp // .ring .l, uppercase, .08em
    val Title = 14.sp // .t, weight 600
    val Body = 10.5.sp // .s
    val Pill = 11.sp // .btn, weight 600
    val TopLabel = 9.sp // .top, uppercase, .07em
    val BottomLabel = 10.sp // .bot
    val Faces = 19.sp // .faces
    val Streak = 10.sp // .fire
}

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

fun AlertLevel.label(): String = when (this) {
    AlertLevel.NOMINAL -> "Nominale"
    AlertLevel.WARNING -> "Attenzione"
    AlertLevel.CRITICAL -> "Critico"
}

fun HealthParameter.label(): String = when (this) {
    HealthParameter.HR -> "Frequenza cardiaca"
    HealthParameter.SPO2 -> "SpO2"
    HealthParameter.EXERCISE -> "Esercizio"
    HealthParameter.GLUCOSE -> "Glicemia"
    HealthParameter.STRESS -> "Stress"
    HealthParameter.RADIATION -> "Radiazioni"
    HealthParameter.SLEEP -> "Sonno"
    HealthParameter.MOOD -> "Umore"
}

// Wear Material scheme rebuilt on the shared palette, so default components (cards, tonal buttons, switches) match.
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
