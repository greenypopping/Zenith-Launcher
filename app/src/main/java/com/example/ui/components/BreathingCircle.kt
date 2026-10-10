package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class BreathPhase(val instruction: String) {
    INHALE("Breathe In"),
    HOLD("Hold"),
    EXHALE("Release"),
    PAUSE("Stillness")
}

@Composable
fun BreathingCircle(
    cycleSeconds: Int = 12,
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var phaseSecondsRemaining by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        val segment = (cycleSeconds / 3).coerceAtLeast(3)
        while (isActive) {
            phase = BreathPhase.INHALE
            for (i in segment downTo 1) {
                phaseSecondsRemaining = i
                delay(1000)
            }
            phase = BreathPhase.HOLD
            for (i in segment downTo 1) {
                phaseSecondsRemaining = i
                delay(1000)
            }
            phase = BreathPhase.EXHALE
            for (i in segment downTo 1) {
                phaseSecondsRemaining = i
                delay(1000)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breath_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (cycleSeconds * 500), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (cycleSeconds * 500), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("breathing_circle_container"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val centerOffset = center
                val baseRadius = size.minDimension / 2.2f

                // Outer ambient pulse ring
                drawCircle(
                    color = primaryColor.copy(alpha = alpha * 0.35f),
                    radius = baseRadius * scale,
                    center = centerOffset
                )

                // Fine inner guide ring
                drawCircle(
                    color = outlineColor.copy(alpha = 0.4f),
                    radius = baseRadius * 0.75f,
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Soft inner glowing core
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = baseRadius * (scale * 0.85f),
                    center = centerOffset
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phase.instruction,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${remainingSeconds}s",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraLight
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
