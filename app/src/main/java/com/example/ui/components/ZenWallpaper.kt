package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import com.example.data.model.WallpaperPreset
import kotlin.random.Random

@Composable
fun ZenWallpaper(
    preset: WallpaperPreset = WallpaperPreset.STARRY_MOUNTAIN,
    modifier: Modifier = Modifier
) {
    when (preset) {
        WallpaperPreset.STARRY_MOUNTAIN -> StarryMountainWallpaper(modifier)
        WallpaperPreset.LUSH_LEAVES -> LushLeavesWallpaper(modifier)
        WallpaperPreset.DUSK_GRADIENT -> DuskGradientWallpaper(modifier)
        WallpaperPreset.SUNSET_CABIN -> SunsetCabinWallpaper(modifier)
        WallpaperPreset.OLED_BLACK -> Box(modifier = modifier.fillMaxSize().background(Color(0xFF030303)))
        WallpaperPreset.PAPER_WHITE -> Box(modifier = modifier.fillMaxSize().background(Color(0xFFF9FAFB)))
    }
}

@Composable
fun StarryMountainWallpaper(modifier: Modifier = Modifier) {
    // Generate deterministic star positions once
    val stars = remember {
        val rand = Random(42)
        List(70) {
            Triple(rand.nextFloat(), rand.nextFloat() * 0.65f, rand.nextFloat() * 1.5f + 0.5f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }

        // Cache paths and gradients keyed by dimensions so they are NOT allocated on every draw frame
        val cachedAssets = remember(width, height) {
            val skyBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F172A), // Dark slate navy
                    Color(0xFF1E293B),
                    Color(0xFF334155),
                    Color(0xFF1E293B)
                ),
                startY = 0f,
                endY = height
            )

            val peakBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE2E8F0), Color(0xFF475569)),
                startY = height * 0.42f,
                endY = height * 0.75f
            )

            val peakPath = Path().apply {
                moveTo(0f, height * 0.75f)
                lineTo(width * 0.25f, height * 0.58f)
                lineTo(width * 0.5f, height * 0.42f)
                lineTo(width * 0.75f, height * 0.60f)
                lineTo(width, height * 0.72f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val ridgePath = Path().apply {
                moveTo(width * 0.5f, height * 0.42f)
                lineTo(width * 0.48f, height * 0.52f)
                lineTo(width * 0.52f, height * 0.62f)
                lineTo(width * 0.5f, height * 0.75f)
                lineTo(width, height * 0.72f)
                lineTo(width * 0.75f, height * 0.60f)
                close()
            }

            val midForestPath = Path().apply {
                moveTo(0f, height * 0.72f)
                lineTo(width * 0.35f, height * 0.68f)
                lineTo(width * 0.7f, height * 0.75f)
                lineTo(width, height * 0.70f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            val foreForestPath = Path().apply {
                moveTo(0f, height * 0.84f)
                lineTo(width * 0.2f, height * 0.80f)
                lineTo(width * 0.5f, height * 0.86f)
                lineTo(width * 0.8f, height * 0.82f)
                lineTo(width, height * 0.85f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            StarryMountainAssets(skyBrush, peakBrush, peakPath, ridgePath, midForestPath, foreForestPath)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = cachedAssets.skyBrush)

            stars.forEach { (xRatio, yRatio, radius) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius,
                    center = Offset(xRatio * size.width, yRatio * size.height)
                )
            }

            drawPath(path = cachedAssets.peakPath, brush = cachedAssets.peakBrush)
            drawPath(path = cachedAssets.ridgePath, color = Color(0xFF1E293B).copy(alpha = 0.65f))
            drawPath(path = cachedAssets.midForestPath, color = Color(0xFF1E2633).copy(alpha = 0.85f))
            drawPath(path = cachedAssets.foreForestPath, color = Color(0xFF0F1720))
        }
    }
}

private class StarryMountainAssets(
    val skyBrush: Brush,
    val peakBrush: Brush,
    val peakPath: Path,
    val ridgePath: Path,
    val midForestPath: Path,
    val foreForestPath: Path
)

@Composable
fun LushLeavesWallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val bgBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF061A12),
                Color(0xFF0B2E21),
                Color(0xFF123D2E),
                Color(0xFF071F16)
            ),
            startY = 0f,
            endY = height
        )
        drawRect(brush = bgBrush)

        for (i in 0..6) {
            val yOffset = height * (0.2f + i * 0.12f)
            val xCenter = width * (if (i % 2 == 0) 0.35f else 0.65f)
            val leafWidth = width * 0.55f
            val leafHeight = height * 0.16f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E523F).copy(alpha = 0.45f),
                        Color(0xFF0E3024).copy(alpha = 0.1f)
                    ),
                    center = Offset(xCenter, yOffset),
                    radius = leafWidth / 2f
                ),
                topLeft = Offset(xCenter - leafWidth / 2f, yOffset - leafHeight / 2f),
                size = androidx.compose.ui.geometry.Size(leafWidth, leafHeight)
            )
        }
    }
}

@Composable
fun DuskGradientWallpaper(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF181326),
                    Color(0xFF2A1F3D),
                    Color(0xFF3E284D),
                    Color(0xFF20172D)
                ),
                startY = 0f,
                endY = size.height
            )
        )
    }
}

@Composable
fun SunsetCabinWallpaper(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }

        val cached = remember(width, height) {
            val skyBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1B35),
                    Color(0xFF3F2746),
                    Color(0xFF8B3A48),
                    Color(0xFFE06651),
                    Color(0xFF201625)
                ),
                startY = 0f,
                endY = height
            )

            val ridge = Path().apply {
                moveTo(0f, height * 0.72f)
                lineTo(width * 0.45f, height * 0.65f)
                lineTo(width * 0.8f, height * 0.70f)
                lineTo(width, height * 0.66f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            Pair(skyBrush, ridge)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = cached.first)
            drawPath(path = cached.second, color = Color(0xFF120E18))
        }
    }
}
