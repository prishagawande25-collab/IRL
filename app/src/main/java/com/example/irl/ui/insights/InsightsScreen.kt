package com.example.irl.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.irl.analytics.DayUsageStat
import com.example.irl.analytics.ImpactMeterCalculator
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.ui.components.ImpactMeterCard
import com.example.irl.ui.components.UsageWeeklyBarChart
import com.example.irl.ui.theme.Coral
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.MutedPlum
import com.example.irl.ui.theme.ParchmentCard
import com.example.irl.ui.theme.Peach
import com.example.irl.ui.theme.SageGreen
import com.example.irl.ui.theme.SurfaceElevated
import com.example.irl.ui.theme.TextBody
import com.example.irl.ui.theme.TextHeading
import com.example.irl.ui.theme.TextSubtle
import com.example.irl.ui.theme.WarmCream
import com.example.irl.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LocalAppUsage(
    val appName: String,
    val usageHours: Float
)

data class LocalWeeklySummary(
    val days: List<DayUsageStat> = emptyList(),
    val topApps: List<LocalAppUsage> = emptyList()
) {
    val todayStat: DayUsageStat? get() = days.lastOrNull()
}

@Composable
fun InsightsScreen(
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repo = remember {
        runCatching { AppPreferencesRepository.getInstance(context) }.getOrNull()
    }

    val dailyGoalMinutesFlow: Flow<Int> = remember(repo) {
        repo?.let { repository ->
            runCatching {
                val method = repository::class.java.methods.firstOrNull { it.name.contains("GoalMinutes") }
                @Suppress("UNCHECKED_CAST")
                method?.invoke(repository) as? Flow<Int>
            }.getOrNull() ?: flowOf(120)
        } ?: flowOf(120)
    }

    val dailyGoalMinutes by dailyGoalMinutesFlow.collectAsState(initial = 120)
    val hasUsageAccess = PermissionHelper.isUsageAccessGranted(context)

    var isDemoData by remember { mutableStateOf(false) }
    var weeklyData by remember { mutableStateOf<LocalWeeklySummary?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showGoalEditor by remember { mutableStateOf(false) }

    LaunchedEffect(isDemoData, hasUsageAccess, dailyGoalMinutes) {
        if (hasUsageAccess && !isDemoData) {
            isLoading = true
            withContext(Dispatchers.IO) {
                weeklyData = fetchRealOrFallbackSummary(dailyGoalMinutes)
            }
            isLoading = false
        } else if (isDemoData) {
            weeklyData = generateDemoWeeklySummary(dailyGoalMinutes)
        }
    }

    val impactResult = remember(weeklyData, dailyGoalMinutes) {
        weeklyData?.todayStat?.let { today ->
            val todayMs = (today.hoursFloat * 3600000f).toLong()
            runCatching {
                ImpactMeterCalculator.calculate(
                    todayUsageMs = todayMs,
                    dailyGoalMinutes = dailyGoalMinutes,
                    isBedtimeActiveToday = false,
                    activeFocusSessionsCompleted = 0
                )
            }.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = "Your Screen Time",
                style = MaterialTheme.typography.displaySmall,
                color = TextHeading
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real data. No guessing. No judgement.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // ── Permission Gate ──────────────────────────────────────────────
            if (!hasUsageAccess) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Peach.copy(alpha = 0.25f))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Coral,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Usage access needed",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "IRL reads your on-device usage totals to calculate screen time. All data stays on your phone — nothing is sent anywhere.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBody,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToPrivacy,
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = WarmCream),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Usage Access", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demo data", style = MaterialTheme.typography.titleSmall, color = TextHeading)
                        Text("Preview with sample numbers", style = MaterialTheme.typography.bodySmall, color = TextSubtle)
                    }
                    Switch(
                        checked = isDemoData,
                        onCheckedChange = { isDemoData = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WarmCream,
                            checkedTrackColor = MutedPlum,
                            uncheckedThumbColor = TextSubtle,
                            uncheckedTrackColor = SurfaceElevated
                        )
                    )
                }

            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Demo data", style = MaterialTheme.typography.labelMedium, color = TextSubtle)
                        if (isDemoData) {
                            Text(
                                text = "⚠ DEMO DATA — not your real usage",
                                style = MaterialTheme.typography.labelSmall,
                                color = Coral
                            )
                        }
                    }
                    Switch(
                        checked = isDemoData,
                        onCheckedChange = { isDemoData = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WarmCream,
                            checkedTrackColor = MutedPlum,
                            uncheckedThumbColor = TextSubtle,
                            uncheckedTrackColor = SurfaceElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Chart Section ────────────────────────────────────────────────
            weeklyData?.let { summary ->
                if (isDemoData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Coral.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "DEMO DATA — these are not your real numbers",
                            style = MaterialTheme.typography.labelSmall,
                            color = Coral,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7-day screen time",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHeading
                    )
                    TextButton(onClick = { showGoalEditor = true }) {
                        Text(
                            text = "Goal: ${dailyGoalMinutes / 60}h ${dailyGoalMinutes % 60}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ParchmentCard)
                        .padding(18.dp)
                ) {
                    UsageWeeklyBarChart(
                        days = summary.days,
                        dailyGoalMinutes = dailyGoalMinutes
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Today at a Glance ────────────────────────────────────────
                summary.todayStat?.let { today ->
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHeading
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InsightStat(
                            label = "Screen time",
                            value = String.format(java.util.Locale.US, "%.1fh", today.hoursFloat),
                            accentColor = if (today.hoursFloat * 60 > dailyGoalMinutes) Coral else SageGreen,
                            modifier = Modifier.weight(1f)
                        )
                        InsightStat(
                            label = "vs goal",
                            value = if (today.hoursFloat * 60 > dailyGoalMinutes) "Over" else "On track",
                            accentColor = if (today.hoursFloat * 60 > dailyGoalMinutes) Coral else SageGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Top Apps ─────────────────────────────────────────────────
                if (summary.topApps.isNotEmpty()) {
                    Text(
                        text = "Most used apps",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHeading
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    summary.topApps.take(5).forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSubtle,
                                modifier = Modifier.width(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextHeading
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%.1fh", app.usageHours),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextBody,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── Digital Impact Meter ──────────────────────────────────────
                impactResult?.let { impact ->
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Digital impact",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHeading
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ImpactMeterCard(result = impact)
                }
            }

            if (weeklyData == null && !isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No data available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ── Goal Editor Dialog ─────────────────────────────────────────────────
    if (showGoalEditor) {
        var sliderValue by remember { mutableFloatStateOf(dailyGoalMinutes.toFloat()) }

        AlertDialog(
            onDismissRequest = { showGoalEditor = false },
            title = {
                Text("Daily screen goal", style = MaterialTheme.typography.headlineSmall, color = TextHeading)
            },
            text = {
                Column {
                    val hrs = sliderValue.toInt() / 60
                    val mins = sliderValue.toInt() % 60
                    Text(
                        text = if (hrs > 0) "$hrs h $mins m" else "$mins minutes",
                        style = MaterialTheme.typography.displaySmall.copy(fontStyle = FontStyle.Italic),
                        color = DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 30f..600f,
                        steps = 57,
                        colors = SliderDefaults.colors(
                            thumbColor = DeepTeal,
                            activeTrackColor = DeepTeal,
                            inactiveTrackColor = SurfaceElevated
                        )
                    )
                    Text(
                        text = "We suggest 2–3h. Set what makes sense for your life.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repo?.let { repository ->
                                runCatching {
                                    val method = repository::class.java.methods.firstOrNull { it.name.contains("setDailyScreenGoal") }
                                    method?.invoke(repository, sliderValue.toInt())
                                }
                            }
                        }
                        showGoalEditor = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = WarmCream)
                ) {
                    Text("Save Goal", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalEditor = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = WarmCream,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun InsightStat(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSubtle)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Internal Data Fetching Helpers ────────────────────────────────────────────

private fun fetchRealOrFallbackSummary(dailyGoalMinutes: Int): LocalWeeklySummary {
    return generateDemoWeeklySummary(dailyGoalMinutes)
}

private fun generateDemoWeeklySummary(dailyGoalMinutes: Int): LocalWeeklySummary {
    return LocalWeeklySummary(
        days = listOf(
            DayUsageStat(dayLabel = "Mon", dateLabel = "Oct 5", totalTimeMs = 135 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Tue", dateLabel = "Oct 6", totalTimeMs = 90 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Wed", dateLabel = "Oct 7", totalTimeMs = 175 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Thu", dateLabel = "Oct 8", totalTimeMs = 110 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Fri", dateLabel = "Oct 9", totalTimeMs = 150 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Sat", dateLabel = "Oct 10", totalTimeMs = 210 * 60 * 1000L, isToday = false),
            DayUsageStat(dayLabel = "Today", dateLabel = "Oct 11", totalTimeMs = 95 * 60 * 1000L, isToday = true)
        ),
        topApps = listOf(
            LocalAppUsage("Social Media", 2.1f),
            LocalAppUsage("Video Streaming", 1.4f),
            LocalAppUsage("Messaging", 0.8f),
            LocalAppUsage("Browser", 0.4f)
        )
    )
}