package com.jintonic.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import com.jintonic.app.pvt.Status
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.HealthParameter

// Design tokens taken verbatim from docs/flussi_ui.html (CSS source, not eyeballed).
object FfdColors {
    val Background = Color(0xFF000000)
    val OnBackground = Color(0xFFFFFFFF)
    val Green = Color(0xFF39D98A)
    val Yellow = Color(0xFFF2C14E)
    val Red = Color(0xFFFF6B5E)
    val TextSecondary = Color(0xFFC2C9D0) // .s, .ring .l
    val LabelTop = Color(0xFF7A8692) // .top
    val LabelBottom = Color(0xFF9AA4AE) // .bot
    val PillContainer = Color(0xFFFFFFFF) // .btn
    val PillContent = Color(0xFF000000)
    val Surface = Color(0xFF1A1C1E)
}

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
    Status.GREEN -> FfdColors.Green
    Status.YELLOW -> FfdColors.Yellow
    Status.RED -> FfdColors.Red
}

fun AlertLevel.color(): Color = when (this) {
    AlertLevel.NOMINAL -> FfdColors.Green
    AlertLevel.WARNING -> FfdColors.Yellow
    AlertLevel.CRITICAL -> FfdColors.Red
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

@Composable
fun FfdTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
