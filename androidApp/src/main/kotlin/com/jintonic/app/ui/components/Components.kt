package com.jintonic.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import com.jintonic.app.ui.theme.FfdColors
import com.jintonic.app.ui.theme.FfdSizes
import com.jintonic.app.ui.theme.FfdType

// Mockup ".top": on a round screen it is curved along the top edge so it never collides with the ring.
@Composable
fun TopLabel(text: String) {
    TimeText(backgroundColor = Color.Transparent) { _ ->
        curvedText(
            text = text.uppercase(),
            color = FfdColors.LabelTop,
            fontSize = FfdType.TopLabel,
            letterSpacing = 0.07.em,
        )
    }
}

// Mockup ".ring": coloured 6dp ring, big score, small uppercase caption.
@Composable
fun ScoreRing(score: Int?, caption: String, ringColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .size(FfdSizes.RingDiameter)
            .border(FfdSizes.RingStroke, ringColor, CircleShape),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = score?.toString() ?: "--",
            fontSize = FfdType.RingNumber,
            fontWeight = FontWeight.Bold,
            color = FfdColors.OnBackground,
        )
        Text(
            text = caption.uppercase(),
            fontSize = FfdType.RingLabel,
            letterSpacing = 0.08.em,
            color = FfdColors.TextSecondary,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

// Mockup ".btn": white pill, black 600 label.
@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(FfdSizes.PillHeight),
        shape = RoundedCornerShape(FfdSizes.PillRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = FfdColors.PillContainer,
            contentColor = FfdColors.PillContent,
        ),
        contentPadding = PaddingValues(horizontal = FfdSizes.PillPaddingH, vertical = FfdSizes.PillPaddingV),
    ) {
        Text(text = text, fontSize = FfdType.Pill, fontWeight = FontWeight.SemiBold)
    }
}

// Secondary navigation below the mockup viewport; dark surface so the white pill stays primary.
@Composable
fun MenuButton(text: String, accent: Color = FfdColors.OnBackground, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = FfdColors.Surface, contentColor = accent),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

// List row for the round face: label over value, so long labels never squeeze or split the value.
@Composable
fun InfoRow(label: String, value: String, valueColor: Color = FfdColors.OnBackground) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = FfdType.Body, color = FfdColors.TextSecondary, textAlign = TextAlign.Center)
        Text(text = value, fontSize = FfdType.Title, fontWeight = FontWeight.SemiBold, color = valueColor, textAlign = TextAlign.Center)
    }
}

@Composable
fun BodyText(text: String, modifier: Modifier = Modifier, color: Color = FfdColors.TextSecondary) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = FfdType.Body,
        color = color,
        textAlign = TextAlign.Center,
        lineHeight = 1.3.em,
    )
}

@Composable
fun TitleText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = FfdType.Title,
        fontWeight = FontWeight.SemiBold,
        color = FfdColors.OnBackground,
        textAlign = TextAlign.Center,
    )
}

// Mockup ".w": black round face, content centred with a 10dp gap.
@Composable
fun WatchFace(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FfdSizes.ScreenGap),
            modifier = Modifier.padding(20.dp),
        ) { content() }
    }
}
