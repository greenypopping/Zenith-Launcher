package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        AppRestrictionEntity::class,
        LauncherPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZenithDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun appRestrictionDao(): AppRestrictionDao
    abstract fun launcherPreferencesDao(): LauncherPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: ZenithDatabase? = null

        fun getInstance(context: Context): ZenithDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZenithDatabase::class.java,
                    "zenith_launcher.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
