package com.example.ui.screens.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppInfo
import com.example.ui.components.AlphabetScrubber
import com.example.ui.components.ZenIcons
import com.example.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerSheet(
    viewModel: LauncherViewModel,
    apps: List<AppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val settings by viewModel.settings.collectAsState()

    var selectedAppForOptions by remember { mutableStateOf<AppInfo?>(null) }
    var activeScrubLetter by remember { mutableStateOf('A') }

    // Group apps alphabetically
    val groupedApps = remember(apps) {
        apps.groupBy { app ->
            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            if (firstChar in 'A'..'Z') firstChar else '#'
        }.toSortedMap()
    }

    // Flatten for fast indexing
    val flatItems = remember(groupedApps) {
        val list = mutableListOf<DrawerItem>()
        groupedApps.forEach { (char, appList) ->
            list.add(DrawerItem.Header(char))
            appList.forEach { list.add(DrawerItem.App(it)) }
        }
        list
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_sheet")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top Search Bar (matching Screenshot 7: "Search" + filter icon + "X")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = ZenIcons.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Grayscale toggle icon
                                IconButton(
                                    onClick = {
                                        viewModel.preferencesRepository.setGrayscaleIcons(!settings.grayscaleIcons)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = "Toggle Grayscale",
                                        tint = if (settings.grayscaleIcons) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(onClick = onClose) {
                                    Icon(
                                        imageVector = ZenIcons.Close,
                                        contentDescription = "Close Drawer",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_search_input")
                    )
                }

                // App List with curved Alphabet Scrubber
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Apps LazyColumn
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_apps_list")
                    ) {
                        itemsIndexed(flatItems, key = { index, item ->
                            when (item) {
                                is DrawerItem.Header -> "header_${item.char}"
                                is DrawerItem.App -> item.app.packageName
                            }
                        }) { index, item ->
                            when (item) {
                                is DrawerItem.Header -> {
                                    Text(
                                        text = item.char.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 12.dp)
                                            .testTag("section_${item.char}")
                                    )
                                }
                                is DrawerItem.App -> {
                                    DrawerAppRowItem(
                                        app = item.app,
                                        grayscale = settings.grayscaleIcons,
                                        onClick = { viewModel.onAppClicked(item.app) },
                                        onLongClick = { selectedAppForOptions = item.app }
                                    )
                                }
                            }
                        }
                    }

                    // Signature Yin Yang Curved Alphabet Scrubber (matching Screenshot 7)
                    AlphabetScrubber(
                        selectedLetter = activeScrubLetter,
                        onLetterSelected = { char ->
                            activeScrubLetter = char
                            val targetIndex = flatItems.indexOfFirst {
                                it is DrawerItem.Header && it.char == char
                            }
                            if (targetIndex >= 0) {
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        }
                    )
                }
            }

            // Floating Keyboard Icon Button (bottom right, matching Screenshot 7)
            FloatingActionButton(
                onClick = { /* Keyboard is handled via search bar focus */ },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 44.dp, bottom = 24.dp)
                    .size(46.dp)
                    .testTag("keyboard_toggle_fab")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Keyboard,
                    contentDescription = "Keyboard",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // App Options Sheet
    selectedAppForOptions?.let { app ->
        AppOptionsSheet(
            app = app,
            viewModel = viewModel,
            onDismiss = { selectedAppForOptions = null }
        )
    }
}

sealed class DrawerItem {
    data class Header(val char: Char) : DrawerItem()
    data class App(val app: AppInfo) : DrawerItem()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerAppRowItem(
    app: AppInfo,
    grayscale: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("drawer_app_${app.packageName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cachedBitmap = remember(app.packageName, app.icon) {
                com.example.ui.util.AppIconCache.getIcon(app.packageName, app.icon, sizePx = 84)
            }
            if (cachedBitmap != null) {
                Image(
                    bitmap = cachedBitmap,
                    contentDescription = app.label,
                    colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (app.mindfulPauseEnabled) {
                Icon(
                    imageVector = ZenIcons.Mindful,
                    contentDescription = "Mindful Pause",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOptionsSheet(
    app: AppInfo,
    viewModel: LauncherViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("app_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Action 1: Favorite Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = {
                    viewModel.toggleFavorite(app)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = if (app.isFavorite) ZenIcons.Star else ZenIcons.StarOutline,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action 2: Mindful Pause Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mindful Pause",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Breathing interruption before launching",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                FilterChip(
                    selected = app.mindfulPauseEnabled,
                    onClick = {
                        viewModel.setMindfulPause(app, !app.mindfulPauseEnabled, 10)
                    },
                    label = { Text(if (app.mindfulPauseEnabled) "Enabled" else "Disabled") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Action 3: Daily Budget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Time Budget",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (app.dailyBudgetMinutes > 0) "${app.dailyBudgetMinutes}m allowance" else "No limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0, 30, 60).forEach { minutes ->
                        FilterChip(
                            selected = app.dailyBudgetMinutes == minutes,
                            onClick = { viewModel.setDailyBudget(app, minutes) },
                            label = { Text(if (minutes == 0) "Off" else "${minutes}m") }
                        )
                    }
                }
            }

            // Action 4: Mindful Break (Digital Detox)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mindful Break",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (app.isBreakActive) "Restricted until later" else "Restrict access for a period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (app.isBreakActive) {
                    TextButton(onClick = { viewModel.setMindfulBreak(app, 0, "") }) {
                        Text("Lift Break")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                viewModel.setMindfulBreak(app, 24, "Digital detox")
                                onDismiss()
                            },
                            label = { Text("1 Day") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                viewModel.setMindfulBreak(app, 72, "Digital detox")
                                onDismiss()
                            },
                            label = { Text("3 Days") }
                        )
                    }
                }
            }

            // Action 5: Focus Mode Whitelist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Permit in Focus Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Allowed during active focus sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                FilterChip(
                    selected = app.isWhitelistedInFocus,
                    onClick = {
                        viewModel.setFocusWhitelist(app, !app.isWhitelistedInFocus)
                    },
                    label = { Text(if (app.isWhitelistedInFocus) "Allowed" else "Restricted") }
                )
            }

            // Action 6: Hide App
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hide Application",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = {
                    viewModel.toggleHidden(app)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = ZenIcons.VisibilityOff,
                        contentDescription = "Hide App",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
