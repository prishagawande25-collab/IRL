package com.example.irl.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.data.ActiveSessionState
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.data.BlockMode
import com.example.irl.ui.components.NatureLandscapeIllustration
import com.example.irl.ui.navigation.MainTab
import com.example.irl.ui.theme.BorderLight
import com.example.irl.ui.theme.ButterYellow
import com.example.irl.ui.theme.Coral
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.DustyRose
import com.example.irl.ui.theme.Lavender
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToTab: (MainTab) -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }

    val activeSession by repo.activeSessionFlow.collectAsState(initial = ActiveSessionState())
    val selectedApps by repo.selectedBlockedPackagesFlow.collectAsState(initial = emptySet())
    val userPoints by repo.userPointsFlow.collectAsState(initial = 0)
    val focusStreak by repo.focusStreakFlow.collectAsState(initial = 0)
    val bedtimeConfig by repo.bedtimeConfigFlow.collectAsState(initial = com.example.irl.data.BedtimeConfig())

    val isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeSession.isActive) {
        while (activeSession.isActive) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Hero Illustration Section ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
        ) {
            NatureLandscapeIllustration(height = 200.dp)

            // Hero text overlay at bottom of illustration
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 20.dp, end = 24.dp)
            ) {
                Text(
                    text = "In Real Life",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = WarmCream,
                        fontStyle = FontStyle.Italic
                    )
                )
                Text(
                    text = "Reclaim your time.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmCream.copy(alpha = 0.85f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Accessibility Notice (only when disabled) ──────────────────────
        if (!isAccessibilityEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Peach.copy(alpha = 0.25f))
                    .clickable { onNavigateToPrivacy() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable blocking",
                        style = MaterialTheme.typography.titleSmall,
                        color = Coral,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap to enable Accessibility so IRL can pause distracting apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBody
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Set up →",
                    style = MaterialTheme.typography.labelMedium,
                    color = Coral,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Active Session Card ────────────────────────────────────────────
        if (activeSession.isActive) {
            val remainingMs = (activeSession.endTimeMs - currentTimeMs).coerceAtLeast(0L)
            val sec = (remainingMs / 1000) % 60
            val min = (remainingMs / (1000 * 60)) % 60
            val hrs = remainingMs / (1000 * 60 * 60)
            val countdownStr = if (hrs > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hrs, min, sec)
            } else {
                String.format(Locale.US, "%02d:%02d", min, sec)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DeepTeal, DeepTeal.copy(alpha = 0.85f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = activeSession.mode.displayName + " session",
                    style = MaterialTheme.typography.labelMedium,
                    color = SageGreen.copy(alpha = 0.9f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = countdownStr,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = WarmCream,
                        fontStyle = FontStyle.Normal
                    ),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = "${activeSession.blockedPackages.size} apps paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmCream.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { scope.launch { repo.stopSession() } },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Coral,
                        contentColor = WarmCream
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End Session", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // ── Quick Focus Start ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = if (activeSession.isActive) "In session" else "Start a focus session",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextHeading
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedApps.isNotEmpty()) {
                        "${selectedApps.size} apps ready to pause."
                    } else {
                        "Choose apps to pause, then start when ready."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextBody
                )

                Spacer(modifier = Modifier.height(14.dp))

                var quickDuration by remember { mutableIntStateOf(30) }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        FilterChip(
                            selected = quickDuration == mins,
                            onClick = { quickDuration = mins },
                            label = {
                                Text(
                                    "${mins}m",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepTeal,
                                selectedLabelColor = WarmCream,
                                containerColor = SurfaceElevated,
                                labelColor = TextBody
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = quickDuration == mins,
                                borderColor = BorderLight,
                                selectedBorderColor = DeepTeal
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedApps.isNotEmpty()) {
                                scope.launch {
                                    repo.startSession(quickDuration, BlockMode.FOCUS, selectedApps)
                                }
                            } else {
                                onNavigateToTab(MainTab.BLOCK)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepTeal,
                            contentColor = WarmCream
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedApps.isNotEmpty()) "Focus ${quickDuration}m" else "Choose apps",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigateToTab(MainTab.BLOCK) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBody),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Text("Configure")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Streak & Points strip ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeStatPill(
                label = "Day streak",
                value = "$focusStreak",
                accentColor = ButterYellow,
                modifier = Modifier.weight(1f)
            )
            HomeStatPill(
                label = "Focus points",
                value = "$userPoints pts",
                accentColor = Lavender,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Bedtime Banner (when configured) ──────────────────────────────
        if (bedtimeConfig.enabled) {
            val startFmt = String.format(Locale.US, "%02d:%02d", bedtimeConfig.startHour, bedtimeConfig.startMinute)
            val endFmt   = String.format(Locale.US, "%02d:%02d", bedtimeConfig.endHour, bedtimeConfig.endMinute)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MutedPlum.copy(alpha = 0.08f))
                    .clickable { onNavigateToTab(MainTab.BLOCK) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = DustyRose,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bedtime schedule active",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHeading
                    )
                    Text(
                        text = "$startFmt – $endFmt · ${bedtimeConfig.blockedPackages.size} apps paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBody
                    )
                }
                Text(
                    text = "Edit →",
                    style = MaterialTheme.typography.labelMedium,
                    color = DustyRose
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Mindful Nudge ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            SageGreen.copy(alpha = 0.2f),
                            Lavender.copy(alpha = 0.15f)
                        )
                    )
                )
                .clickable { onNavigateToTab(MainTab.RESET) }
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Need a pause?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Step outside. Breathe. Play a focus game. The Reset tab has ideas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextBody
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Explore reset activities →",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Insights shortcut ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ParchmentCard)
                .clickable { onNavigateToTab(MainTab.INSIGHTS) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Digital Impact Meter",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextHeading,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "View your wellbeing balance & screen-time trends",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBody
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = DeepTeal
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun HomeStatPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSubtle
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextHeading,
            fontWeight = FontWeight.SemiBold
        )
    }
}
