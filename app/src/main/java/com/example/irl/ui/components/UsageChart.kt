package com.example.irl.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.analytics.DayUsageStat
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.TextBody
import com.example.irl.ui.theme.TextHeading
import com.example.irl.ui.theme.TextSubtle
import com.example.irl.ui.theme.Coral

@Composable
fun UsageWeeklyBarChart(
    days: List<DayUsageStat>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    val goalHours = dailyGoalMinutes / 60f
    val maxUsageHours = days.maxOfOrNull { it.hoursFloat } ?: 1f
    val chartCeilingHours = maxOf(maxUsageHours, goalHours, 1f) * 1.15f // 15% headroom

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Goal Legend Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last 7 Days Screen Time",
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(3.dp)
                        .background(Coral, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Goal: ${dailyGoalMinutes / 60}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = Coral
                )
            }
        }

        // Bars Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Goal line indicator
            val goalFraction = (goalHours / chartCeilingHours).coerceIn(0.05f, 0.95f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(bottom = (160 * goalFraction).dp)
                    .height(1.dp)
                    .background(Coral.copy(alpha = 0.4f))
            )

            // Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = selectedIndex == index
                    val fraction = (day.hoursFloat / chartCeilingHours).coerceIn(0.04f, 1f)

                    val animatedHeight by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 600),
                        label = "BarHeightAnimation"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedIndex = if (isSelected) null else index },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Duration label above bar on select or today
                        if (isSelected || (selectedIndex == null && day.isToday)) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1fh", day.hoursFloat),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (day.isToday) DeepTeal else TextBody,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Bar
                        val barColor = when {
                            day.isToday -> DeepTeal
                            day.hoursFloat > goalHours -> Coral
                            else -> DeepTeal.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of week labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day.isToday) DeepTeal else TextSubtle,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}