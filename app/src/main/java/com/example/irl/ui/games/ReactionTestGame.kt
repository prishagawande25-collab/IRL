package com.example.irl.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class ReactionState {
    IDLE,
    WAITING_FOR_SIGNAL,
    SIGNAL_ACTIVE,
    FALSE_START,
    RESULT
}

@Composable
fun ReactionTestGame(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }
    val bestTimeMs by repo.bestReactionTimeMsFlow.collectAsState(initial = 0L)

    var state by remember { mutableStateOf(ReactionState.IDLE) }
    var signalTriggerTimeMs by remember { mutableLongStateOf(0L) }
    var reactionTimeResultMs by remember { mutableLongStateOf(0L) }
    var falseStartPenaltyCount by remember { mutableIntStateOf(0) }
    var isNewBest by remember { mutableStateOf(false) }
    var pointsEarned by remember { mutableIntStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun startRound() {
        timerJob?.cancel()
        state = ReactionState.WAITING_FOR_SIGNAL
        signalTriggerTimeMs = 0L

        // Random delay between 2000ms and 4500ms
        val randomDelay = Random.nextLong(2000L, 4500L)
        timerJob = scope.launch {
            delay(randomDelay)
            if (state == ReactionState.WAITING_FOR_SIGNAL) {
                signalTriggerTimeMs = System.currentTimeMillis()
                state = ReactionState.SIGNAL_ACTIVE
            }
        }
    }

    fun handleSurfaceTap() {
        when (state) {
            ReactionState.IDLE -> {
                startRound()
            }
            ReactionState.WAITING_FOR_SIGNAL -> {
                // False start! Penalize.
                timerJob?.cancel()
                falseStartPenaltyCount++
                state = ReactionState.FALSE_START
            }
            ReactionState.SIGNAL_ACTIVE -> {
                val now = System.currentTimeMillis()
                val measured = now - signalTriggerTimeMs
                val penaltyMs = falseStartPenaltyCount * 150L
                val totalResult = measured + penaltyMs
                reactionTimeResultMs = totalResult

                val pts = if (totalResult < 250L) 60 else 40
                pointsEarned = pts

                scope.launch {
                    isNewBest = repo.saveReactionTimeScore(totalResult)
                    repo.addPoints(pts)
                    if (totalResult < 300L) {
                        repo.unlockAchievement("ach_reaction_fast")
                    }
                }
                state = ReactionState.RESULT
            }
            ReactionState.FALSE_START, ReactionState.RESULT -> {
                startRound()
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            ReactionState.SIGNAL_ACTIVE -> ElectricBlue
            ReactionState.FALSE_START -> DangerCrimson.copy(alpha = 0.4f)
            ReactionState.WAITING_FOR_SIGNAL -> DangerCrimson.copy(alpha = 0.15f)
            else -> NavySurfaceElevated
        },
        label = "ReactionBgAnim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reaction Test",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap immediately when color flashes electric blue",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                if (bestTimeMs > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best: ${bestTimeMs}ms",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Reaction Pad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .border(
                        1.dp,
                        if (state == ReactionState.SIGNAL_ACTIVE) ElectricBlue else NavyCardStroke,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { handleSurfaceTap() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (state) {
                        ReactionState.IDLE -> {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tap Anywhere to Start",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Wait for the blue signal. False starts penalized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                        ReactionState.WAITING_FOR_SIGNAL -> {
                            Text(
                                text = "Wait for Electric Blue...",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Do not tap yet!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        ReactionState.SIGNAL_ACTIVE -> {
                            Text(
                                text = "TAP NOW!",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyBackground
                            )
                        }
                        ReactionState.FALSE_START -> {
                            Text(
                                text = "Too Soon!",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerCrimson
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "False start penalized (+150ms). Tap to retry.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        ReactionState.RESULT -> {
                            Text(
                                text = "${reactionTimeResultMs} ms",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            if (falseStartPenaltyCount > 0) {
                                Text(
                                    text = "includes ${falseStartPenaltyCount * 150}ms false-start penalty",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarningAmber
                                )
                            }
                            if (isNewBest) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "New Personal Best!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "+$pointsEarned Wellbeing Points • Tap to Try Again",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        falseStartPenaltyCount = 0
                        startRound()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
                ) {
                    Text("Restart", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
