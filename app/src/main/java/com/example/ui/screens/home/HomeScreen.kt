package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppInfo
import com.example.data.model.HomeLayoutMode
import com.example.ui.components.ZenClock
import com.example.ui.components.ZenIcons
import com.example.ui.components.ZenWallpaper
import com.example.ui.viewmodel.LauncherViewModel

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    favoriteApps: List<AppInfo>,
    allApps: List<AppInfo>,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCustomization: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_root")
            .combinedClickable(
                onClick = { /* Home blank tap */ },
                onLongClick = onOpenCustomization
            )
    ) {
        // Aesthetic Procedural Wallpaper
        ZenWallpaper(preset = settings.wallpaperPreset)

        // Subtle dark vignette overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Clean subtle controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings action button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = ZenIcons.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Wallpaper switcher shortcut
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val nextPreset = com.example.data.model.WallpaperPreset.entries.let { list ->
                                val curIdx = list.indexOf(settings.wallpaperPreset)
                                list[(curIdx + 1) % list.size]
                            }
                            viewModel.preferencesRepository.setWallpaperPreset(nextPreset)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ZenIcons.Mindful,
                            contentDescription = "Theme",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = settings.wallpaperPreset.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Center: Clock & Pinned Apps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Zen Clock (Script, Digital, Dial, or Analog)
                ZenClock(
                    style = settings.clockStyle,
                    textColor = Color.White,
                    secondaryColor = Color.White.copy(alpha = 0.8f),
                    onClockClick = {
                        val nextStyle = com.example.data.model.ClockStyle.entries.let { list ->
                            val cur = list.indexOf(settings.clockStyle)
                            list[(cur + 1) % list.size]
                        }
                        viewModel.preferencesRepository.setClockStyle(nextStyle)
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Pinned Applications (Minimal Text List OR Classic Icons based on settings.layoutMode)
                val appsToShow = if (favoriteApps.isNotEmpty()) {
                    favoriteApps.take(settings.maxPinnedApps)
                } else {
                    allApps.take(settings.maxPinnedApps.coerceAtMost(4))
                }

                if (appsToShow.isNotEmpty()) {
                    if (settings.layoutMode == HomeLayoutMode.CLASSIC_ICONS) {
                        // Classic Mode: App Icons with subtle labels underneath
                        // Classic Mode: App Icons with subtle labels underneath
                        val rows = appsToShow.chunked(4)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rows.forEach { rowApps ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowApps.forEach { app ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.onAppClicked(app) }
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                                .testTag("pinned_app_${app.packageName}")
                                        ) {
                                            val cachedBitmap = remember(app.packageName, app.icon) {
                                                com.example.ui.util.AppIconCache.getIcon(app.packageName, app.icon, sizePx = 96)
                                            }
                                            if (cachedBitmap != null) {
                                                Image(
                                                    bitmap = cachedBitmap,
                                                    contentDescription = app.label,
                                                    colorFilter = if (settings.grayscaleIcons) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = app.label.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = app.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = Color.White.copy(alpha = 0.85f),
                                                maxLines = 1,
                                                softWrap = true,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Minimal Text List (matching signature Yin Yang Minimal layout)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            appsToShow.forEach { app ->
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = 0.5.sp,
                                        fontSize = 17.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.92f),
                                    textAlign = TextAlign.Center,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.onAppClicked(app) }
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .testTag("pinned_app_${app.packageName}")
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Section: Spotlight Search Bar & 4-App Glass Dock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spotlight Search Bar (matching Screenshots 2 & 8)
                if (settings.showSearchBar) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onOpenDrawer() }
                            .testTag("spotlight_search_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ZenIcons.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Spotlight search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 4-App Translucent Glass Dock (matching Screenshots 2 & 8)
                if (settings.showDock) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .testTag("home_glass_dock"),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DockIconTile(
                            icon = Icons.Outlined.Call,
                            contentDescription = "Phone",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        )
                        DockIconTile(
                            icon = Icons.AutoMirrored.Outlined.Message,
                            contentDescription = "Messages",
                            onClick = {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    onOpenDrawer()
                                }
                            }
                        )
                        DockIconTile(
                            icon = Icons.Outlined.Language,
                            contentDescription = "Browser",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    onOpenDrawer()
                                }
                            }
                        )
                        DockIconTile(
                            icon = Icons.Outlined.CameraAlt,
                            contentDescription = "Camera",
                            onClick = {
                                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {
                                    onOpenDrawer()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DockIconTile(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("dock_icon_${contentDescription.lowercase()}")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
