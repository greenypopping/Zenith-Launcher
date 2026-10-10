package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true,
    val tag: String = "Focus"
)

@Entity(tableName = "app_restrictions")
data class AppRestrictionEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val mindfulPauseEnabled: Boolean = false,
    val pauseDurationSeconds: Int = 10,
    val breathingPattern: String = "4-4-4",
    val dailyBudgetMinutes: Int = 0, // 0 means unbudgeted
    val mindfulBreakUntil: Long = 0L, // timestamp epoch ms
    val breakMotivation: String = "",
    val isWhitelistedInFocus: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false
)

@Entity(tableName = "launcher_preferences")
data class LauncherPreferencesEntity(
    @PrimaryKey val key: String,
    val value: String
)
