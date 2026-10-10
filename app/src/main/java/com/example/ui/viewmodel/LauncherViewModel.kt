package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NoteEntity
import com.example.data.local.TaskEntity
import com.example.data.local.ZenithDatabase
import com.example.data.model.AppInfo
import com.example.data.model.CalendarEvent
import com.example.data.model.ClockStyle
import com.example.data.model.HomeLayoutMode
import com.example.data.model.ScreenTimeStat
import com.example.data.model.ThemePreset
import com.example.data.repository.AppRepository
import com.example.data.repository.FocusRepository
import com.example.data.repository.FocusState
import com.example.data.repository.LauncherSettings
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.ProductivityRepository
import com.example.data.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZenithDatabase.getInstance(application)
    val appRepository = AppRepository(application, db.appRestrictionDao())
    val usageStatsRepository = UsageStatsRepository(application)
    val productivityRepository = ProductivityRepository(application, db.noteDao(), db.taskDao())
    val focusRepository = FocusRepository(db.focusSessionDao(), viewModelScope)
    val preferencesRepository = PreferencesRepository(db.launcherPreferencesDao(), viewModelScope)

    // Settings
    val settings: StateFlow<LauncherSettings> = preferencesRepository.settings

    // All installed apps with restriction metadata
    val allApps: StateFlow<List<AppInfo>> = appRepository.getApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query in drawer
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered apps for drawer
    val filteredDrawerApps: StateFlow<List<AppInfo>> = combine(allApps, searchQuery) { apps, query ->
        val visible = apps.filter { !it.isHidden }
        if (query.isBlank()) {
            visible.sortedBy { it.label.lowercase() }
        } else {
            visible.filter { it.label.contains(query, ignoreCase = true) }
                .sortedBy { it.label.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorite apps for Home screen dock/shortcuts
    val favoriteApps: StateFlow<List<AppInfo>> = allApps.map { apps ->
        apps.filter { it.isFavorite && !it.isHidden }.sortedBy { it.label.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & Modal states
    val activeTab = MutableStateFlow(0) // 0: Home, 1: My Space, 2: Mind Space
    val isAppDrawerOpen = MutableStateFlow(false)
    val isSettingsOpen = MutableStateFlow(false)

    // Interception modals
    val activePauseApp = MutableStateFlow<AppInfo?>(null)
    val activeBreakApp = MutableStateFlow<AppInfo?>(null)
    val activeBudgetApp = MutableStateFlow<AppInfo?>(null)
    val activeFocusBlockApp = MutableStateFlow<AppInfo?>(null)

    // Notes and Tasks
    val notes: StateFlow<List<NoteEntity>> = productivityRepository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = productivityRepository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar
    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    // Usage Stats
    private val _usageStats = MutableStateFlow<List<ScreenTimeStat>>(emptyList())
    val usageStats: StateFlow<List<ScreenTimeStat>> = _usageStats.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    // Focus State
    val focusState: StateFlow<FocusState> = focusRepository.focusState
    val focusHistory = focusRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshUsageStats()
        refreshCalendarEvents()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun refreshUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPerm = usageStatsRepository.hasUsagePermission()
            _hasUsagePermission.value = hasPerm
            if (hasPerm) {
                val stats = usageStatsRepository.getTodayScreenTimeStats()
                _usageStats.value = stats
            } else {
                _usageStats.value = emptyList()
            }
        }
    }

    fun refreshCalendarEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            if (productivityRepository.hasCalendarPermission()) {
                val events = productivityRepository.getUpcomingCalendarEvents()
                _calendarEvents.value = events
            } else {
                _calendarEvents.value = emptyList()
            }
        }
    }

    // App Launch Interception
    fun onAppClicked(app: AppInfo) {
        // 1. Long term Mindful Break restriction check
        if (app.isBreakActive) {
            activeBreakApp.value = app
            return
        }

        // 2. Active Focus session restriction check
        val focus = focusState.value
        if (focus.isRunning && !focus.isBreak && !app.isWhitelistedInFocus) {
            activeFocusBlockApp.value = app
            return
        }

        // 3. Daily usage budget check
        if (app.isBudgetExceeded) {
            activeBudgetApp.value = app
            return
        }

        // 4. Mindful Pause check
        if (app.mindfulPauseEnabled) {
            activePauseApp.value = app
            return
        }

        // 5. Normal launch
        launchApplication(app)
    }

    fun launchApplication(app: AppInfo) {
        viewModelScope.launch {
            appRepository.launchApp(app.packageName)
            // Dismiss drawer
            isAppDrawerOpen.value = false
            dismissAllModals()
        }
    }

    fun dismissAllModals() {
        activePauseApp.value = null
        activeBreakApp.value = null
        activeBudgetApp.value = null
        activeFocusBlockApp.value = null
    }

    // Note actions
    fun saveNote(id: Long = 0, title: String, content: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            productivityRepository.saveNote(id, title, content, isPinned)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            productivityRepository.deleteNote(note)
        }
    }

    // Task actions
    fun addTask(title: String, dueDate: Long? = null, priority: Int = 1) {
        viewModelScope.launch {
            productivityRepository.addTask(title, dueDate, priority)
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            productivityRepository.toggleTaskComplete(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            productivityRepository.deleteTask(task)
        }
    }

    // Restriction & App customizations
    fun toggleFavorite(app: AppInfo) {
        viewModelScope.launch {
            appRepository.toggleFavorite(app)
        }
    }

    fun toggleHidden(app: AppInfo) {
        viewModelScope.launch {
            appRepository.toggleHidden(app)
        }
    }

    fun setMindfulPause(app: AppInfo, enabled: Boolean, durationSeconds: Int = 10) {
        viewModelScope.launch {
            appRepository.setMindfulPause(app, enabled, durationSeconds)
        }
    }

    fun setDailyBudget(app: AppInfo, minutes: Int) {
        viewModelScope.launch {
            appRepository.setDailyBudget(app, minutes)
        }
    }

    fun setMindfulBreak(app: AppInfo, durationHours: Int, motivation: String) {
        viewModelScope.launch {
            val until = if (durationHours > 0) {
                System.currentTimeMillis() + (durationHours * 3600000L)
            } else 0L
            appRepository.setMindfulBreak(app, until, motivation)
        }
    }

    fun setFocusWhitelist(app: AppInfo, whitelisted: Boolean) {
        viewModelScope.launch {
            appRepository.setFocusWhitelist(app, whitelisted)
        }
    }

    // Settings actions
    fun setThemePreset(preset: ThemePreset) = preferencesRepository.setThemePreset(preset)
    fun setWallpaperPreset(preset: com.example.data.model.WallpaperPreset) = preferencesRepository.setWallpaperPreset(preset)
    fun setLayoutMode(mode: HomeLayoutMode) = preferencesRepository.setLayoutMode(mode)
    fun setClockStyle(style: ClockStyle) = preferencesRepository.setClockStyle(style)
    fun setShowSearchBar(show: Boolean) = preferencesRepository.setShowSearchBar(show)
    fun setShowDock(show: Boolean) = preferencesRepository.setShowDock(show)
    fun setShowAppIcons(show: Boolean) = preferencesRepository.setShowAppIcons(show)
    fun setGrayscaleIcons(enabled: Boolean) = preferencesRepository.setGrayscaleIcons(enabled)
    fun setUserName(name: String) = preferencesRepository.setUserName(name)
    fun setShowWeatherWidget(show: Boolean) = preferencesRepository.setShowWeatherWidget(show)
    fun setShowFocusWidget(show: Boolean) = preferencesRepository.setShowFocusWidget(show)
    fun setShowMyAppsWidget(show: Boolean) = preferencesRepository.setShowMyAppsWidget(show)
    fun setShowCalendarWidget(show: Boolean) = preferencesRepository.setShowCalendarWidget(show)
    fun setShowNotesWidget(show: Boolean) = preferencesRepository.setShowNotesWidget(show)
    fun setShowQuoteWidget(show: Boolean) = preferencesRepository.setShowQuoteWidget(show)
    fun setMaxPinnedApps(count: Int) = preferencesRepository.setMaxPinnedApps(count)
    fun nextQuote() = preferencesRepository.nextQuote()
}
