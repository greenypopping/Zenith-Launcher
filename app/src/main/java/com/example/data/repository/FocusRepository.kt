package com.example.data.repository

import com.example.data.local.FocusSessionDao
import com.example.data.local.FocusSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FocusState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isBreak: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val completedIntervals: Int = 0,
    val workDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val intervalsBeforeLongBreak: Int = 4,
    val tag: String = "Focus"
) {
    val progress: Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds else 0f
}

class FocusRepository(
    private val focusSessionDao: FocusSessionDao,
    private val scope: CoroutineScope
) {
    private val _focusState = MutableStateFlow(FocusState())
    val focusState: StateFlow<FocusState> = _focusState.asStateFlow()

    val history: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()

    private var timerJob: Job? = null
    private var sessionEndTimestamp: Long = 0L

    fun configure(workMinutes: Int, breakMinutes: Int, tag: String = "Focus") {
        if (!_focusState.value.isRunning) {
            val totalSec = workMinutes * 60
            _focusState.value = _focusState.value.copy(
                workDurationMinutes = workMinutes,
                breakDurationMinutes = breakMinutes,
                totalSeconds = totalSec,
                remainingSeconds = totalSec,
                tag = tag
            )
        }
    }

    fun startSession() {
        val current = _focusState.value
        val duration = if (current.isBreak) current.breakDurationMinutes * 60 else current.workDurationMinutes * 60
        sessionEndTimestamp = System.currentTimeMillis() + duration * 1000L

        _focusState.value = current.copy(
            isRunning = true,
            isPaused = false,
            totalSeconds = duration,
            remainingSeconds = duration
        )

        startTicker()
    }

    fun pauseSession() {
        timerJob?.cancel()
        val current = _focusState.value
        _focusState.value = current.copy(isPaused = true)
    }

    fun resumeSession() {
        val current = _focusState.value
        sessionEndTimestamp = System.currentTimeMillis() + current.remainingSeconds * 1000L
        _focusState.value = current.copy(isPaused = false)
        startTicker()
    }

    fun stopSession() {
        timerJob?.cancel()
        val current = _focusState.value
        val initialSeconds = current.workDurationMinutes * 60
        _focusState.value = current.copy(
            isRunning = false,
            isPaused = false,
            isBreak = false,
            totalSeconds = initialSeconds,
            remainingSeconds = initialSeconds
        )
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = ((sessionEndTimestamp - now) / 1000L).toInt().coerceAtLeast(0)

                _focusState.value = _focusState.value.copy(remainingSeconds = remaining)

                if (remaining <= 0) {
                    onIntervalCompleted()
                    break
                }
                delay(500)
            }
        }
    }

    private suspend fun onIntervalCompleted() {
        val current = _focusState.value
        if (!current.isBreak) {
            // Completed a work interval
            scope.launch(Dispatchers.IO) {
                focusSessionDao.insertSession(
                    FocusSessionEntity(
                        durationMinutes = current.workDurationMinutes,
                        timestamp = System.currentTimeMillis(),
                        completed = true,
                        tag = current.tag
                    )
                )
            }

            val newCount = current.completedIntervals + 1
            val nextBreakMinutes = if (newCount % current.intervalsBeforeLongBreak == 0) {
                current.breakDurationMinutes * 3 // Long break
            } else {
                current.breakDurationMinutes
            }

            val breakSeconds = nextBreakMinutes * 60
            sessionEndTimestamp = System.currentTimeMillis() + breakSeconds * 1000L
            _focusState.value = current.copy(
                isBreak = true,
                completedIntervals = newCount,
                totalSeconds = breakSeconds,
                remainingSeconds = breakSeconds
            )
            startTicker()
        } else {
            // Completed a break interval, return to work
            val workSeconds = current.workDurationMinutes * 60
            _focusState.value = current.copy(
                isBreak = false,
                isRunning = false, // pause before starting next work session
                totalSeconds = workSeconds,
                remainingSeconds = workSeconds
            )
        }
    }
}
