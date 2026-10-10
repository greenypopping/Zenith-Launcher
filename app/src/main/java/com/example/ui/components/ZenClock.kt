package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ZenClock(
    style: ClockStyle = ClockStyle.SCRIPT_DAY,
    onClockClick: () -> Unit = {},
    textColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }

    // Adaptive ticker: Only ticks every second if ANALOG_DIAL requires smooth second hand.
    // Digital and Script styles tick once per minute, saving massive CPU and battery!
    LaunchedEffect(style) {
        while (isActive) {
            currentTime = Date()
            val delayMs = if (style == ClockStyle.ANALOG_DIAL) {
                1000L
            } else {
                val msUntilNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
                msUntilNextMinute.coerceIn(1000L, 60_000L)
            }
            delay(delayMs)
        }
    }

    val dayNameFormat = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val shortDayFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val monthDayFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("EEEE, d MMM", Locale.getDefault()) }

    val dayName = dayNameFormat.format(currentTime)
    val shortDay = shortDayFormat.format(currentTime).uppercase()
    val monthDay = monthDayFormat.format(currentTime)
    val timeString = timeFormat.format(currentTime)
    val fullDate = fullDateFormat.format(currentTime)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClockClick
            )
            .testTag("zen_clock_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (style) {
            ClockStyle.SCRIPT_DAY -> {
                // Elegant cursive Day with flanked line and date
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontSize = 58.sp,
                        letterSpacing = 1.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("clock_script_day")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("clock_script_date_row")
                ) {
                    HorizontalDivider(
                        modifier = Modifier.width(28.dp),
                        thickness = 1.dp,
                        color = secondaryColor.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = monthDay,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        ),
                        color = secondaryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(28.dp),
                        thickness = 1.dp,
                        color = secondaryColor.copy(alpha = 0.5f)
                    )
                }
            }

            ClockStyle.MINIMAL_DIGITAL -> {
                // Clean Minimal Digital: "Thursday, 23 Apr" over bold digital "06:08"
                Text(
                    text = fullDate,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = secondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("clock_digital_date")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.testTag("clock_digital_time")
                )
            }

            ClockStyle.DIAL_CIRCLE -> {
                // Circular badge with Day / Time / Date
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, textColor.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(136.dp)
                        .testTag("clock_dial_circle")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = shortDay,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 10.sp
                            ),
                            color = secondaryColor
                        )
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = monthDay,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 11.sp
                            ),
                            color = secondaryColor
                        )
                    }
                }
            }

            ClockStyle.ANALOG_DIAL -> {
                val cal = remember { Calendar.getInstance() }
                cal.time = currentTime
                val hours = cal.get(Calendar.HOUR)
                val minutes = cal.get(Calendar.MINUTE)
                val seconds = cal.get(Calendar.SECOND)

                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        val centerOffset = center
                        val radius = size.minDimension / 2f

                        // Outer ring
                        drawCircle(
                            color = textColor.copy(alpha = 0.6f),
                            radius = radius,
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // 12 tick marks
                        for (i in 0 until 12) {
                            val angleRad = Math.toRadians((i * 30).toDouble())
                            val innerR = if (i % 3 == 0) radius - 8.dp.toPx() else radius - 5.dp.toPx()
                            val start = Offset(
                                (centerOffset.x + innerR * sin(angleRad)).toFloat(),
                                (centerOffset.y - innerR * cos(angleRad)).toFloat()
                            )
                            val end = Offset(
                                (centerOffset.x + radius * sin(angleRad)).toFloat(),
                                (centerOffset.y - radius * cos(angleRad)).toFloat()
                            )
                            drawLine(
                                color = textColor.copy(alpha = 0.7f),
                                start = start,
                                end = end,
                                strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        // Hour hand
                        val hourAngle = Math.toRadians(((hours % 12 + minutes / 60f) * 30).toDouble())
                        val hourHandLen = radius * 0.52f
                        drawLine(
                            color = textColor,
                            start = centerOffset,
                            end = Offset(
                                (centerOffset.x + hourHandLen * sin(hourAngle)).toFloat(),
                                (centerOffset.y - hourHandLen * cos(hourAngle)).toFloat()
                            ),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Minute hand
                        val minAngle = Math.toRadians(((minutes + seconds / 60f) * 6).toDouble())
                        val minHandLen = radius * 0.75f
                        drawLine(
                            color = textColor,
                            start = centerOffset,
                            end = Offset(
                                (centerOffset.x + minHandLen * sin(minAngle)).toFloat(),
                                (centerOffset.y - minHandLen * cos(minAngle)).toFloat()
                            ),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Center pin
                        drawCircle(color = textColor, radius = 3.dp.toPx())
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = fullDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor
                )
            }
        }
    }
}
