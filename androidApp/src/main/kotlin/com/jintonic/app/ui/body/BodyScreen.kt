package com.jintonic.app.ui.body

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.jintonic.app.core.states.ResultState
import com.jintonic.app.R
import com.jintonic.app.data.HealthView
import com.jintonic.app.ui.components.InfoRow
import com.jintonic.app.ui.health.HealthViewModel
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.color
import com.jintonic.app.ui.theme.labelRes
import com.jintonic.app.vitals.AlertLevel
import com.jintonic.app.vitals.Apparatus

// Position of each apparatus on the figure, as fractions of the canvas (x, y).
private val ANCHORS = mapOf(
    Apparatus.PSYCHOLOGICAL to Offset(0.5f, 0.11f),
    Apparatus.CARDIOVASCULAR to Offset(0.56f, 0.32f),
    Apparatus.RESPIRATORY to Offset(0.42f, 0.29f),
    Apparatus.METABOLIC to Offset(0.5f, 0.46f),
    Apparatus.RADIATION to Offset(0.25f, 0.38f),
    Apparatus.MUSCULOSKELETAL to Offset(0.44f, 0.78f),
)

@StringRes
private fun Apparatus.labelRes(): Int = when (this) {
    Apparatus.CARDIOVASCULAR -> R.string.apparatus_cardiovascular
    Apparatus.RESPIRATORY -> R.string.apparatus_respiratory
    Apparatus.METABOLIC -> R.string.apparatus_metabolic
    Apparatus.RADIATION -> R.string.param_radiation
    Apparatus.MUSCULOSKELETAL -> R.string.apparatus_musculoskeletal
    Apparatus.PSYCHOLOGICAL -> R.string.apparatus_psychological
}

// Worst level among each apparatus' readings; null = no data.
private fun HealthView.levels(): Map<Apparatus, AlertLevel> =
    assessment.readings.groupBy { it.parameter.apparatus }
        .mapValues { (_, readings) -> readings.maxOf { it.level } }

@Composable
fun BodyScreen(vm: HealthViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val levels = (state as? ResultState.Success)?.data?.levels().orEmpty()
    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxWidth(), contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.nav_body)) } }
            item { BodyFigure(levels) }
            Apparatus.entries.forEach { apparatus ->
                val level = levels[apparatus]
                item {
                    InfoRow(
                        stringResource(apparatus.labelRes()),
                        level?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.body_na),
                        level?.color() ?: FfdColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyFigure(levels: Map<Apparatus, AlertLevel>) {
    val apparatusNames = mapOf(
        Apparatus.CARDIOVASCULAR to stringResource(R.string.apparatus_cardiovascular),
        Apparatus.RESPIRATORY to stringResource(R.string.apparatus_respiratory),
        Apparatus.METABOLIC to stringResource(R.string.apparatus_metabolic),
        Apparatus.RADIATION to stringResource(R.string.param_radiation),
        Apparatus.MUSCULOSKELETAL to stringResource(R.string.apparatus_musculoskeletal),
        Apparatus.PSYCHOLOGICAL to stringResource(R.string.apparatus_psychological),
    )
    val levelNames = mapOf(
        AlertLevel.NOMINAL to stringResource(R.string.level_nominal),
        AlertLevel.WARNING to stringResource(R.string.level_warning),
        AlertLevel.CRITICAL to stringResource(R.string.level_critical),
    )
    val summary = levels.entries.joinToString { "${apparatusNames[it.key]} ${levelNames[it.value]}" }
    val desc = stringResource(R.string.body_map_desc, summary)
    Canvas(modifier = Modifier.size(width = 90.dp, height = 140.dp).semantics { contentDescription = desc }) {
        drawSilhouette(FfdColors.TextSecondary.copy(alpha = 0.25f), FfdColors.TextSecondary)
        ANCHORS.forEach { (apparatus, anchor) ->
            val color = levels[apparatus]?.color() ?: FfdColors.TextSecondary
            val center = Offset(anchor.x * size.width, anchor.y * size.height)
            drawCircle(color.copy(alpha = 0.35f), radius = 9.dp.toPx(), center = center)
            drawCircle(color, radius = 5.dp.toPx(), center = center)
        }
    }
}

private fun DrawScope.drawSilhouette(fill: Color, outline: Color) {
    val w = size.width
    val h = size.height
    val r = CornerRadius(w * 0.08f)
    drawCircle(fill, radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.11f))
    drawRoundRect(fill, topLeft = Offset(w * 0.3f, h * 0.21f), size = Size(w * 0.4f, h * 0.36f), cornerRadius = r)
    drawRoundRect(fill, topLeft = Offset(w * 0.14f, h * 0.22f), size = Size(w * 0.13f, h * 0.32f), cornerRadius = r)
    drawRoundRect(fill, topLeft = Offset(w * 0.73f, h * 0.22f), size = Size(w * 0.13f, h * 0.32f), cornerRadius = r)
    drawRoundRect(fill, topLeft = Offset(w * 0.32f, h * 0.58f), size = Size(w * 0.16f, h * 0.4f), cornerRadius = r)
    drawRoundRect(fill, topLeft = Offset(w * 0.52f, h * 0.58f), size = Size(w * 0.16f, h * 0.4f), cornerRadius = r)
    drawCircle(outline.copy(alpha = 0.4f), radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.11f), style = Stroke(1.dp.toPx()))
}
