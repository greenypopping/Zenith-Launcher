package com.example.data.repository

import com.example.data.local.LauncherPreferencesDao
import com.example.data.local.LauncherPreferencesEntity
import com.example.data.model.ClockStyle
import com.example.data.model.HomeLayoutMode
import com.example.data.model.ThemePreset
import com.example.data.model.WallpaperPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LauncherSettings(
    val themePreset: ThemePreset = ThemePreset.YIN_YANG_MONO,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.STARRY_MOUNTAIN,
    val layoutMode: HomeLayoutMode = HomeLayoutMode.MINIMAL_TEXT,
    val clockStyle: ClockStyle = ClockStyle.SCRIPT_DAY,
    val showSearchBar: Boolean = true,
    val showDock: Boolean = true,
    val grayscaleIcons: Boolean = true,
    val showAppIcons: Boolean = true,
    val mindfulPauseDefaultSeconds: Int = 10,
    val quoteIndex: Int = 0,
    val userName: String = "Guest",
    val showWeatherWidget: Boolean = true,
    val showFocusWidget: Boolean = true,
    val showMyAppsWidget: Boolean = true,
    val showCalendarWidget: Boolean = true,
    val showNotesWidget: Boolean = true,
    val showQuoteWidget: Boolean = true,
    val maxPinnedApps: Int = 6
)

class PreferencesRepository(
    private val preferencesDao: LauncherPreferencesDao,
    private val scope: CoroutineScope
) {
    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            preferencesDao.getAllPreferences().collect { list ->
                val map = list.associate { it.key to it.value }
                val theme = map["theme_preset"]?.let {
                    try { ThemePreset.valueOf(it) } catch (_: Exception) { null }
                } ?: ThemePreset.YIN_YANG_MONO

                val wallpaper = map["wallpaper_preset"]?.let {
                    try { WallpaperPreset.valueOf(it) } catch (_: Exception) { null }
                } ?: WallpaperPreset.STARRY_MOUNTAIN

                val layout = map["layout_mode"]?.let {
                    try { HomeLayoutMode.valueOf(it) } catch (_: Exception) { null }
                } ?: HomeLayoutMode.MINIMAL_TEXT

                val clock = map["clock_style"]?.let {
                    try { ClockStyle.valueOf(it) } catch (_: Exception) { null }
                } ?: ClockStyle.SCRIPT_DAY

                val search = map["show_search_bar"]?.toBooleanStrictOrNull() ?: true
                val dock = map["show_dock"]?.toBooleanStrictOrNull() ?: true
                val grayscale = map["grayscale_icons"]?.toBooleanStrictOrNull() ?: true
                val icons = map["show_app_icons"]?.toBooleanStrictOrNull() ?: true
                val pauseSec = map["mindful_pause_seconds"]?.toIntOrNull() ?: 10
                val qIdx = map["quote_index"]?.toIntOrNull() ?: 0
                val user = map["user_name"] ?: "Guest"
                val showWeather = map["show_weather_widget"]?.toBooleanStrictOrNull() ?: true
                val showFocus = map["show_focus_widget"]?.toBooleanStrictOrNull() ?: true
                val showMyApps = map["show_my_apps_widget"]?.toBooleanStrictOrNull() ?: true
                val showCal = map["show_calendar_widget"]?.toBooleanStrictOrNull() ?: true
                val showNotes = map["show_notes_widget"]?.toBooleanStrictOrNull() ?: true
                val showQuote = map["show_quote_widget"]?.toBooleanStrictOrNull() ?: true
                val maxApps = map["max_pinned_apps"]?.toIntOrNull() ?: 6

                _settings.value = LauncherSettings(
                    themePreset = theme,
                    wallpaperPreset = wallpaper,
                    layoutMode = layout,
                    clockStyle = clock,
                    showSearchBar = search,
                    showDock = dock,
                    grayscaleIcons = grayscale,
                    showAppIcons = icons,
                    mindfulPauseDefaultSeconds = pauseSec,
                    quoteIndex = qIdx,
                    userName = user,
                    showWeatherWidget = showWeather,
                    showFocusWidget = showFocus,
                    showMyAppsWidget = showMyApps,
                    showCalendarWidget = showCal,
                    showNotesWidget = showNotes,
                    showQuoteWidget = showQuote,
                    maxPinnedApps = maxApps
                )
            }
        }
    }

    fun setThemePreset(preset: ThemePreset) = savePref("theme_preset", preset.name)
    fun setWallpaperPreset(preset: WallpaperPreset) = savePref("wallpaper_preset", preset.name)
    fun setLayoutMode(mode: HomeLayoutMode) = savePref("layout_mode", mode.name)
    fun setClockStyle(style: ClockStyle) = savePref("clock_style", style.name)
    fun setShowSearchBar(show: Boolean) = savePref("show_search_bar", show.toString())
    fun setShowDock(show: Boolean) = savePref("show_dock", show.toString())
    fun setGrayscaleIcons(enabled: Boolean) = savePref("grayscale_icons", enabled.toString())
    fun setShowAppIcons(show: Boolean) = savePref("show_app_icons", show.toString())
    fun setMindfulPauseDefaultSeconds(seconds: Int) = savePref("mindful_pause_seconds", seconds.toString())
    fun setUserName(name: String) = savePref("user_name", name)
    fun setShowWeatherWidget(show: Boolean) = savePref("show_weather_widget", show.toString())
    fun setShowFocusWidget(show: Boolean) = savePref("show_focus_widget", show.toString())
    fun setShowMyAppsWidget(show: Boolean) = savePref("show_my_apps_widget", show.toString())
    fun setShowCalendarWidget(show: Boolean) = savePref("show_calendar_widget", show.toString())
    fun setShowNotesWidget(show: Boolean) = savePref("show_notes_widget", show.toString())
    fun setShowQuoteWidget(show: Boolean) = savePref("show_quote_widget", show.toString())
    fun setMaxPinnedApps(count: Int) = savePref("max_pinned_apps", count.toString())

    fun nextQuote() {
        val current = _settings.value.quoteIndex
        savePref("quote_index", (current + 1).toString())
    }

    private fun savePref(key: String, value: String) {
        scope.launch(Dispatchers.IO) {
            preferencesDao.setPreference(LauncherPreferencesEntity(key, value))
        }
    }
}
