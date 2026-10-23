package com.example.irl.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.ui.theme.DangerCrimson
import com.example.irl.ui.theme.ElectricBlue
import com.example.irl.ui.theme.NavyBackground
import com.example.irl.ui.theme.NavyCardStroke
import com.example.irl.ui.theme.NavySurface
import com.example.irl.ui.theme.NavySurfaceElevated
import com.example.irl.ui.theme.SuccessEmerald
import com.example.irl.ui.theme.TextMuted
import com.example.irl.ui.theme.TextPrimary
import com.example.irl.ui.theme.TextSecondary
import com.example.irl.ui.theme.VioletAccent
import com.example.irl.ui.theme.WarningAmber
import com.example.irl.util.PermissionHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }

    var isAccessibilityEnabled by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var isUsageAccessGranted by remember { mutableStateOf(PermissionHelper.isUsageAccessGranted(context)) }

    // Re-check permissions when coming back from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
                isUsageAccessGranted = PermissionHelper.isUsageAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showAccessibilityPreExplanation by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRestrictedSettingsGuide by remember { mutableStateOf(false) }
    var showUsagePreExplanation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy & Permissions",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        },
        containerColor = NavyBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 100% Offline Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SuccessEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "100% Local & Offline",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "No INTERNET permission. No servers, accounts, or telemetry. Zero bytes ever leave your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Stored Data Transparency
            Text(
                text = "What is Stored on Your Phone",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricBlue,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StoredDataItem(title = "App Blocklist", detail = "Package names of launchable apps you selected to restrict.")
                    StoredDataItem(title = "Active Session Timestamps", detail = "Start time, end time, and mode (Gentle, Focus, Deep Focus).")
                    StoredDataItem(title = "Bedtime Schedule", detail = "Configured start/end times and chosen days of the week.")
                    StoredDataItem(title = "Offline Game Scores", detail = "High scores from Number Rush, Memory Match, and Reaction Test.")
                    StoredDataItem(title = "Habit Streaks & Rewards", detail = "Your focus streak, points tally, and custom reward entries.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Accessibility Service
            Text(
                text = "Accessibility Service",
                style = MaterialTheme.typography.titleMedium,
                color = VioletAccent,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        PermissionStatusBadge(granted = isAccessibilityEnabled)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Used exclusively to identify when a restricted app is brought to the foreground so the block screen can appear. It never reads screen contents, keystrokes, passwords, or gestures.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAccessibilityPreExplanation = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VioletAccent,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(text = if (isAccessibilityEnabled) "Accessibility Settings" else "Enable Accessibility")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sideload Help Button
                    TextButton(
                        onClick = { showRestrictedSettingsGuide = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Android 13+ 'Restricted setting' guide", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    }

                    Text(
                        text = "How to disable: Open Settings → Accessibility → IRL → Turn switch off. Blocking immediately stops.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Usage Access
            Text(
                text = "Usage Stats Access",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricBlue,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        PermissionStatusBadge(granted = isUsageAccessGranted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Allows the Insights screen to read on-device usage totals directly from Android's local UsageStatsManager. Without this permission, analytics remain completely blank and no numbers are fabricated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showUsagePreExplanation = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = NavyBackground
                        )
                    ) {
                        Text(text = if (isUsageAccessGranted) "Usage Access Settings" else "Grant Usage Access", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How to revoke: Open Settings → Apps → Special access → Usage access → IRL → Turn off.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Section: Delete All Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DangerCrimson.copy(alpha = 0.4f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reset All Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wipes all local settings, active block sessions, high scores, and custom rewards from your device storage immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerCrimson),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DangerCrimson)
                        )
                    ) {
                        Text(text = "Delete All Stored Data", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Dialog: Plain Explanation before Accessibility Settings
    if (showAccessibilityPreExplanation) {
        AlertDialog(
            onDismissRequest = { showAccessibilityPreExplanation = false },
            title = {
                Text(
                    text = "Understanding Accessibility in IRL",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Before you open system settings, here is an exact explanation of what this permission does and does not do:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "✓ What IRL does:",
                        style = MaterialTheme.typography.labelLarge,
                        color = SuccessEmerald
                    )
                    Text(
                        text = "• Detects only the package name of the app currently opened.\n• If that app is in your active block list, IRL displays the blocking screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✗ What IRL CANNOT and will NEVER do:",
                        style = MaterialTheme.typography.labelLarge,
                        color = DangerCrimson
                    )
                    Text(
                        text = "• Cannot read any text, messages, or content on your screen.\n• Cannot capture keystrokes, passwords, or credentials.\n• Cannot perform taps or gestures.\n• Cannot transmit anything (no internet permission).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Self-Control Notice: IRL is a self-control aid to build healthy habits, not an unbreakable barrier. You can disable this permission at any moment in Android Settings.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityPreExplanation = false
                        PermissionHelper.openAccessibilitySettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent, contentColor = TextPrimary)
                ) {
                    Text("Continue to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityPreExplanation = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog: Plain Explanation before Usage Access Settings
    if (showUsagePreExplanation) {
        AlertDialog(
            onDismissRequest = { showUsagePreExplanation = false },
            title = {
                Text(
                    text = "Usage Stats Permission",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "IRL requires Usage Access (PACKAGE_USAGE_STATS) solely to calculate your daily screen time and per-app usage inside the Insights tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• All numbers are computed strictly on-device via Android's local UsageStatsManager.\n• If permission is missing, Insights shows no data and never fabricates numbers.\n• You can revoke this permission anytime in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUsagePreExplanation = false
                        PermissionHelper.openUsageAccessSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = NavyBackground)
                ) {
                    Text("Continue to Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsagePreExplanation = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog: Android 13+ Sideload Restricted Settings Guide
    if (showRestrictedSettingsGuide) {
        AlertDialog(
            onDismissRequest = { showRestrictedSettingsGuide = false },
            title = {
                Text(
                    text = "Allow Restricted Settings (Android 13+)",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Because IRL is sideloaded directly via USB/debug APK rather than Google Play, Android may grey out the Accessibility switch with 'Restricted setting'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To enable it safely:",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Tap the button below to open IRL's App Info page.\n2. Tap the 3-dots menu (⋮) in the top-right corner.\n3. Tap 'Allow restricted settings'.\n4. Authenticate using your fingerprint, face, or PIN.\n5. Return to Accessibility and toggle IRL on.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Security note: This is Android's built-in check for manually installed apps. Do not disable Google Play Protect or weaken other security settings.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestrictedSettingsGuide = false
                        PermissionHelper.openAppInfoSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = NavyBackground)
                ) {
                    Text("Open App Info", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestrictedSettingsGuide = false }) {
                    Text("Done", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dialog: Delete All Data Confirmation
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete All Stored Data?",
                    style = MaterialTheme.typography.titleMedium,
                    color = DangerCrimson,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will immediately end any active block session, clear your selected apps list, reset all game high scores and wellbeing points, and restore default settings. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        scope.launch {
                            repo.clearAllData()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson, contentColor = TextPrimary)
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun StoredDataItem(title: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun PermissionStatusBadge(granted: Boolean) {
    val bgColor = if (granted) SuccessEmerald.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
    val textColor = if (granted) SuccessEmerald else WarningAmber
    val text = if (granted) "Active" else "Disabled"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
