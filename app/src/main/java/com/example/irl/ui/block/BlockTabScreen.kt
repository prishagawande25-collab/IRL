package com.example.irl.ui.block

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.data.ActiveSessionState
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.data.BedtimeConfig
import com.example.irl.data.BlockMode
import com.example.irl.data.InstalledAppItem
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
import com.example.irl.util.AppListProvider
import com.example.irl.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private enum class BlockSubTab(val title: String) {
    SESSION("Focus Session"),
    BEDTIME("Bedtime")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockTabScreen(
    onNavigateToPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }

    val activeSession by repo.activeSessionFlow.collectAsState(initial = ActiveSessionState())
    val persistedBlockedApps by repo.selectedBlockedPackagesFlow.collectAsState(initial = emptySet())
    val bedtimeConfig by repo.bedtimeConfigFlow.collectAsState(initial = BedtimeConfig())

    var currentSubTab by rememberSaveable { mutableStateOf(BlockSubTab.SESSION) }

    var selectedApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    var installedApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableIntStateOf(30) }
    var selectedMode by remember { mutableStateOf(BlockMode.FOCUS) }
    var isAppListLoading by remember { mutableStateOf(true) }

    var bedtimeEnabled by remember { mutableStateOf(bedtimeConfig.enabled) }
    var bedtimeStartH by remember { mutableIntStateOf(bedtimeConfig.startHour) }
    var bedtimeStartM by remember { mutableIntStateOf(bedtimeConfig.startMinute) }
    var bedtimeEndH by remember { mutableIntStateOf(bedtimeConfig.endHour) }
    var bedtimeEndM by remember { mutableIntStateOf(bedtimeConfig.endMinute) }
    var bedtimeDays by remember { mutableStateOf(bedtimeConfig.daysOfWeek) }
    var bedtimeBlockedApps by remember { mutableStateOf(bedtimeConfig.blockedPackages) }
    var bedtimeSavedNotice by remember { mutableStateOf(false) }

    val isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)

    LaunchedEffect(bedtimeConfig) {
        bedtimeEnabled = bedtimeConfig.enabled
        bedtimeStartH = bedtimeConfig.startHour
        bedtimeStartM = bedtimeConfig.startMinute
        bedtimeEndH = bedtimeConfig.endHour
        bedtimeEndM = bedtimeConfig.endMinute
        bedtimeDays = bedtimeConfig.daysOfWeek
        bedtimeBlockedApps = bedtimeConfig.blockedPackages
    }

    LaunchedEffect(persistedBlockedApps) {
        if (selectedApps.isEmpty() && persistedBlockedApps.isNotEmpty()) {
            selectedApps = persistedBlockedApps
        }
    }

    LaunchedEffect(Unit) {
        isAppListLoading = true
        withContext(Dispatchers.IO) {
            val apps = AppListProvider.getInstalledLaunchableApps(context, persistedBlockedApps)
            installedApps = apps
        }
        isAppListLoading = false
    }

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeSession.isActive) {
        while (activeSession.isActive) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        val query = searchQuery.trim().lowercase()
        installedApps.filter {
            query.isEmpty() || it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(horizontal = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "App Blocker",
                style = MaterialTheme.typography.displaySmall,
                color = TextHeading
            )
            Text(
                text = "Pause distracting apps while you focus or sleep.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Tab switcher
            TabRow(
                selectedTabIndex = currentSubTab.ordinal,
                containerColor = SurfaceElevated,
                contentColor = DeepTeal,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                        color = DeepTeal,
                        height = 2.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                BlockSubTab.entries.forEach { tab ->
                    val isSelected = currentSubTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { currentSubTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) DeepTeal else TextSubtle,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active session status (always shown)
        if (activeSession.isActive) {
            item {
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
                        .clip(RoundedCornerShape(18.dp))
                        .background(DeepTeal)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Session active · ${activeSession.mode.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SageGreen.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = countdownStr,
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = WarmCream,
                            fontWeight = FontWeight.Bold
                        ),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "${activeSession.blockedPackages.size} apps paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmCream.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { scope.launch { repo.stopSession() } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Coral,
                            contentColor = WarmCream
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End Session", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Accessibility warning
        if (!isAccessibilityEnabled) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Peach.copy(alpha = 0.3f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility required", style = MaterialTheme.typography.titleSmall, color = Coral, fontWeight = FontWeight.SemiBold)
                        Text("IRL needs Accessibility access to detect and pause restricted apps.", style = MaterialTheme.typography.bodySmall, color = TextBody)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onNavigateToPrivacy,
                        colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = WarmCream),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enable", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ─── SESSION TAB ───────────────────────────────────────────────────
        if (currentSubTab == BlockSubTab.SESSION) {
            item {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextHeading
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { mins ->
                        FilterChip(
                            selected = selectedDuration == mins,
                            onClick = { selectedDuration = mins },
                            label = { Text("${mins}m", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepTeal,
                                selectedLabelColor = WarmCream,
                                containerColor = SurfaceElevated,
                                labelColor = TextBody
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDuration == mins,
                                borderColor = BorderLight,
                                selectedBorderColor = DeepTeal
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Focus mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextHeading
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeOptionRow(
                        mode = BlockMode.GENTLE,
                        isSelected = selectedMode == BlockMode.GENTLE,
                        accentColor = SageGreen,
                        onSelect = { selectedMode = BlockMode.GENTLE }
                    )
                    ModeOptionRow(
                        mode = BlockMode.FOCUS,
                        isSelected = selectedMode == BlockMode.FOCUS,
                        accentColor = ButterYellow,
                        onSelect = { selectedMode = BlockMode.FOCUS }
                    )
                    ModeOptionRow(
                        mode = BlockMode.DEEP_FOCUS,
                        isSelected = selectedMode == BlockMode.DEEP_FOCUS,
                        accentColor = Coral,
                        onSelect = { selectedMode = BlockMode.DEEP_FOCUS }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apps to pause (${selectedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading
                    )
                    Row {
                        TextButton(onClick = {
                            val all = installedApps.map { it.packageName }.toSet()
                            selectedApps = all
                            scope.launch { repo.setSelectedBlockedPackages(all) }
                        }) {
                            Text("All", color = DeepTeal, style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = {
                            selectedApps = emptySet()
                            scope.launch { repo.setSelectedBlockedPackages(emptySet()) }
                        }) {
                            Text("Clear", color = TextSubtle, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps…", color = TextSubtle) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSubtle) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSubtle)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ParchmentCard,
                        unfocusedContainerColor = ParchmentCard,
                        focusedBorderColor = DeepTeal,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextHeading,
                        unfocusedTextColor = TextHeading
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(filteredApps, key = { "session_${it.packageName}" }) { appItem ->
                val isBlocked = selectedApps.contains(appItem.packageName)
                AppRowItem(
                    appItem = appItem,
                    isBlocked = isBlocked,
                    accentColor = DeepTeal,
                    onToggle = { checked ->
                        val updated = if (checked) selectedApps + appItem.packageName else selectedApps - appItem.packageName
                        selectedApps = updated
                        scope.launch { repo.setSelectedBlockedPackages(updated) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (selectedApps.isNotEmpty()) {
                            scope.launch {
                                repo.startSession(
                                    durationMinutes = selectedDuration,
                                    mode = selectedMode,
                                    packages = selectedApps
                                )
                            }
                        }
                    },
                    enabled = selectedApps.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor = WarmCream,
                        disabledContainerColor = SurfaceElevated,
                        disabledContentColor = TextSubtle
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedApps.isNotEmpty()) {
                            "Start ${selectedMode.displayName} · ${selectedDuration}m"
                        } else {
                            "Select at least 1 app"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

        } else {
            // ─── BEDTIME TAB ────────────────────────────────────────────────
            item {
                // Bedtime toggle card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ParchmentCard)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bedtime protection",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextHeading,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (bedtimeEnabled) "Active on selected days" else "Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bedtimeEnabled) SageGreen else TextSubtle
                            )
                        }
                        Switch(
                            checked = bedtimeEnabled,
                            onCheckedChange = { bedtimeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WarmCream,
                                checkedTrackColor = MutedPlum,
                                uncheckedThumbColor = TextSubtle,
                                uncheckedTrackColor = SurfaceElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Time pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Starts at", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                            Spacer(modifier = Modifier.height(4.dp))
                            TimePickerChip(
                                hour = bedtimeStartH,
                                minute = bedtimeStartM,
                                onHourChange = { bedtimeStartH = it },
                                onMinuteChange = { bedtimeStartM = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ends at", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                            Spacer(modifier = Modifier.height(4.dp))
                            TimePickerChip(
                                hour = bedtimeEndH,
                                minute = bedtimeEndM,
                                onHourChange = { bedtimeEndH = it },
                                onMinuteChange = { bedtimeEndM = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Active days", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysMap = listOf(2 to "M", 3 to "T", 4 to "W", 5 to "T", 6 to "F", 7 to "S", 1 to "S")
                        daysMap.forEach { (calDay, letter) ->
                            val isSelected = bedtimeDays.contains(calDay)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MutedPlum else SurfaceElevated)
                                    .clickable {
                                        bedtimeDays = if (isSelected) bedtimeDays - calDay else bedtimeDays + calDay
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) WarmCream else TextSubtle,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apps paused at bedtime (${bedtimeBlockedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading
                    )
                    Row {
                        TextButton(onClick = { bedtimeBlockedApps = installedApps.map { it.packageName }.toSet() }) {
                            Text("All", color = MutedPlum, style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { bedtimeBlockedApps = emptySet() }) {
                            Text("None", color = TextSubtle, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredApps, key = { "bedtime_${it.packageName}" }) { appItem ->
                val isBlocked = bedtimeBlockedApps.contains(appItem.packageName)
                AppRowItem(
                    appItem = appItem,
                    isBlocked = isBlocked,
                    accentColor = MutedPlum,
                    onToggle = { checked ->
                        bedtimeBlockedApps = if (checked) bedtimeBlockedApps + appItem.packageName else bedtimeBlockedApps - appItem.packageName
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val config = BedtimeConfig(
                            enabled = bedtimeEnabled,
                            startHour = bedtimeStartH,
                            startMinute = bedtimeStartM,
                            endHour = bedtimeEndH,
                            endMinute = bedtimeEndM,
                            daysOfWeek = bedtimeDays,
                            blockedPackages = bedtimeBlockedApps
                        )
                        scope.launch {
                            repo.updateBedtimeConfig(config)
                            bedtimeSavedNotice = true
                            delay(2000L)
                            bedtimeSavedNotice = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedPlum, contentColor = WarmCream)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (bedtimeSavedNotice) "Saved!" else "Save Bedtime Schedule",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Safety note — no card, just a clean note row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SageGreen.copy(alpha = 0.12f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SageGreen,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fail-safe: System UI, emergency dialer, Android Settings, and IRL itself are never blocked. You can turn bedtime off at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBody
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun TimePickerChip(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val isPm = hour >= 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val timeFormatted = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, if (isPm) "PM" else "AM")

    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
            .clickable { showPicker = true }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.titleMedium,
            color = TextHeading,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (showPicker) {
        var tempHour by remember { mutableIntStateOf(hour) }
        var tempMinute by remember { mutableIntStateOf(minute) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Adjust Time", color = TextHeading, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { tempHour = (tempHour - 1 + 24) % 24 }) {
                                    Text("-", fontSize = 20.sp, color = DeepTeal)
                                }
                                Text("$tempHour", style = MaterialTheme.typography.titleLarge, color = TextHeading, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { tempHour = (tempHour + 1) % 24 }) {
                                    Text("+", fontSize = 20.sp, color = DeepTeal)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute", style = MaterialTheme.typography.labelSmall, color = TextSubtle)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { tempMinute = (tempMinute - 15 + 60) % 60 }) {
                                    Text("-", fontSize = 20.sp, color = DeepTeal)
                                }
                                Text(String.format(Locale.US, "%02d", tempMinute), style = MaterialTheme.typography.titleLarge, color = TextHeading, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { tempMinute = (tempMinute + 15) % 60 }) {
                                    Text("+", fontSize = 20.sp, color = DeepTeal)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onHourChange(tempHour)
                        onMinuteChange(tempMinute)
                        showPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = WarmCream)
                ) {
                    Text("Set Time", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = ParchmentCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun AppRowItem(
    appItem: InstalledAppItem,
    isBlocked: Boolean,
    accentColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isBlocked) accentColor.copy(alpha = 0.08f) else ParchmentCard)
            .clickable { onToggle(!isBlocked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appItem.appName,
                style = MaterialTheme.typography.titleSmall,
                color = if (isBlocked) accentColor else TextHeading
            )
            Text(
                text = appItem.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSubtle
            )
        }
        Checkbox(
            checked = isBlocked,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = accentColor,
                checkmarkColor = WarmCream,
                uncheckedColor = BorderLight
            )
        )
    }
}

@Composable
private fun ModeOptionRow(
    mode: BlockMode,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else ParchmentCard)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = accentColor,
                unselectedColor = BorderLight
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) accentColor else TextHeading,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = mode.shortDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody
            )
        }
    }
}
