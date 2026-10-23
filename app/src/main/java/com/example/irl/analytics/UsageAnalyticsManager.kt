package com.example.irl.analytics

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.irl.util.AppListProvider
import com.example.irl.util.PermissionHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val percentageOfTotal: Float
) {
    val formattedDuration: String
        get() = formatDuration(foregroundTimeMs)

    companion object {
        fun formatDuration(ms: Long): String {
            val totalSeconds = ms / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "< 1m"
            }
        }
    }
}

data class DayUsageStat(
    val dayLabel: String,     // e.g. "Mon"
    val dateLabel: String,    // e.g. "10 Oct"
    val totalTimeMs: Long,
    val isToday: Boolean
) {
    val formattedDuration: String
        get() = AppUsageStat.formatDuration(totalTimeMs)

    val hoursFloat: Float
        get() = totalTimeMs.toFloat() / (1000f * 60f * 60f)
}

data class UsageReport(
    val isPermissionGranted: Boolean,
    val isDemoData: Boolean,
    val todayTotalMs: Long,
    val weeklyTotalMs: Long,
    val dailyAverageMs: Long,
    val topApps: List<AppUsageStat>,
    val last7Days: List<DayUsageStat>
)

object UsageAnalyticsManager {

    private val SYSTEM_EXCLUDED_PACKAGES = setOf(
        "com.android.systemui",
        "com.android.launcher",
        "com.google.android.apps.nexuslauncher"
    )

    fun getUsageReport(context: Context, isDemoDataEnabled: Boolean): UsageReport {
        val hasPermission = PermissionHelper.isUsageAccessGranted(context)

        // 1. If user enabled demo data, return clearly marked simulated data
        if (isDemoDataEnabled) {
            return generateDemoReport(hasPermission)
        }

        // 2. If permission is missing and demo data is off, return blank data (NEVER fabricate real data)
        if (!hasPermission) {
            return UsageReport(
                isPermissionGranted = false,
                isDemoData = false,
                todayTotalMs = 0L,
                weeklyTotalMs = 0L,
                dailyAverageMs = 0L,
                topApps = emptyList(),
                last7Days = emptyList()
            )
        }

        // 3. Query real UsageStatsManager data
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return UsageReport(false, false, 0L, 0L, 0L, emptyList(), emptyList())

            val now = System.currentTimeMillis()

            // Today's range (00:00:00 to now)
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = cal.timeInMillis

            // Query aggregated usage for today
            val todayStatsMap: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(startOfToday, now)
            val filteredTodayList = mutableListOf<AppUsageStat>()
            var todayTotalMs = 0L

            for ((pkg, stats) in todayStatsMap) {
                val time = stats.totalTimeInForeground
                if (time > 60_000L && !SYSTEM_EXCLUDED_PACKAGES.contains(pkg)) {
                    val label = AppListProvider.getAppLabel(context, pkg)
                    filteredTodayList.add(
                        AppUsageStat(
                            packageName = pkg,
                            appName = label,
                            foregroundTimeMs = time,
                            percentageOfTotal = 0f
                        )
                    )
                    todayTotalMs += time
                }
            }

            // Sort by highest usage first
            val sortedTodayApps = filteredTodayList.sortedByDescending { it.foregroundTimeMs }
            val topAppsWithPercentage = sortedTodayApps.map {
                it.copy(percentageOfTotal = if (todayTotalMs > 0) (it.foregroundTimeMs.toFloat() / todayTotalMs) else 0f)
            }

            // Calculate last 7 days history
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
            val last7Days = mutableListOf<DayUsageStat>()
            var weeklyTotalMs = 0L

            for (i in 6 downTo 0) {
                val dayCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -i)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayStart = dayCal.timeInMillis
                val dayEnd = if (i == 0) now else (dayStart + 24 * 60 * 60 * 1000L - 1)

                val dayMap = usageStatsManager.queryAndAggregateUsageStats(dayStart, dayEnd)
                var dayTotal = 0L
                for ((pkg, stats) in dayMap) {
                    if (!SYSTEM_EXCLUDED_PACKAGES.contains(pkg)) {
                        dayTotal += stats.totalTimeInForeground
                    }
                }

                weeklyTotalMs += dayTotal
                last7Days.add(
                    DayUsageStat(
                        dayLabel = dayFormat.format(dayCal.time),
                        dateLabel = dateFormat.format(dayCal.time),
                        totalTimeMs = dayTotal,
                        isToday = (i == 0)
                    )
                )
            }

            val dailyAverage = if (last7Days.isNotEmpty()) (weeklyTotalMs / last7Days.size) else 0L

            UsageReport(
                isPermissionGranted = true,
                isDemoData = false,
                todayTotalMs = todayTotalMs,
                weeklyTotalMs = weeklyTotalMs,
                dailyAverageMs = dailyAverage,
                topApps = topAppsWithPercentage,
                last7Days = last7Days
            )
        } catch (_: Exception) {
            UsageReport(false, false, 0L, 0L, 0L, emptyList(), emptyList())
        }
    }

    private fun generateDemoReport(hasPermission: Boolean): UsageReport {
        val todayTotal = 13_320_000L // 3h 42m

        val demoApps = listOf(
            AppUsageStat("com.example.video", "Video Player", 5_040_000L, 0.38f),       // 1h 24m
            AppUsageStat("com.example.social", "Social Feed", 3_480_000L, 0.26f),        // 58m
            AppUsageStat("com.example.learn", "Language Learning", 1_920_000L, 0.14f),   // 32m
            AppUsageStat("com.example.music", "Music & Podcasts", 1_560_000L, 0.12f),    // 26m
            AppUsageStat("com.example.chat", "Messaging App", 1_320_000L, 0.10f)         // 22m
        )

        val demo7Days = listOf(
            DayUsageStat("Mon", "5 Oct", 11_700_000L, false), // 3h 15m
            DayUsageStat("Tue", "6 Oct", 15_000_000L, false), // 4h 10m
            DayUsageStat("Wed", "7 Oct", 9_900_000L, false),  // 2h 45m
            DayUsageStat("Thu", "8 Oct", 13_800_000L, false), // 3h 50m
            DayUsageStat("Fri", "9 Oct", 16_200_000L, false), // 4h 30m
            DayUsageStat("Sat", "10 Oct", 18_600_000L, false), // 5h 10m
            DayUsageStat("Sun", "11 Oct", todayTotal, true)    // 3h 42m
        )

        val weeklyTotal = demo7Days.sumOf { it.totalTimeMs }
        val dailyAverage = weeklyTotal / demo7Days.size

        return UsageReport(
            isPermissionGranted = hasPermission,
            isDemoData = true,
            todayTotalMs = todayTotal,
            weeklyTotalMs = weeklyTotal,
            dailyAverageMs = dailyAverage,
            topApps = demoApps,
            last7Days = demo7Days
        )
    }
}
