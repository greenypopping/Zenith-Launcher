package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: NoteEntity): Long

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE completed = 1 AND timestamp >= :sinceEpoch")
    suspend fun getTotalFocusMinutesSince(sinceEpoch: Long): Int?
}

@Dao
interface AppRestrictionDao {
    @Query("SELECT * FROM app_restrictions")
    fun getAllRestrictions(): Flow<List<AppRestrictionEntity>>

    @Query("SELECT * FROM app_restrictions WHERE packageName = :packageName LIMIT 1")
    suspend fun getRestriction(packageName: String): AppRestrictionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRestriction(restriction: AppRestrictionEntity)

    @Query("UPDATE app_restrictions SET isFavorite = :favorite WHERE packageName = :pkg")
    suspend fun setFavorite(pkg: String, favorite: Boolean)

    @Query("UPDATE app_restrictions SET isHidden = :hidden WHERE packageName = :pkg")
    suspend fun setHidden(pkg: String, hidden: Boolean)

    @Query("UPDATE app_restrictions SET mindfulPauseEnabled = :enabled, pauseDurationSeconds = :duration WHERE packageName = :pkg")
    suspend fun setMindfulPause(pkg: String, enabled: Boolean, duration: Int)

    @Query("UPDATE app_restrictions SET dailyBudgetMinutes = :budget WHERE packageName = :pkg")
    suspend fun setDailyBudget(pkg: String, budget: Int)

    @Query("UPDATE app_restrictions SET mindfulBreakUntil = :untilTime, breakMotivation = :motivation WHERE packageName = :pkg")
    suspend fun setMindfulBreak(pkg: String, untilTime: Long, motivation: String)

    @Query("UPDATE app_restrictions SET isWhitelistedInFocus = :whitelisted WHERE packageName = :pkg")
    suspend fun setFocusWhitelist(pkg: String, whitelisted: Boolean)
}

@Dao
interface LauncherPreferencesDao {
    @Query("SELECT value FROM launcher_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): String?

    @Query("SELECT * FROM launcher_preferences")
    fun getAllPreferences(): Flow<List<LauncherPreferencesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: LauncherPreferencesEntity)
}
