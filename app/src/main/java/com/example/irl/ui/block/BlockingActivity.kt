package com.example.irl.ui.block

import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.MainActivity
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.data.BlockMode
import com.example.irl.ui.components.FocusCircleIllustration
import com.example.irl.ui.theme.ButterYellow
import com.example.irl.ui.theme.Coral
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.DeepTealLight
import com.example.irl.ui.theme.DustyRose
import com.example.irl.ui.theme.IRLTheme
import com.example.irl.ui.theme.Lavender
import com.example.irl.ui.theme.MutedPlum
import com.example.irl.ui.theme.ParchmentCard
import com.example.irl.ui.theme.SageGreen
import com.example.irl.ui.theme.SurfacePlum
import com.example.irl.ui.theme.TextBody
import com.example.irl.ui.theme.TextHeading
import com.example.irl.ui.theme.TextSubtle
import com.example.irl.ui.theme.WarmCream
import com.example.irl.util.AppListProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class BlockingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_IS_BEDTIME = "extra_is_bedtime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val endTimeMs = intent.getLongExtra(EXTRA_END_TIME, 0L)
        val modeStr = intent.getStringExtra(EXTRA_MODE) ?: "FOCUS"
        val isBedtime = intent.getBooleanExtra(EXTRA_IS_BEDTIME, false)
        val mode = BlockMode.fromString(modeStr)

        val appLabel = AppListProvider.getAppLabel(this, blockedPackage)

        setContent {
            IRLTheme {
                BackHandler {
                    returnToHomeScreen()
                }

                BlockingScreenContent(
                    appLabel = appLabel,
                    blockedPackage = blockedPackage,
                    endTimeMs = endTimeMs,
                    mode = mode,
                    isBedtime = isBedtime,
                    onReturnToIRL = { returnToIRL() },
                    onGoToHome = { returnToHomeScreen() },
                    onTakeGentleBreak = {
                        val repo = AppPreferencesRepository.getInstance(applicationContext)
                        lifecycleScope.launch {
                            repo.grantEmergencyBreak(5)
                            finish()
                        }
                    }
                )
            }
        }
    }

    private fun returnToIRL() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun returnToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun BlockingScreenContent(
    appLabel: String,
    blockedPackage: String,
    endTimeMs: Long,
    mode: BlockMode,
    isBedtime: Boolean,
    onReturnToIRL: () -> Unit,
    onGoToHome: () -> Unit,
    onTakeGentleBreak: () -> Unit
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val remainingMs = (endTimeMs - currentTimeMs).coerceAtLeast(0L)
    val remainingSeconds = (remainingMs / 1000) % 60
    val remainingMinutes = (remainingMs / (1000 * 60)) % 60
    val remainingHours = remainingMs / (1000 * 60 * 60)

    val timeString = if (remainingHours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds)
    } else {
        String.format(Locale.US, "%02d:%02d", remainingMinutes, remainingSeconds)
    }

    var gentleCooldownSeconds by remember { mutableIntStateOf(10) }
    var isCooldownFinished by remember { mutableStateOf(false) }

    LaunchedEffect(mode) {
        if (mode == BlockMode.GENTLE && !isBedtime) {
            for (i in 10 downTo 1) {
                gentleCooldownSeconds = i
                delay(1000L)
            }
            isCooldownFinished = true
            gentleCooldownSeconds = 0
        }
    }

    // Choose background and accent colors per mode
    val (bgBrush, accentColor, circleColor) = when {
        isBedtime -> Triple(
            Brush.verticalGradient(listOf(SurfacePlum, MutedPlum.copy(alpha = 0.85f))),
            DustyRose,
            Lavender
        )
        mode == BlockMode.GENTLE -> Triple(
            Brush.verticalGradient(listOf(DeepTeal, DeepTealLight.copy(alpha = 0.9f))),
            SageGreen,
            SageGreen.copy(alpha = 0.7f)
        )
        mode == BlockMode.FOCUS -> Triple(
            Brush.verticalGradient(listOf(DeepTeal, DeepTeal.copy(alpha = 0.85f))),
            ButterYellow,
            ButterYellow.copy(alpha = 0.5f)
        )
        else -> Triple(
            Brush.verticalGradient(listOf(MutedPlum, MutedPlum.copy(alpha = 0.8f))),
            Coral,
            Coral.copy(alpha = 0.5f)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepTeal
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Mode badge
                val badgeText = when {
                    isBedtime -> "Bedtime"
                    mode == BlockMode.GENTLE -> "Gentle focus"
                    mode == BlockMode.FOCUS -> "Focus session"
                    else -> "Deep focus"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Focus illustration
                FocusCircleIllustration(
                    size = 148.dp,
                    primaryColor = accentColor.copy(alpha = 0.7f),
                    accentColor = circleColor
                )

                Spacer(modifier = Modifier.height(28.dp))

                // App blocked text
                Text(
                    text = "Hold on.",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = WarmCream,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$appLabel is paused.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = WarmCream.copy(alpha = 0.75f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Countdown
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = WarmCream,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = if (isBedtime) "until bedtime ends" else "remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmCream.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Primary action
                Button(
                    onClick = onReturnToIRL,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmCream,
                        contentColor = DeepTeal
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Return to IRL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onGoToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WarmCream.copy(alpha = 0.8f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        WarmCream.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Go to Home Screen",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Gentle mode break option
                if (mode == BlockMode.GENTLE && !isBedtime) {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isCooldownFinished) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Mindful pause unlocks in ${gentleCooldownSeconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmCream.copy(alpha = 0.55f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (10 - gentleCooldownSeconds) / 10f },
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = SageGreen,
                                trackColor = WarmCream.copy(alpha = 0.15f)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onTakeGentleBreak,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SageGreen
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                SageGreen.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Take 5-min mindful break",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Calming note
                Text(
                    text = "Your real life is happening right now.",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = WarmCream.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
