package com.example.ui.screens.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MindfulPauseScreen(
    app: AppInfo,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onCancel()
    }

    val totalDuration = app.pauseDurationSeconds.coerceAtLeast(6)
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (elapsedSeconds < totalDuration && isActive) {
            delay(1000)
            elapsedSeconds += 1
        }
        // Auto-proceed after breath completes (matching Screenshot 9)
        delay(600)
        onProceed()
    }

    val progress by animateFloatAsState(
        targetValue = elapsedSeconds.toFloat() / totalDuration.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "pause_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .testTag("mindful_pause_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Floating Modal Card (matching Screenshot 9)
        Surface(
            color = Color(0xFF14171F),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 380.dp)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: "Mindful Pause", "Take a deep breath and relax."
                Text(
                    text = "Mindful Pause",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Take a deep breath and relax.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Centered Meditating Figure Illustration with Ripple Rings (matching Screenshot 9)
                MeditatingZenFigure(modifier = Modifier.size(160.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // Smooth Progress Bar (matching Screenshot 9)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.18f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Row: Today's Usage (left), Opens Today (right) (matching Screenshot 9)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Today's Usage",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = if (app.usedTodayMinutes > 0) "${app.usedTodayMinutes} mins" else "12 mins",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Opens Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = if (app.launchCountToday > 0) "${app.launchCountToday} times" else "3 times",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Primary Button: "I'll skip this time" (matching Screenshot 9)
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("skip_pause_button")
                ) {
                    Text(
                        text = "I'll skip this time",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Subtitle: "App will open automatically after the pause." (matching Screenshot 9)
                Text(
                    text = "App will open automatically after the pause.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MeditatingZenFigure(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val centerOffset = center
        val radius = size.minDimension / 2f

        // Ripple concentric outer circles (matching Screenshot 9)
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = radius * 0.98f,
            center = centerOffset
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = radius * 0.82f,
            center = centerOffset
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = radius * 0.66f,
            center = centerOffset
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = radius * 0.50f,
            center = centerOffset
        )

        // Meditating Monk Silhouette (head, shoulders, folded legs)
        val figureColor = Color.White.copy(alpha = 0.9f)
        val headRadius = radius * 0.14f
        val headCenter = Offset(centerOffset.x, centerOffset.y - radius * 0.22f)
        drawCircle(color = figureColor, radius = headRadius, center = headCenter)

        // Torso & Robe
        val bodyPath = Path().apply {
            moveTo(centerOffset.x - radius * 0.22f, centerOffset.y + radius * 0.15f)
            lineTo(centerOffset.x - radius * 0.12f, centerOffset.y - radius * 0.08f)
            lineTo(centerOffset.x + radius * 0.12f, centerOffset.y - radius * 0.08f)
            lineTo(centerOffset.x + radius * 0.22f, centerOffset.y + radius * 0.15f)
            close()
        }
        drawPath(path = bodyPath, color = figureColor)

        // Folded meditation base / legs
        val legsPath = Path().apply {
            moveTo(centerOffset.x - radius * 0.38f, centerOffset.y + radius * 0.26f)
            lineTo(centerOffset.x + radius * 0.38f, centerOffset.y + radius * 0.26f)
            lineTo(centerOffset.x + radius * 0.28f, centerOffset.y + radius * 0.15f)
            lineTo(centerOffset.x - radius * 0.28f, centerOffset.y + radius * 0.15f)
            close()
        }
        drawPath(path = legsPath, color = figureColor)
    }
}
