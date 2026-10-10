package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.local.AppRestrictionDao
import com.example.data.local.AppRestrictionEntity
import com.example.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val restrictionDao: AppRestrictionDao
) {
    private val packageManager: PackageManager = context.packageManager

    // Raw installed apps flow
    private fun getRawInstalledApps(): Flow<List<RawApp>> = flow {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val list = resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            // Don't show our own launcher in the app drawer
            if (pkg == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            RawApp(pkg, label, icon)
        }.sortedBy { it.label.lowercase() }
        emit(list)
    }.flowOn(Dispatchers.IO)

    fun getApps(): Flow<List<AppInfo>> {
        return combine(
            getRawInstalledApps(),
            restrictionDao.getAllRestrictions()
        ) { rawApps, restrictions ->
            val restrictionMap = restrictions.associateBy { it.packageName }
            rawApps.map { raw ->
                val restriction = restrictionMap[raw.packageName]
                AppInfo(
                    packageName = raw.packageName,
                    label = raw.label,
                    icon = raw.icon,
                    isFavorite = restriction?.isFavorite ?: false,
                    isHidden = restriction?.isHidden ?: false,
                    mindfulPauseEnabled = restriction?.mindfulPauseEnabled ?: false,
                    pauseDurationSeconds = restriction?.pauseDurationSeconds ?: 10,
                    dailyBudgetMinutes = restriction?.dailyBudgetMinutes ?: 0,
                    mindfulBreakUntil = restriction?.mindfulBreakUntil ?: 0L,
                    breakMotivation = restriction?.breakMotivation ?: "",
                    isWhitelistedInFocus = restriction?.isWhitelistedInFocus ?: false
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(app: AppInfo) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    isFavorite = !app.isFavorite
                )
            )
        } else {
            restrictionDao.setFavorite(app.packageName, !app.isFavorite)
        }
    }

    suspend fun toggleHidden(app: AppInfo) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    isHidden = !app.isHidden
                )
            )
        } else {
            restrictionDao.setHidden(app.packageName, !app.isHidden)
        }
    }

    suspend fun setMindfulPause(app: AppInfo, enabled: Boolean, durationSeconds: Int) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    mindfulPauseEnabled = enabled,
                    pauseDurationSeconds = durationSeconds
                )
            )
        } else {
            restrictionDao.setMindfulPause(app.packageName, enabled, durationSeconds)
        }
    }

    suspend fun setDailyBudget(app: AppInfo, budgetMinutes: Int) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    dailyBudgetMinutes = budgetMinutes
                )
            )
        } else {
            restrictionDao.setDailyBudget(app.packageName, budgetMinutes)
        }
    }

    suspend fun setMindfulBreak(app: AppInfo, untilMillis: Long, motivation: String) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    mindfulBreakUntil = untilMillis,
                    breakMotivation = motivation
                )
            )
        } else {
            restrictionDao.setMindfulBreak(app.packageName, untilMillis, motivation)
        }
    }

    suspend fun setFocusWhitelist(app: AppInfo, whitelisted: Boolean) = withContext(Dispatchers.IO) {
        val existing = restrictionDao.getRestriction(app.packageName)
        if (existing == null) {
            restrictionDao.upsertRestriction(
                AppRestrictionEntity(
                    packageName = app.packageName,
                    appName = app.label,
                    isWhitelistedInFocus = whitelisted
                )
            )
        } else {
            restrictionDao.setFocusWhitelist(app.packageName, whitelisted)
        }
    }

    private data class RawApp(
        val packageName: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable?
    )
}
