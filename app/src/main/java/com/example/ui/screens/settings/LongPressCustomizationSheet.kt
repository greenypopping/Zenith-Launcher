package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LongPressCustomizationSheet(
    viewModel: LauncherViewModel,
    onOpenFullSettings: () -> Unit,
    onOpenFocusMode: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    // 0: Wallpaper & Theme, 1: Clock & Typography, 2: Home Layout, 3: Widgets & My Space
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("customization_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // Header: Title + Quick close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = ZenIcons.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Home Customisation",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tailor layout, widgets, and aesthetics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("customization_close_button")
                ) {
                    Icon(
                        imageVector = ZenIcons.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Categories horizontal tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    "Wallpapers" to Icons.Outlined.Image,
                    "Clock & Text" to Icons.Outlined.Schedule,
                    "Layout" to Icons.Outlined.Dashboard,
                    "Widgets" to Icons.Outlined.Widgets
                )

                categories.forEachIndexed { idx, (title, icon) ->
                    val isSelected = selectedCategoryIndex == idx
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategoryIndex = idx }
                            .testTag("customization_tab_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Body Content based on selected category
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp)
            ) {
                when (selectedCategoryIndex) {
                    0 -> WallpaperThemeTab(
                        settings = settings,
                        onSelectWallpaper = { viewModel.setWallpaperPreset(it) },
                        onSelectTheme = { viewModel.setThemePreset(it) }
                    )
                    1 -> ClockTypographyTab(
                        settings = settings,
                        onSelectClock = { viewModel.setClockStyle(it) }
                    )
                    2 -> HomeLayoutTab(
                        settings = settings,
                        onSelectLayout = { viewModel.setLayoutMode(it) },
                        onToggleSearchBar = { viewModel.setShowSearchBar(it) },
                        onToggleDock = { viewModel.setShowDock(it) },
                        onToggleGrayscale = { viewModel.setGrayscaleIcons(it) },
                        onSetMaxPinned = { viewModel.setMaxPinnedApps(it) }
                    )
                    3 -> WidgetsCustomizationTab(
                        settings = settings,
                        viewModel = viewModel
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom row: Full Settings shortcut + Quick Focus Mode + Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullSettings()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_full_settings_button")
                    ) {
                        Icon(
                            imageVector = ZenIcons.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Settings",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFocusMode()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_focus_button")
                    ) {
                        Icon(
                            imageVector = ZenIcons.Mindful,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Focus",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("customization_done_button")
                    ) {
                        Text(
                            text = "Done",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WallpaperThemeTab(
    settings: com.example.data.repository.LauncherSettings,
    onSelectWallpaper: (WallpaperPreset) -> Unit,
    onSelectTheme: (ThemePreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Atmospheric Wallpaper",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Procedurally crafted procedural landscapes and clean dark surfaces",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WallpaperPreset.entries.forEach { preset ->
                val isSelected = settings.wallpaperPreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectWallpaper(preset) },
                    label = { Text(preset.displayName) },
                    leadingIcon = if (isSelected) {
                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("chip_wallpaper_${preset.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "System Aesthetic Palette",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Accent palettes and contrast tones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemePreset.entries.forEach { preset ->
                val isSelected = settings.themePreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTheme(preset) },
                    label = { Text(preset.displayName) },
                    leadingIcon = if (isSelected) {
                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("chip_theme_${preset.name}")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClockTypographyTab(
    settings: com.example.data.repository.LauncherSettings,
    onSelectClock: (ClockStyle) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Clock Typography & Form",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Choose how time is presented on your center home screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClockStyle.entries.forEach { style ->
                val isSelected = settings.clockStyle == style
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectClock(style) }
                        .testTag("clock_option_${style.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (style) {
                                    ClockStyle.SCRIPT_DAY -> "Artistic cursive weekday with clean digital hours"
                                    ClockStyle.MINIMAL_DIGITAL -> "Prominent bold digital numerals with date"
                                    ClockStyle.DIAL_CIRCLE -> "Circular ring clock with active hand indicator"
                                    ClockStyle.ANALOG_DIAL -> "Classic analog clock dial with hour markers"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = ZenIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeLayoutTab(
    settings: com.example.data.repository.LauncherSettings,
    onSelectLayout: (HomeLayoutMode) -> Unit,
    onToggleSearchBar: (Boolean) -> Unit,
    onToggleDock: (Boolean) -> Unit,
    onToggleGrayscale: (Boolean) -> Unit,
    onSetMaxPinned: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "App Display Format",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Switch between distraction-free text lists and minimal app icons",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeLayoutMode.entries.forEach { mode ->
                val isSelected = settings.layoutMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectLayout(mode) },
                    label = { Text(mode.displayName) },
                    leadingIcon = if (isSelected) {
                        { Icon(ZenIcons.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    modifier = Modifier.testTag("chip_layout_${mode.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Max Pinned Apps selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Max Pinned Apps on Home",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Limit home choices to curb decision fatigue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(4, 6, 8).forEach { count ->
                    FilterChip(
                        selected = settings.maxPinnedApps == count,
                        onClick = { onSetMaxPinned(count) },
                        label = { Text("$count") },
                        modifier = Modifier.testTag("chip_max_apps_$count")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        // Interface toggles
        CustomizationSwitchRow(
            title = "Spotlight Search Bar",
            subtitle = "Quick app search bar at bottom of screen",
            checked = settings.showSearchBar,
            onCheckedChange = onToggleSearchBar,
            testTag = "switch_search_bar"
        )

        CustomizationSwitchRow(
            title = "Glass Dock (Phone, Camera, Web, Mail)",
            subtitle = "Floating quick launch dock at home bottom",
            checked = settings.showDock,
            onCheckedChange = onToggleDock,
            testTag = "switch_glass_dock"
        )

        CustomizationSwitchRow(
            title = "Grayscale App Icons",
            subtitle = "Dim color stimuli to prevent dopamine triggers",
            checked = settings.grayscaleIcons,
            onCheckedChange = onToggleGrayscale,
            testTag = "switch_grayscale_icons"
        )
    }
}

@Composable
private fun WidgetsCustomizationTab(
    settings: com.example.data.repository.LauncherSettings,
    viewModel: LauncherViewModel
) {
    var editNameText by remember(settings.userName) { mutableStateOf(settings.userName) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "My Space Workspace Widgets",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Enable or disable tiled widgets in your swipe-left / right workspace",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom Profile / Greeting Name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = editNameText,
                onValueChange = {
                    editNameText = it
                    viewModel.setUserName(it)
                },
                label = { Text("Workspace Profile Name") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("workspace_name_input")
            )
        }

        CustomizationSwitchRow(
            title = "Live Weather Widget",
            subtitle = "Local temperature, highs/lows, and hourly pills",
            checked = settings.showWeatherWidget,
            onCheckedChange = { viewModel.setShowWeatherWidget(it) },
            testTag = "switch_widget_weather"
        )

        CustomizationSwitchRow(
            title = "Today's Focus Widget",
            subtitle = "Interactive daily priorities and checkbox items",
            checked = settings.showFocusWidget,
            onCheckedChange = { viewModel.setShowFocusWidget(it) },
            testTag = "switch_widget_focus"
        )

        CustomizationSwitchRow(
            title = "My Apps Quick Tile",
            subtitle = "Fast 4-item circular launcher grid in workspace",
            checked = settings.showMyAppsWidget,
            onCheckedChange = { viewModel.setShowMyAppsWidget(it) },
            testTag = "switch_widget_my_apps"
        )

        CustomizationSwitchRow(
            title = "Calendar Events Widget",
            subtitle = "Upcoming schedule and agendas from your device",
            checked = settings.showCalendarWidget,
            onCheckedChange = { viewModel.setShowCalendarWidget(it) },
            testTag = "switch_widget_calendar"
        )

        CustomizationSwitchRow(
            title = "Quick Notes Widget",
            subtitle = "Instant mindful scratchpad and thoughts",
            checked = settings.showNotesWidget,
            onCheckedChange = { viewModel.setShowNotesWidget(it) },
            testTag = "switch_widget_notes"
        )

        CustomizationSwitchRow(
            title = "Daily Stoic & Zen Quotes",
            subtitle = "Inspiring reflection quotes that rotate on tap",
            checked = settings.showQuoteWidget,
            onCheckedChange = { viewModel.setShowQuoteWidget(it) },
            testTag = "switch_widget_quote"
        )
    }
}

@Composable
private fun CustomizationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
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
