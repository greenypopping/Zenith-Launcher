package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.data.model.ThemePreset

private val YinYangColorScheme = darkColorScheme(
    primary = YinYangPrimary,
    onPrimary = YinYangOnPrimary,
    secondary = YinYangSecondary,
    onSecondary = YinYangPrimary,
    background = YinYangBg,
    onBackground = YinYangPrimary,
    surface = YinYangSurface,
    onSurface = YinYangPrimary,
    surfaceVariant = YinYangSurfaceVariant,
    onSurfaceVariant = YinYangSecondary,
    outline = YinYangOutline
)

private val ObsidianColorScheme = darkColorScheme(
    primary = ObsidianPrimary,
    onPrimary = ObsidianOnPrimary,
    secondary = ObsidianSecondary,
    onSecondary = ObsidianPrimary,
    background = ObsidianBg,
    onBackground = ObsidianPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianSecondary,
    outline = ObsidianOutline
)

private val PaperColorScheme = lightColorScheme(
    primary = PaperPrimary,
    onPrimary = PaperOnPrimary,
    secondary = PaperSecondary,
    onSecondary = PaperPrimary,
    background = PaperBg,
    onBackground = PaperPrimary,
    surface = PaperSurface,
    onSurface = PaperPrimary,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = PaperSecondary,
    outline = PaperOutline
)

private val SandColorScheme = darkColorScheme(
    primary = SandPrimary,
    onPrimary = SandOnPrimary,
    secondary = SandSecondary,
    onSecondary = SandPrimary,
    background = SandBg,
    onBackground = SandPrimary,
    surface = SandSurface,
    onSurface = SandPrimary,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = SandSecondary,
    outline = SandOutline
)

private val SageColorScheme = darkColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    secondary = SageSecondary,
    onSecondary = SagePrimary,
    background = SageBg,
    onBackground = SagePrimary,
    surface = SageSurface,
    onSurface = SagePrimary,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = SageSecondary,
    outline = SageOutline
)

fun getThemeColorScheme(preset: ThemePreset): ColorScheme {
    return when (preset) {
        ThemePreset.YIN_YANG_MONO -> YinYangColorScheme
        ThemePreset.OBSIDIAN_DARK -> ObsidianColorScheme
        ThemePreset.PAPER_LIGHT -> PaperColorScheme
        ThemePreset.WARM_SAND -> SandColorScheme
        ThemePreset.ZEN_SAGE -> SageColorScheme
    }
}

@Composable
fun ZenithLauncherTheme(
    themePreset: ThemePreset = ThemePreset.YIN_YANG_MONO,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(themePreset)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
