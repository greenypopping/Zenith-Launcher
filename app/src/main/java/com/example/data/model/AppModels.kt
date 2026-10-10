package com.example.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val mindfulPauseEnabled: Boolean = false,
    val pauseDurationSeconds: Int = 10,
    val dailyBudgetMinutes: Int = 0,
    val usedTodayMinutes: Int = 0,
    val launchCountToday: Int = 0,
    val mindfulBreakUntil: Long = 0L,
    val breakMotivation: String = "",
    val isWhitelistedInFocus: Boolean = false
) {
    val isBreakActive: Boolean
        get() = mindfulBreakUntil > System.currentTimeMillis()

    val isBudgetExceeded: Boolean
        get() = dailyBudgetMinutes > 0 && usedTodayMinutes >= dailyBudgetMinutes
}

data class ScreenTimeStat(
    val packageName: String,
    val appName: String,
    val timeForegroundMs: Long,
    val launchCount: Int = 0,
    val percentageOfTotal: Float = 0f
) {
    val durationMinutes: Int
        get() = (timeForegroundMs / 60000L).toInt()

    fun getFormattedDuration(): String {
        val totalMinutes = durationMinutes
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            mins > 0 -> "${mins}m"
            else -> "< 1m"
        }
    }
}

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean = false,
    val location: String = ""
)

data class HourlyForecast(
    val time: String,
    val temp: Int,
    val condition: String
)

data class WeatherInfo(
    val currentTemp: Int = 26,
    val highTemp: Int = 28,
    val lowTemp: Int = 21,
    val condition: String = "Partly Cloudy",
    val location: String = "Bengaluru",
    val hourly: List<HourlyForecast> = listOf(
        HourlyForecast("6 pm", 24, "Partly Cloudy"),
        HourlyForecast("7 pm", 23, "Scattered Clouds"),
        HourlyForecast("8 pm", 23, "Light Rain"),
        HourlyForecast("9 pm", 23, "Light Rain"),
        HourlyForecast("10 pm", 23, "Overcast"),
        HourlyForecast("11 pm", 22, "Clear")
    )
)

enum class ThemePreset(val displayName: String) {
    YIN_YANG_MONO("Yin Yang"),
    OBSIDIAN_DARK("Obsidian"),
    PAPER_LIGHT("Paper"),
    WARM_SAND("Sand"),
    ZEN_SAGE("Sage")
}

enum class HomeLayoutMode(val displayName: String) {
    MINIMAL_TEXT("Minimal Text"),
    CLASSIC_ICONS("Classic Icons")
}

enum class ClockStyle(val displayName: String) {
    SCRIPT_DAY("Cursive Script"),
    MINIMAL_DIGITAL("Modern Digital"),
    DIAL_CIRCLE("Circle Dial"),
    ANALOG_DIAL("Analog Dial")
}

enum class WallpaperPreset(val displayName: String) {
    STARRY_MOUNTAIN("Starry Peak"),
    LUSH_LEAVES("Botanical Forest"),
    DUSK_GRADIENT("Twilight Dusk"),
    SUNSET_CABIN("Sunset Ridge"),
    OLED_BLACK("OLED Black"),
    PAPER_WHITE("Clean Paper")
}
