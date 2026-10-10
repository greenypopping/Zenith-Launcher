package com.example.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockStyle
import com.example.data.model.HomeLayoutMode
import com.example.data.model.ThemePreset
import com.example.data.model.WallpaperPreset
import com.example.ui.components.ZenIcons
import com.example.ui.viewmodel.LauncherViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val hasUsagePerm by viewModel.hasUsagePermission.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(
                        imageVector = ZenIcons.Back,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customize your minimal experience",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // SECTION 1: Appearance & Themes
                item {
                    SettingsSection(
                        icon = ZenIcons.Palette,
                        title = "Appearance & Themes",
                        subtitle = "Atmospheric procedural wallpapers and contrast palettes"
                    ) {
                        // Wallpaper Preset
                        Text(
                            text = "Wallpaper Preset",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WallpaperPreset.entries.forEach { wallpaper ->
                                val isSelected = settings.wallpaperPreset == wallpaper
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setWallpaperPreset(wallpaper) },
                                    label = { Text(wallpaper.displayName, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("settings_wallpaper_${wallpaper.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Theme Preset
                        Text(
                            text = "Aesthetic Color Palette",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemePreset.entries.forEach { preset ->
                                val isSelected = settings.themePreset == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemePreset(preset) },
                                    label = { Text(preset.displayName, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("settings_theme_${preset.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Grayscale Icons Toggle
                        SettingsToggleRow(
                            title = "Grayscale App Icons",
                            subtitle = "Reduces bright saturated badges that trigger compulsive checking",
                            checked = settings.grayscaleIcons,
                            onCheckedChange = { viewModel.setGrayscaleIcons(it) },
                            testTag = "settings_toggle_grayscale"
                        )
                    }
                }

                // SECTION 2: Home Screen & Layout
                item {
                    SettingsSection(
                        icon = ZenIcons.Home,
                        title = "Home Screen & Layout",
                        subtitle = "Configure clock typography and app presentation format"
                    ) {
                        // Layout Mode (Text vs Icons)
                        Text(
                            text = "App Display Format",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeLayoutMode.entries.forEach { mode ->
                                val isSelected = settings.layoutMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setLayoutMode(mode) },
                                    label = { Text(mode.displayName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("settings_layout_${mode.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Clock Style
                        Text(
                            text = "Clock Typography Style",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClockStyle.entries.forEach { style ->
                                val isSelected = settings.clockStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setClockStyle(style) },
                                    label = { Text(style.displayName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("settings_clock_${style.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Max Pinned Apps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Max Pinned Apps",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Limit home shortcuts to curb decision fatigue",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(4, 6, 8).forEach { count ->
                                    FilterChip(
                                        selected = settings.maxPinnedApps == count,
                                        onClick = { viewModel.setMaxPinnedApps(count) },
                                        label = { Text("$count") },
                                        modifier = Modifier.testTag("settings_max_apps_$count")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Spotlight Search Bar
                        SettingsToggleRow(
                            title = "Spotlight Search Bar",
                            subtitle = "Fast app launcher and web search bar at bottom of Home",
                            checked = settings.showSearchBar,
                            onCheckedChange = { viewModel.setShowSearchBar(it) },
                            testTag = "settings_toggle_search_bar"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Glass Dock
                        SettingsToggleRow(
                            title = "Bottom Application Dock",
                            subtitle = "Quick essential launch shortcuts (Phone, Messages, Browser, Camera)",
                            checked = settings.showDock,
                            onCheckedChange = { viewModel.setShowDock(it) },
                            testTag = "settings_toggle_dock"
                        )
                    }
                }

                // SECTION 3: My Space (Productivity Workspace)
                item {
                    SettingsSection(
                        icon = ZenIcons.Widgets,
                        title = "My Space (Right Swipe Hub)",
                        subtitle = "Toggle modular productivity cards in your personal space"
                    ) {
                        val widgets = listOf(
                            Triple("Weather Forecast & Outlook", settings.showWeatherWidget) { b: Boolean -> viewModel.setShowWeatherWidget(b) },
                            Triple("Today's Focus Tasks", settings.showFocusWidget) { b: Boolean -> viewModel.setShowFocusWidget(b) },
                            Triple("Quick Scratchpad Notes", settings.showNotesWidget) { b: Boolean -> viewModel.setShowNotesWidget(b) },
                            Triple("Device Calendar Events", settings.showCalendarWidget) { b: Boolean -> viewModel.setShowCalendarWidget(b) },
                            Triple("Stoic & Zen Wisdom Quotes", settings.showQuoteWidget) { b: Boolean -> viewModel.setShowQuoteWidget(b) },
                            Triple("Favorite Apps Quick Grid", settings.showMyAppsWidget) { b: Boolean -> viewModel.setShowMyAppsWidget(b) }
                        )

                        widgets.forEachIndexed { idx, (title, enabled, onToggle) ->
                            if (idx > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = onToggle,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.testTag("settings_widget_toggle_$idx")
                                )
                            }
                        }
                    }
                }

                // SECTION 4: Mind Space & Digital Wellbeing
                item {
                    SettingsSection(
                        icon = ZenIcons.Mindful,
                        title = "Mind Space & Wellbeing",
                        subtitle = "Screen time tracking, intentional friction, and mindful timers"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usage Stats Permission",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (hasUsagePerm) "Permission granted · On-device tracking active" else "Permission required for accurate screen time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasUsagePerm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = viewModel.usageStatsRepository.getUsageAccessSettingsIntent()
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("settings_usage_access_button")
                            ) {
                                Text(if (hasUsagePerm) "Manage" else "Grant")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mindful Pause & Focus Features",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add breathing pauses to distracting apps, set daily timers, and launch timed deep work sessions in Mind Space (swipe left from Home).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // SECTION 5: Gestures & Navigation Map
                item {
                    SettingsSection(
                        icon = ZenIcons.Gestures,
                        title = "Gestures & Navigation",
                        subtitle = "Fast fluid gestures for effortless one-handed control"
                    ) {
                        val gestures = listOf(
                            "Swipe Right" to "Open My Space (Productivity Feed)",
                            "Swipe Left" to "Open Mind Space (Digital Wellbeing)",
                            "Swipe Up" to "Open Alphabetical Application Drawer",
                            "Tap Search" to "Spotlight App & Web Search",
                            "Long Press Home" to "Quick Customization Menu",
                            "Tap Clock" to "Cycle through Clock Typography Styles"
                        )

                        gestures.forEachIndexed { idx, (action, target) ->
                            if (idx > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = target,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // SECTION 6: System & Default Launcher
                item {
                    SettingsSection(
                        icon = ZenIcons.Tune,
                        title = "System & Default Launcher",
                        subtitle = "Configure Zenith as your primary Android home screen"
                    ) {
                        Text(
                            text = "Set Zenith as your default launcher so every time you unlock your phone you are greeted with calm clarity instead of algorithmic clutter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val manageIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try { context.startActivity(manageIntent) } catch (_: Exception) {}
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("settings_set_default_button")
                        ) {
                            Text("Set as Default Launcher")
                        }
                    }
                }

                // SECTION 7: Philosophy & Privacy
                item {
                    SettingsSection(
                        icon = ZenIcons.Info,
                        title = "Philosophy & Privacy",
                        subtitle = "Built on absolute digital sovereignty and offline respect"
                    ) {
                        Text(
                            text = "Zenith Launcher is inspired by the harmonious balance of the Yin Yang Minimalist Launcher. 100% offline, zero ads, zero tracking, zero external telemetry. All app restrictions, notes, and habits live solely on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Zenith Launcher v1.0 · Yin Yang Replica Edition",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Section Content Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
