package com.example

import com.example.data.model.AppInfo
import com.example.data.model.ScreenTimeStat
import com.example.data.model.ThemePreset
import com.example.data.repository.FocusState
import com.example.ui.components.ZenQuotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZenithLauncherUnitTest {

    @Test
    fun testAppInfoBudgetAndBreak() {
        val app = AppInfo(
            packageName = "com.test.app",
            label = "Test App",
            dailyBudgetMinutes = 30,
            usedTodayMinutes = 45,
            mindfulBreakUntil = System.currentTimeMillis() + 60000L
        )

        assertTrue(app.isBudgetExceeded)
        assertTrue(app.isBreakActive)
    }

    @Test
    fun testScreenTimeFormatting() {
        val stat = ScreenTimeStat(
            packageName = "com.social.app",
            appName = "Social App",
            timeForegroundMs = 3900000L // 65 mins -> 1h 5m
        )
        assertEquals("1h 5m", stat.getFormattedDuration())
    }

    @Test
    fun testFocusStateCalculations() {
        val state = FocusState(
            totalSeconds = 1500,
            remainingSeconds = 750
        )
        assertEquals(0.5f, state.progress, 0.01f)
    }

    @Test
    fun testZenQuotesIntegrity() {
        assertFalse(ZenQuotes.quotes.isEmpty())
        val first = ZenQuotes.getQuote(0)
        assertTrue(first.first.isNotBlank())
        assertTrue(first.second.isNotBlank())
    }

    @Test
    fun testThemePresets() {
        assertEquals(5, ThemePreset.entries.size)
    }
}
