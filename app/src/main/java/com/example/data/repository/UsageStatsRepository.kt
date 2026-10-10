package com.example.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.example.data.model.ScreenTimeStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class UsageStatsRepository(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    @Suppress("DEPRECATION")
    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    suspend fun getTodayScreenTimeStats(): List<ScreenTimeStat> = withContext(Dispatchers.IO) {
        if (!hasUsagePermission() || usageStatsManager == null) {
            return@withContext emptyList()
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return@withContext emptyList()

        val pm = context.packageManager
        val aggregated = mutableMapOf<String, Long>()

        for (usage in stats) {
            val time = usage.totalTimeInForeground
            if (time > 1000L) { // at least 1 second
                aggregated[usage.packageName] = (aggregated[usage.packageName] ?: 0L) + time
            }
        }

        val totalForegroundAll = aggregated.values.sum().coerceAtLeast(1L).toFloat()

        aggregated.entries
            .filter { it.key != context.packageName } // exclude self
            .map { entry ->
                val pkg = entry.key
                val label = try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: PackageManager.NameNotFoundException) {
                    pkg.substringAfterLast('.')
                }
                ScreenTimeStat(
                    packageName = pkg,
                    appName = label,
                    timeForegroundMs = entry.value,
                    percentageOfTotal = (entry.value / totalForegroundAll).coerceIn(0f, 1f)
                )
            }
            .sortedByDescending { it.timeForegroundMs }
    }

    suspend fun getAppUsageToday(packageName: String): Long = withContext(Dispatchers.IO) {
        if (!hasUsagePermission() || usageStatsManager == null) return@withContext 0L
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return@withContext 0L

        stats.filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
    }
}
