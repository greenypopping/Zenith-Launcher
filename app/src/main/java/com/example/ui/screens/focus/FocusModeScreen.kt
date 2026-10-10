package com.example.ui.screens.focus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.components.ZenIcons
import com.example.ui.viewmodel.LauncherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val focusState by viewModel.focusState.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    var showAppsSheet by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d · EEE · h:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val minutes = focusState.remainingSeconds / 60
    val seconds = focusState.remainingSeconds % 60
    val timeDisplay = remember(minutes, seconds) {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    val now = remember { System.currentTimeMillis() }
    val startTimeFormatted = remember(now) { timeFormat.format(Date(now)) }
    val endTimeFormatted = remember(now, focusState.totalSeconds) {
        timeFormat.format(Date(now + focusState.totalSeconds * 1000L))
    }

    Surface(
        color = Color(0xFF0F1014),
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_mode_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: "JUN 14 · SUN · 11:20 AM" (matching Screenshot 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(ZenIcons.Back, contentDescription = "Back", tint = Color.White.copy(alpha = 0.8f))
                }

                Text(
                    text = dateFormat.format(Date()).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Center: Circular Framed Study/Zen Illustration (matching Screenshot 6)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                StudyZenIllustration(modifier = Modifier.size(190.dp))

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "FOCUS SESSION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (focusState.isRunning) "REMAINING" else "DURATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.45f)
                )

                // Large Digital Clock (matching Screenshot 6: "05:00")
                Text(
                    text = timeDisplay,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Timeline Bar: start time, duration pill, projected end time (matching Screenshot 6)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { focusState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = startTimeFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${focusState.workDurationMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = endTimeFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Controls & Bottom "::: APPS" Pill (matching Screenshot 6)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Controls
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!focusState.isRunning) {
                        Button(
                            onClick = { viewModel.focusRepository.startSession() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Start Focus", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (focusState.isPaused) viewModel.focusRepository.resumeSession()
                                else viewModel.focusRepository.pauseSession()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(if (focusState.isPaused) "Resume" else "Pause")
                        }

                        Button(
                            onClick = { viewModel.focusRepository.stopSession() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("End")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom "::: APPS" Pill Button (matching Screenshot 6)
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showAppsSheet = true }
                        .testTag("focus_apps_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = "Permitted Apps",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "APPS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }

    // Permitted Apps Bottom Sheet
    if (showAppsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAppsSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF14171F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Permitted Applications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
                Text(
                    text = "Only selected applications remain accessible during a focus session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(allApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFocusWhitelist(app, !app.isWhitelistedInFocus)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.isWhitelistedInFocus,
                                onCheckedChange = { viewModel.setFocusWhitelist(app, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.White,
                                    checkmarkColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyZenIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val centerOffset = center
        val radius = size.minDimension / 2f

        // Concentric grayscale vignette rings (matching Screenshot 6)
        drawCircle(color = Color.White.copy(alpha = 0.04f), radius = radius, center = centerOffset)
        drawCircle(color = Color.White.copy(alpha = 0.08f), radius = radius * 0.82f, center = centerOffset)
        drawCircle(color = Color.White.copy(alpha = 0.12f), radius = radius * 0.65f, center = centerOffset)
        drawCircle(color = Color(0xFF20232B), radius = radius * 0.50f, center = centerOffset)

        // Desk surface line
        val deskY = centerOffset.y + radius * 0.22f
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(centerOffset.x - radius * 0.38f, deskY),
            end = Offset(centerOffset.x + radius * 0.38f, deskY),
            strokeWidth = 2.dp.toPx()
        )

        // Study Lamp silhouette on left
        val lampBaseX = centerOffset.x - radius * 0.25f
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = Offset(lampBaseX, deskY),
            end = Offset(lampBaseX, deskY - radius * 0.25f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = Offset(lampBaseX, deskY - radius * 0.25f),
            end = Offset(lampBaseX + radius * 0.1f, deskY - radius * 0.32f),
            strokeWidth = 2.dp.toPx()
        )
        // Lamp head cone
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 6.dp.toPx(),
            center = Offset(lampBaseX + radius * 0.1f, deskY - radius * 0.32f)
        )

        // Person reading/studying silhouette
        val personX = centerOffset.x + radius * 0.05f
        val headCenter = Offset(personX, deskY - radius * 0.25f)
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 8.dp.toPx(), center = headCenter)

        // Body leaning forward
        val torsoPath = Path().apply {
            moveTo(personX - 6.dp.toPx(), headCenter.y + 8.dp.toPx())
            lineTo(personX + 8.dp.toPx(), headCenter.y + 8.dp.toPx())
            lineTo(personX + 12.dp.toPx(), deskY)
            lineTo(personX - 10.dp.toPx(), deskY)
            close()
        }
        drawPath(path = torsoPath, color = Color.White.copy(alpha = 0.75f))
    }
}
