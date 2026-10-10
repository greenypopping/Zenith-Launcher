package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun AlphabetScrubber(
    selectedLetter: Char,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = remember { ('A'..'Z').toList() + '#' }
    var componentHeight by remember { mutableFloatStateOf(0f) }

    fun updateLetter(y: Float) {
        if (componentHeight > 0) {
            val itemHeight = componentHeight / alphabet.size
            val index = (y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
            onLetterSelected(alphabet[index])
        }
    }

    Column(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight()
            .padding(vertical = 16.dp)
            .onGloballyPositioned {
                componentHeight = it.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    updateLetter(offset.y)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    updateLetter(change.position.y)
                }
            }
            .testTag("alphabet_scrubber"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val selectedIndex = alphabet.indexOf(selectedLetter).coerceAtLeast(0)

        alphabet.forEachIndexed { index, char ->
            val diff = abs(index - selectedIndex)
            // Curving inwards effect (matching Yin Yang Launcher Screenshot 7)
            val horizontalOffset = when (diff) {
                0 -> (-14).dp
                1 -> (-10).dp
                2 -> (-6).dp
                3 -> (-2).dp
                else -> 0.dp
            }

            val isSelected = char == selectedLetter
            val fontSize = if (isSelected) 12.sp else 9.sp
            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)

            Box(
                modifier = Modifier
                    .offset(x = horizontalOffset),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color
                )
            }
        }
    }
}
