package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.model.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class ProductivityRepository(
    private val context: Context,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao
) {
    val notes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val tasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun saveNote(id: Long = 0, title: String, content: String, isPinned: Boolean = false): Long =
        withContext(Dispatchers.IO) {
            noteDao.insertOrUpdateNote(
                NoteEntity(
                    id = id,
                    title = title.trim(),
                    content = content.trim(),
                    updatedAt = System.currentTimeMillis(),
                    isPinned = isPinned
                )
            )
        }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun addTask(title: String, dueDate: Long? = null, priority: Int = 1): Long =
        withContext(Dispatchers.IO) {
            taskDao.insertOrUpdateTask(
                TaskEntity(
                    title = title.trim(),
                    dueDate = dueDate,
                    priority = priority,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun toggleTaskComplete(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getUpcomingCalendarEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) return@withContext emptyList()

        val events = mutableListOf<CalendarEvent>()
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startCal.timeInMillis)
        ContentUris.appendId(builder, endCal.timeInMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.let {
                val idIdx = it.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginIdx = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = it.getColumnIndex(CalendarContract.Instances.END)
                val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                val locIdx = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Untitled Event" else "Event"
                    val begin = if (beginIdx >= 0) it.getLong(beginIdx) else 0L
                    val end = if (endIdx >= 0) it.getLong(endIdx) else 0L
                    val allDay = if (allDayIdx >= 0) it.getInt(allDayIdx) == 1 else false
                    val loc = if (locIdx >= 0) it.getString(locIdx) ?: "" else ""

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startMillis = begin,
                            endMillis = end,
                            isAllDay = allDay,
                            location = loc
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        } finally {
            cursor?.close()
        }

        events
    }

    fun openCalendarApp() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to main launcher intent for calendar
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
            launchIntent?.let { context.startActivity(it) }
        }
    }
}
