package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.drawer.AppDrawerSheet
import com.example.ui.screens.focus.FocusModeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.mindspace.MindSpaceScreen
import com.example.ui.screens.myspace.MySpaceScreen
import com.example.ui.screens.pause.MindfulPauseScreen
import com.example.ui.screens.restrictions.MindfulBreakScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.ZenithLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            ZenithLauncherTheme(themePreset = settings.themePreset) {
                LauncherRootApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LauncherRootApp(viewModel: LauncherViewModel) {
    val isDrawerOpen by viewModel.isAppDrawerOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredDrawerApps.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    val activePauseApp by viewModel.activePauseApp.collectAsState()
    val activeBreakApp by viewModel.activeBreakApp.collectAsState()

    var isFocusScreenOpen by remember { mutableStateOf(false) }
    var isCustomizationSheetOpen by remember { mutableStateOf(false) }

    // Interception Screen 1: Mindful Pause
    activePauseApp?.let { app ->
        MindfulPauseScreen(
            app = app,
            onProceed = { viewModel.launchApplication(app) },
            onCancel = { viewModel.dismissAllModals() }
        )
        return
    }

    // Interception Screen 2: Mindful Break (Lock)
    activeBreakApp?.let { app ->
        MindfulBreakScreen(
            app = app,
            onReturnHome = { viewModel.dismissAllModals() },
            onEmergencyOverride = { viewModel.launchApplication(app) }
        )
        return
    }

    // Overlay Screen 3: Focus Mode Pomodoro
    if (isFocusScreenOpen) {
        FocusModeScreen(
            viewModel = viewModel,
            onBack = { isFocusScreenOpen = false }
        )
        return
    }

    // Overlay Screen 4: Settings
    if (isSettingsOpen) {
        SettingsScreen(
            viewModel = viewModel,
            onBack = { viewModel.isSettingsOpen.value = false }
        )
        return
    }

    // 3-Page Horizontal Pager matching Yin Yang Launcher architecture (Screenshot 1):
    // Page 0: My Space (swipe right from Home)
    // Page 1: Home Screen (Center!)
    // Page 2: Mind Space (swipe left from Home)
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // BackHandler: Navigate back to Home if in another page or drawer
    if (isDrawerOpen) {
        BackHandler {
            viewModel.isAppDrawerOpen.value = false
        }
    } else if (isCustomizationSheetOpen) {
        BackHandler {
            isCustomizationSheetOpen = false
        }
    } else if (pagerState.currentPage != 1) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MySpaceScreen(viewModel = viewModel)
                    1 -> HomeScreen(
                        viewModel = viewModel,
                        favoriteApps = favoriteApps,
                        allApps = allApps,
                        onOpenDrawer = { viewModel.isAppDrawerOpen.value = true },
                        onOpenSettings = { viewModel.isSettingsOpen.value = true },
                        onOpenCustomization = { isCustomizationSheetOpen = true }
                    )
                    2 -> MindSpaceScreen(
                        viewModel = viewModel,
                        onOpenFocusMode = { isFocusScreenOpen = true }
                    )
                }
            }

            // Long Press Customization Bottom Sheet
            if (isCustomizationSheetOpen) {
                com.example.ui.screens.settings.LongPressCustomizationSheet(
                    viewModel = viewModel,
                    onOpenFullSettings = {
                        isCustomizationSheetOpen = false
                        viewModel.isSettingsOpen.value = true
                    },
                    onOpenFocusMode = {
                        isCustomizationSheetOpen = false
                        isFocusScreenOpen = true
                    },
                    onDismiss = { isCustomizationSheetOpen = false }
                )
            }

            // Minimalist Page Indicator Dots at Bottom
            if (!isDrawerOpen) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("pager_dots_indicator")
                ) {
                    repeat(3) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 6.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) Color.White.copy(alpha = 0.85f)
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            // Slide-in Application Drawer (matching Screenshot 7)
            AnimatedVisibility(
                visible = isDrawerOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppDrawerSheet(
                    viewModel = viewModel,
                    apps = filteredApps,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onClose = { viewModel.isAppDrawerOpen.value = false }
                )
            }
        }
    }
}
